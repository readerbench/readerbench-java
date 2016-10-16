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

import data.AbstractDocument.SaveType;
import data.Block;
import data.Lang;
import data.cscl.Conversation;
import data.cscl.TimeStats;
import data.cscl.Utterance;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.openide.util.Exceptions;
import services.semanticModels.SemanticModel;

public class TimeStatistics {

    public static Logger logger = Logger.getLogger(TimeStatistics.class);

    private static final String CORPORA_PATH = "resources/in/corpus_v2/";

    public static void main(String[] args) {

        // Map<String, TimeStats> timeStatsPerChat = new HashMap<String, TimeStats>();
        Map<Integer, TimeStats> timeStatsGlobal = new TreeMap<>();

        try {
            Files.walk(Paths.get(TimeStatistics.CORPORA_PATH)).forEach(filePath -> {
                String filePathString = filePath.toString();
                if (filePathString.contains("in.xml")) {
                    logger.info("Processing file " + filePath.getFileName().toString() + " ...");
                    Map<SemanticModel, String> modelPaths = new EnumMap<>(SemanticModel.class);
                    modelPaths.put(SemanticModel.LSA, "resources/config/LSA/tasa_en");
                    modelPaths.put(SemanticModel.LDA, "resources/config/LDA/tasa_en");
                    
                    Conversation c = Conversation.load(filePathString, modelPaths, Lang.en, false);
                    c.computeAll(true);
                    c.save(SaveType.SERIALIZED_AND_CSV_EXPORT);

                    logger.info("Conversation has " + c.getBlocks().size() + " blocks.");

                    for (int i = 0; i < c.getBlocks().size(); i++) {
                        Block block1 = c.getBlocks().get(i);
                        if (block1 != null) {
                            Utterance utterance1 = (Utterance) block1;
                            logger.info("Processing contribution " + block1.getText());
                            if (block1.getRefBlock() != null && block1.getRefBlock().getIndex() != 0) {
                                Block block2 = c.getBlocks().get(block1.getRefBlock().getIndex());
                                if (block2 != null) {
                                    // new reference was found

                                    Utterance utterance2 = (Utterance) block2;
                                    logger.info("First utt time: " + utterance1.getTime() + "; second utt time: "
                                            + utterance2.getTime());
                                    if (utterance1.getTime().after(utterance2.getTime())) {
                                        DateUtils.addHours(utterance2.getTime(), 24);
                                    }
                                    int timp = (int) getDateDiff(utterance2.getTime(), utterance1.getTime(),
                                            TimeUnit.SECONDS);
                                    logger.info("Difference in seconds: " + timp);
                                    if (timeStatsGlobal.get(timp) == null) {
                                        timeStatsGlobal.put(timp,
                                                new TimeStats(0, // references
                                                        0, // same speaker
                                                        // references
                                                        0 // different speaker
                                                // references
                                                ));
                                    }
                                    timeStatsGlobal.get(timp)
                                            .setExplicitLinks(timeStatsGlobal.get(timp).getExplicitLinks() + 1);
                                    logger.info("Processing refered contribution " + block2.getText());

                                    // global information for the conversation
                                    // corpus
                                    if (utterance1.getParticipant() == utterance2.getParticipant()) {
                                        timeStatsGlobal.get(timp)
                                                .setSameSpeaker(timeStatsGlobal.get(timp).getSameSpeaker() + 1);
                                    } else {
                                        timeStatsGlobal.get(timp).setDifferentSpeaker(
                                                timeStatsGlobal.get(timp).getDifferentSpeaker() + 1);
                                    }
                                }
                            }
                        }
                    }
                }

            });

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        logger.info("Printing final contribution times for conversations.");

        // printTimesToCSVFile(timeStatsGlobal, no_references);
        printConversationStatsToCSVFile(timeStatsGlobal);

    }

    private static void printConversationStatsToCSVFile(Map<Integer, TimeStats> timeStatsGlobal) {

        try {

            StringBuilder sb = new StringBuilder();
            sb.append("sep=,\ntime,explicit links,same speaker,different speaker\n");

            for (Map.Entry pair : timeStatsGlobal.entrySet()) {
                TimeStats cs = (TimeStats) pair.getValue();
                sb.append(pair.getKey());
                sb.append(",");
                sb.append(cs.getExplicitLinks());
                sb.append(",");
                sb.append(cs.getSameSpeaker());
                sb.append(",");
                sb.append(cs.getDifferentSpeaker());
                sb.append("\n");
            }

            File file = new File(CORPORA_PATH + "time_stats.csv");
            try {
                FileUtils.writeStringToFile(file, sb.toString());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                Exceptions.printStackTrace(e);
            }
            logger.info("Printed conversation time stats to CSV file: " + file.getAbsolutePath());
        } catch (Exception e) {
            logger.info("Exception: " + e.getMessage());
            Exceptions.printStackTrace(e);
        }

    }

    private static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

}
