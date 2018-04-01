/*
 * Copyright 2017 ReaderBench.
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
import data.Block;
import data.Lang;
import data.cscl.Community;
import data.cscl.Conversation;
import data.document.Document;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.apache.commons.io.FilenameUtils;
import org.openide.util.Exceptions;
import services.commons.Formatting;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndices;
import services.complexity.DataGathering;
import webService.ReaderBenchServer;

import static view.widgets.ReaderBenchView.LOGGER;

/**
 *
 * @author ReaderBench
 */
public class PostQuality {

    public static void writeHeader(String path, Lang lang) {
        // create measurements.csv header
        try (BufferedWriter out = new BufferedWriter(new FileWriter(path + "/" + new File(path).getName() + "-" + "measurements.csv", false))) {
            StringBuilder concat = new StringBuilder();
            concat.append("SEP=,\n");
            concat.append("Community,Conversation,Posts,Paragraphs,Sentences,Words,Content words");
            ComplexityIndices.getIndices(lang).stream().forEach((factor) -> {
                concat.append(",RB.").append(factor.getAcronym());
            });
            out.write(concat.toString());
        } catch (Exception e) {
            LOGGER.severe("Runtime error while initializing measurements.csv file");
            Exceptions.printStackTrace(e);
        }
    }

    public static void writeResults(String communityName, Conversation originalConversation, AbstractDocument simplifiedConversation, String path) {
        try (BufferedWriter out = new BufferedWriter(new FileWriter(path + "/" + new File(path).getName() + "-" + "measurements.csv", true))) {
            StringBuilder concat = new StringBuilder();
            concat.append("\n").append(communityName);
            concat.append(",").append(FilenameUtils.removeExtension(new File(originalConversation.getPath()).getName().replaceAll(",", "")));
            concat.append(",").append(originalConversation.getBlocks().size() - 1);
            concat.append(",").append(simplifiedConversation.getNoBlocks());
            concat.append(",").append(simplifiedConversation.getNoSentences());
            concat.append(",").append(simplifiedConversation.getNoWords());
            concat.append(",").append(simplifiedConversation.getNoContentWords());
            for (ComplexityIndex factor : ComplexityIndices.getIndices(simplifiedConversation.getLanguage())) {
                concat.append(",").append(Formatting.formatNumber(simplifiedConversation.getComplexityIndices().get(factor)));
            }
            out.write(concat.toString());
        } catch (IOException ex) {
            LOGGER.severe("Runtime error while initializing measurements.csv file");
            Exceptions.printStackTrace(ex);
        }
    }

    public static void processInitialPosts(String path, Lang lang) {
        File dir = new File(path);
        writeHeader(path, lang);
        if (!dir.isDirectory()) {
            return;
        }
        for (File subdir : dir.listFiles((File f) -> f.isDirectory())) {
            for (File f : subdir.listFiles((File file) -> file.getName().endsWith(".ser"))) {
                Conversation c;
                try {
                    c = (Conversation) Conversation.loadSerializedDocument(f.getPath());
                    AbstractDocument simplifiedConversation = new Document(null, c.getSemanticModels(), c.getLanguage());
                    if (!c.getBlocks().isEmpty() && c.getBlocks().get(0) != null) {
                        Block.addBlock(simplifiedConversation, c.getBlocks().get(0));
                    }
                    else {
                        continue;
                    }
                    simplifiedConversation.determineWordOccurences(simplifiedConversation.getBlocks());
                    simplifiedConversation.determineSemanticDimensions();
                    ComplexityIndices.computeComplexityFactors(simplifiedConversation);
                    writeResults(subdir.getName(), c, simplifiedConversation, path);
                } catch (IOException | ClassNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    public static void main(String[] args) {
        //initialize DB
        ReaderBenchServer.initializeDB();

        PostQuality.processInitialPosts("dragos-resources", Lang.en);
    }
}
