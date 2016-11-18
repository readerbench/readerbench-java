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

import data.Block;
import data.Lang;
import data.cscl.CSCLConstants;
import data.cscl.ChatStats;
import data.cscl.Conversation;
import data.cscl.DistanceStats;
import data.cscl.Utterance;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import services.commons.Formatting;
import services.semanticModels.SimilarityType;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class DistanceStatistics {

    public static final Logger LOGGER = Logger.getLogger("");
    private static int no_references = 0;

    public static void main(String[] args) {
        Map<Integer, DistanceStats> blockDistances = new TreeMap<>(
                (Integer o1, Integer o2) -> o1.compareTo(o2));
        Map<String, ChatStats> chatStats = new HashMap<>();

        Map<SimilarityType, String> modelPaths = new EnumMap<>(SimilarityType.class);
        modelPaths.put(SimilarityType.LSA, CSCLConstants.LSA_PATH);
        modelPaths.put(SimilarityType.LDA, CSCLConstants.LDA_PATH);

        int maxRefDistance = 20;

        try {
            Files.walk(Paths.get(CSCLConstants.CSCL_CORPUS)).forEach((Path filePath) -> {
                String filePathString = filePath.toString();
                if (filePathString.contains("in.xml") && FilenameUtils.getExtension(filePathString).compareTo("xml") == 0) {
                    LOGGER.log(Level.INFO, "Processing file {0}", filePath.getFileName().toString());
                    Conversation c = Conversation.load(filePathString, modelPaths, Lang.en, CSCLConstants.USE_POSTAGGING);
                    c.computeAll(CSCLConstants.DIALOGISM);
                    //c.save(SaveType.SERIALIZED_AND_CSV_EXPORT);

                    Utterance firstUtt = null;
                    for (int i = 1; i < c.getBlocks().size(); i++) {
                        firstUtt = (Utterance) c.getBlocks().get(i);
                        if (firstUtt != null) {
                            break;
                        }
                    }
                    Utterance lastUtt = null;
                    for (int i = c.getBlocks().size() - 1; i > 0; i--) {
                        lastUtt = (Utterance) c.getBlocks().get(i);
                        if (lastUtt != null) {
                            break;
                        }
                    }

                    int timp = 0;
                    if (firstUtt != null && lastUtt != null) {
                        timp = (int) TimeHelper.getDateDiff(firstUtt.getTime(), lastUtt.getTime(), TimeUnit.MINUTES);
                    }
                    // save conversation info
                    chatStats.put(filePath.getFileName().toString(),
                            new ChatStats(c.getBlocks().size(), // contributions
                                    c.getParticipants().size(), // participants
                                    timp, // duration
                                    0, // explicitLinks
                                    0, // coverage
                                    0, // same speaker first
                                    0, // different speaker first
                                    0, // same block
                                    0, // different block
                                    null // references
                            ));

                    Map<Integer, Integer> references = new HashMap<>();
                    for (int i = 1; i <= maxRefDistance; i++) {
                        references.put(i, 0);
                    }

                    // first parameter = distance
                    // second parameter = number of links
                    LOGGER.log(Level.INFO, "Conversation has {0} blocks.", c.getBlocks().size());

                    for (int i = 0; i < c.getBlocks().size(); i++) {
                        Block block1 = c.getBlocks().get(i);
                        if (block1 != null) {
                            Utterance utterance1 = (Utterance) block1;
                            LOGGER.log(Level.INFO, "Processing contribution {0}", block1.getText());
                            if (block1.getRefBlock() != null && block1.getRefBlock().getIndex() != 0) {
                                Block block2 = c.getBlocks().get(block1.getRefBlock().getIndex());
                                if (block2 != null) {
                                    // count new reference
                                    no_references++;
                                    Utterance utterance2 = (Utterance) block2;
                                    chatStats.get(filePath.getFileName().toString()).setExplicitLinks(
                                            chatStats.get(filePath.getFileName().toString()).getExplicitLinks() + 1);
                                    LOGGER.log(Level.INFO, "Processing refered contribution {0}", block2.getText());
                                    // for (int j = i - 20; j < i && j > 0 &&
                                    // (block2 = c.getBlocks().get(j)) != null
                                    // && block2.getRefBlock() != null &&
                                    // block2.getRefBlock().getIndex() != 0;
                                    // j++) {
                                    int distance = getBlockDistance(block1, block2);

                                    // global information for the conversation
                                    // corpus
                                    DistanceStats ds;
                                    if (blockDistances.get(distance) != null) {
                                        // blockDistances.put(distance,
                                        // blockDistances.get(distance) + 1);
                                        ds = blockDistances.get(distance);
                                        ds.setTotal(ds.getTotal() + 1);
                                        if (utterance1.getParticipant() == utterance2.getParticipant()) {
                                            ds.setSameSpeaker(ds.getSameSpeaker() + 1);
                                            if (distance == 1) {
                                                ds.setSameSpeakerFirst(ds.getSameSpeakerFirst() + 1);
                                                chatStats.get(filePath.getFileName().toString()).setSameSpeakerFirst(
                                                        chatStats.get(filePath.getFileName().toString())
                                                                .getSameSpeakerFirst() + 1);
                                            }
                                            // check if same block
                                            boolean sameBlock = true;
                                            for (int k = block2.getIndex() + 1; k < block1.getIndex(); k++) {
                                                Utterance aux = (Utterance) c.getBlocks().get(k);
                                                if (aux != null
                                                        && aux.getParticipant() != utterance1.getParticipant()) {
                                                    sameBlock = false;
                                                    break;
                                                }
                                            }
                                            if (sameBlock) {
                                                chatStats.get(filePath.getFileName().toString()).setSameBlock(
                                                        chatStats.get(filePath.getFileName().toString()).getSameBlock()
                                                        + 1);
                                            }
                                            // end check if same block
                                        } else {
                                            ds.setDifferentSpeaker(ds.getDifferentSpeaker() + 1);
                                            if (distance == 1) {
                                                ds.setDifferentSpeakerFirst(ds.getDifferentSpeakerFirst() + 1);
                                                chatStats.get(filePath.getFileName().toString())
                                                        .setDifferentSpeakerFirst(
                                                                chatStats.get(filePath.getFileName().toString())
                                                                        .getDifferentSpeakerFirst() + 1);
                                            }
                                        }
                                    } else if (utterance1.getParticipant() == utterance2.getParticipant()) {
                                        ds = new DistanceStats(1, 1, 0, 0, 0);
                                        if (distance == 1) {
                                            ds.setSameSpeakerFirst(ds.getSameSpeakerFirst() + 1);
                                        }
                                        boolean sameBlock = true;
                                        for (int k = block2.getIndex() + 1; k < block1.getIndex(); k++) {
                                            Utterance aux = (Utterance) c.getBlocks().get(k);
                                            if (aux.getParticipant() != utterance1.getParticipant()) {
                                                sameBlock = false;
                                                chatStats.get(filePath.getFileName().toString()).setDifferentBlock(
                                                        chatStats.get(filePath.getFileName().toString())
                                                                .getDifferentBlock() + 1);
                                                break;
                                            }
                                        }
                                        if (sameBlock) {
                                            chatStats.get(filePath.getFileName().toString()).setSameBlock(
                                                    chatStats.get(filePath.getFileName().toString()).getSameBlock()
                                                    + 1);
                                        }
                                    } else {
                                        ds = new DistanceStats(1, 0, 1, 0, 0);
                                    }
                                    blockDistances.put(distance, ds);

                                    // local information for the conversation
                                    // file stats
                                    if (distance <= maxRefDistance) {
                                        if (references.get(distance) != null) {
                                            references.put(distance, references.get(distance) + 1);
                                        } else {
                                            references.put(distance, 1);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    chatStats.get(filePath.getFileName().toString())
                            .setCoverage((double) chatStats.get(filePath.getFileName().toString())
                                    .getExplicitLinks()
                                    / chatStats.get(filePath.getFileName().toString())
                                            .getContributions());

                    chatStats.get(filePath.getFileName().toString()).setReferences(references);

                    LOGGER.log(Level.INFO, "Printing contribution distances for chat {0}", c.getPath());
                    LOGGER.log(Level.INFO, "Max distance for chat: {0}", blockDistances.size());
                    for (Map.Entry pair : blockDistances.entrySet()) {
                        LOGGER.log(Level.INFO, "{0} = {1}", new Object[]{pair.getKey(), pair.getValue()});
                    }
                }
            });
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        LOGGER.info("Printing final contribution distances for conversations.");
        LOGGER.log(Level.INFO, "Max distance for all conersations: {0}", blockDistances.size());
        for (Map.Entry pair : blockDistances.entrySet()) {
            LOGGER.log(Level.INFO, "{0} = {1}", new Object[]{pair.getKey(), pair.getValue()});
        }

        printDistancesToCSVFile(blockDistances, no_references);
        printConversationStatsToCSVFile(chatStats, maxRefDistance);
    }

    private static void printConversationStatsToCSVFile(Map<String, ChatStats> chatStats, int maxRefDistance) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "sep=,\nchat id,contrubtions,participants,duration,explicit links,coverage,same speaker first,different speaker first,same block,different block,\n");
            for (int i = 1; i <= maxRefDistance; i++) {
                sb.append("d").append(i).append(CSCLConstants.CSV_DELIM);
            }
            for (Map.Entry pair : chatStats.entrySet()) {
                ChatStats cs = (ChatStats) pair.getValue();
                sb.append(pair.getKey()).append(CSCLConstants.CSV_DELIM);
                sb.append(cs.getContributions()).append(CSCLConstants.CSV_DELIM);
                sb.append(cs.getParticipants()).append(CSCLConstants.CSV_DELIM);
                sb.append(cs.getDuration()).append(CSCLConstants.CSV_DELIM);
                sb.append(cs.getExplicitLinks()).append(CSCLConstants.CSV_DELIM);
                sb.append(Formatting.formatNumber(cs.getCoverage())).append(CSCLConstants.CSV_DELIM);
                sb.append(cs.getSameSpeakerFirst()).append(CSCLConstants.CSV_DELIM);
                sb.append(cs.getDifferentSpeakerFirst()).append(CSCLConstants.CSV_DELIM);
                sb.append(cs.getSameBlock()).append(CSCLConstants.CSV_DELIM);
                sb.append(cs.getDifferentBlock()).append(CSCLConstants.CSV_DELIM);
                LOGGER.log(Level.INFO, "References for {0} file: {1}", new Object[]{pair.getKey(), cs.getReferences().size()});
                for (Map.Entry pairReference : cs.getReferences().entrySet()) {
                    sb.append(pairReference.getValue()).append(CSCLConstants.CSV_DELIM);
                }
                sb.append("\n");
            }

            File file = new File(CSCLConstants.CSCL_CORPUS + "/conversations_stats.csv");
            try {
                FileUtils.writeStringToFile(file, sb.toString());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            LOGGER.log(Level.INFO, "Printed conversation stats to CSV file: {0}", file.getAbsolutePath());
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Exception: {0}", e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printDistancesToCSVFile(Map<Integer, DistanceStats> blockDistances, int no_references) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "sep=,\ndistance,total,same speaker,different speaker,%,same speaker first,different speaker first\n");
            for (Map.Entry pair : blockDistances.entrySet()) {
                DistanceStats ds = (DistanceStats) pair.getValue();
                sb.append(pair.getKey()).append(CSCLConstants.CSV_DELIM);
                sb.append(ds.getTotal()).append(CSCLConstants.CSV_DELIM);
                sb.append(ds.getSameSpeaker()).append(CSCLConstants.CSV_DELIM);
                sb.append(ds.getDifferentSpeaker()).append(CSCLConstants.CSV_DELIM);
                sb.append(ds.getTotal() / no_references).append(CSCLConstants.CSV_DELIM);
                sb.append(ds.getSameSpeakerFirst()).append(CSCLConstants.CSV_DELIM);
                sb.append(ds.getDifferentSpeakerFirst()).append("\n");
            }
            File file = new File(CSCLConstants.CSCL_CORPUS + "/distances_stats.csv");
            try {
                FileUtils.writeStringToFile(file, sb.toString());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            LOGGER.log(Level.INFO, "Printed distances to CSV file: {0}", file.getAbsolutePath());
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Exception: {0}", e.getMessage());
            e.printStackTrace();
        }
    }

    private static int getBlockDistance(Block block1, Block block2) {
        return Math.abs(block2.getIndex() - block1.getIndex());
    }
}
