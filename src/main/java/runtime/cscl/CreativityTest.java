/*
 * Copyright 2016 ReaderBench.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package runtime.cscl;

import data.AbstractDocument;
import data.Lang;
import data.cscl.CSCLIndices;
import data.cscl.Conversation;
import data.cscl.Participant;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import services.commons.Formatting;
import services.complexity.dialogism.AvgNoVoices;
import services.complexity.dialogism.VoicesAvgSpan;
import services.complexity.dialogism.VoicesMaxSpan;
import services.replicatedWorker.SerialCorpusAssessment;

/**
 *
 * @author Mihai Dascalu
 */
public class CreativityTest {

    private static final Logger LOGGER = Logger.getLogger(CreativityTest.class.getName());

    public static void processAllFolders(String folder, String prefix, boolean restartProcessing, String pathToLSA, String pathToLDA, Lang lang, boolean usePOSTagging) {
        File dir = new File(folder);

        if (dir.isDirectory()) {
            File[] communityFolder = dir.listFiles();
            for (File f : communityFolder) {
                if (f.isDirectory() && f.getName().startsWith(prefix)) {
                    if (restartProcessing) {
                        // remove checkpoint file
                        File checkpoint = new File(f.getPath() + "/checkpoint.xml");
                        if (checkpoint.exists()) {
                            checkpoint.delete();
                        }
                    }
                    SerialCorpusAssessment.processCorpus(f.getAbsolutePath(), pathToLSA, pathToLDA, lang, usePOSTagging,
                            true, true, AbstractDocument.SaveType.SERIALIZED_AND_CSV_EXPORT);
                    processConversations(f.getAbsolutePath());
                }
            }
        }
        LOGGER.info("Finished processsing all files ...");
    }

    public static void processConversations(String path) {
        LOGGER.log(Level.INFO, "Loading all files in {0}", path);

        FileFilter filter = (File f) -> f.getName().endsWith(".ser");
        File[] filesTODO = (new File(path)).listFiles(filter);

        File output = new File(path + "/measurements.xml");
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"), 32768)) {
            out.write("Filename,AVG(Social KB), ABS(Social KB), AVG(Dialogism), ABS(Dialogism),Voices, Avg voices, Avg voice span, Max voice span");
            for (File f : filesTODO) {
                Conversation c = (Conversation) Conversation.loadSerializedDocument(f.getPath());
                if (c.getParticipants().size() != 2) {
                    LOGGER.log(Level.WARNING, "Incorrect number of participants for {0}", f.getPath());
                } else {
                    Participant p1 = c.getParticipants().get(0);
                    Participant p2 = c.getParticipants().get(1);
                    out.write("\n" + f.getPath()
                            + "," + Formatting.formatNumber((p1.getIndices().get(CSCLIndices.SOCIAL_KB) + p2.getIndices().get(CSCLIndices.SOCIAL_KB)) / 2)
                            + "," + Formatting.formatNumber(Math.abs(p1.getIndices().get(CSCLIndices.SOCIAL_KB) - p2.getIndices().get(CSCLIndices.SOCIAL_KB)))
                            + "," + Formatting.formatNumber((p1.getIndices().get(CSCLIndices.INTER_ANIMATION_DEGREE) + p2.getIndices().get(CSCLIndices.INTER_ANIMATION_DEGREE)) / 2)
                            + "," + Formatting.formatNumber(Math.abs(p1.getIndices().get(CSCLIndices.INTER_ANIMATION_DEGREE) - p2.getIndices().get(CSCLIndices.INTER_ANIMATION_DEGREE)))
                            + "," + c.getVoices().size()
                            + "," + Formatting.formatNumber(new AvgNoVoices().compute(c))
                            + "," + Formatting.formatNumber(new VoicesAvgSpan().compute(c))
                            + "," + Formatting.formatNumber(new VoicesMaxSpan().compute(c))
                    );
                }
            }
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
            Exceptions.printStackTrace(e);
        }
    }

    public static void main(String[] args) {
        CreativityTest.processAllFolders("resources/in/creativity", "", false, "resources/config/EN/LSA/TASA", "resources/config/EN/LDA/TASA", Lang.en, true);
    }
}
