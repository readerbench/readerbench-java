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
import data.cscl.ChatStats;
import data.cscl.Conversation;
import data.cscl.DistanceStats;
import data.cscl.Utterance;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import services.commons.Formatting;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class DistanceStatistics {

    public static Logger logger = Logger.getLogger(DistanceStatistics.class);

    private static final String CORPORA_PATH = "resources/in/corpus_v2/";
    private static int no_references = 0;

    public static void main(String[] args) {

        Map<Integer, DistanceStats> blockDistances = new TreeMap<>(
                new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });
        Map<String, ChatStats> chatStats = new HashMap<>();

        try {
            Files.walk(Paths.get(DistanceStatistics.CORPORA_PATH)).forEach(filePath -> {
                String filePathString = filePath.toString();
                if (filePathString.contains("in.xml")) {
                    logger.info("Processing file " + filePath.getFileName().toString());

                    Conversation c = Conversation.load(filePathString, "resources/config/LSA/tasa_en",
                            "resources/config/LDA/tasa_en", Lang.eng, false, true);
                    c.computeAll(true, null, null, SaveType.SERIALIZED_AND_CSV_EXPORT);

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
                        // add 24 hours if last utterance's time is lower then
                        // first utterance's time (midnight passed)
                        if (firstUtt.getTime().after(lastUtt.getTime())) {
                            DateUtils.addHours(lastUtt.getTime(), 24);
                        }
                        timp = (int) getDateDiff(firstUtt.getTime(), lastUtt.getTime(), TimeUnit.MINUTES);
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
                    for (int i = 1; i < 5; i++) {
                        references.put(i, 0);
                    }

                    // first parameter = distance
                    // second parameter = number of links
                    logger.info("Conversation has " + c.getBlocks().size() + " blocks.");

                    for (int i = 0; i < c.getBlocks().size(); i++) {
                        Block block1 = c.getBlocks().get(i);
                        if (block1 != null) {
                            Utterance utterance1 = (Utterance) block1;
                            logger.info("Processing contribution " + block1.getText());
                            if (block1.getRefBlock() != null && block1.getRefBlock().getIndex() != 0) {
                                Block block2 = c.getBlocks().get(block1.getRefBlock().getIndex());
                                if (block2 != null) {

                                    // count new reference
                                    no_references++;
                                    Utterance utterance2 = (Utterance) block2;
                                    chatStats.get(filePath.getFileName().toString()).setExplicitLinks(
                                            chatStats.get(filePath.getFileName().toString()).getExplicitLinks() + 1);
                                    chatStats.get(filePath.getFileName().toString())
                                            .setCoverage((double) chatStats.get(filePath.getFileName().toString())
                                                    .getExplicitLinks()
                                                    / chatStats.get(filePath.getFileName().toString())
                                                    .getContributions());
                                    logger.info("Processing refered contribution " + block2.getText());
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
                                    if (distance <= 5) {
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

                    chatStats.get(filePath.getFileName().toString()).setReferences(references);

                    logger.info("Printing contribution distances for chat " + c.getPath());
                    logger.info("Max distance for chat: " + blockDistances.size());
                    Iterator it = blockDistances.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        logger.info(pair.getKey() + " = " + pair.getValue());
                    }
                }
            });
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        logger.info("Printing final contribution distances for conversations.");
        logger.info("Max distance for all conersations: " + blockDistances.size());
        Iterator it = blockDistances.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            logger.info(pair.getKey() + " = " + pair.getValue());
        }

        printDistancesToCSVFile(blockDistances, no_references);
        printConversationStatsToCSVFile(chatStats);

    }

    private static void printConversationStatsToCSVFile(Map<String, ChatStats> chatStats) {

        try {

            StringBuilder sb = new StringBuilder();
            sb.append(
                    "sep=,\nchat id,contrubtions,participants,duration,explicit links,coverage,same speaker first,different speaker first,same block,different block,d1,d2,d3,d4,d5\n");

            Iterator it = chatStats.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                ChatStats cs = (ChatStats) pair.getValue();
                sb.append(pair.getKey());
                sb.append(",");
                sb.append(cs.getContributions());
                sb.append(",");
                sb.append(cs.getParticipants());
                sb.append(",");
                sb.append(cs.getDuration());
                sb.append(",");
                sb.append(cs.getExplicitLinks());
                sb.append(",");
                sb.append(Formatting.formatNumber(cs.getCoverage()));
                sb.append(",");
                sb.append(cs.getSameSpeakerFirst());
                sb.append(",");
                sb.append(cs.getDifferentSpeakerFirst());
                sb.append(",");
                sb.append(cs.getSameBlock());
                sb.append(",");
                sb.append(cs.getDifferentBlock());
                sb.append(",");

                logger.info("References for " + pair.getKey() + " file: " + cs.getReferences().size());
                Iterator itReferences = cs.getReferences().entrySet().iterator();
                while (itReferences.hasNext()) {
                    Map.Entry pairReference = (Map.Entry) itReferences.next();
                    sb.append(pairReference.getValue());
                    sb.append(",");
                }

                sb.append("\n");
            }

            File file = new File(CORPORA_PATH + "stats.csv");
            try {
                FileUtils.writeStringToFile(file, sb.toString());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            logger.info("Printed conversation stats to CSV file: " + file.getAbsolutePath());

        } catch (Exception e) {
            logger.info("Exception: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private static void printDistancesToCSVFile(Map<Integer, DistanceStats> blockDistances, int no_references) {
        // String prependPath =
        // "/Users/Berilac/Projects/Eclipse/readerbench/resources/";

        try {

            StringBuilder sb = new StringBuilder();
            sb.append(
                    "sep=,\ndistance,total,same speaker,different speaker,%,same speaker first,different speaker first\n");

            Iterator it = blockDistances.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                DistanceStats ds = (DistanceStats) pair.getValue();
                sb.append(pair.getKey());
                sb.append(",");
                sb.append(ds.getTotal());
                sb.append(",");
                sb.append(ds.getSameSpeaker());
                sb.append(",");
                sb.append(ds.getDifferentSpeaker());
                sb.append(",");
                sb.append(ds.getTotal() / no_references);
                sb.append(",");
                sb.append(ds.getSameSpeakerFirst());
                sb.append(",");
                sb.append(ds.getDifferentSpeakerFirst());
                sb.append("\n");
            }

            File file = new File(CORPORA_PATH + "distances.csv");
            try {
                FileUtils.writeStringToFile(file, sb.toString());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            logger.info("Printed distances to CSV file: " + file.getAbsolutePath());

        } catch (Exception e) {
            logger.info("Exception: " + e.getMessage());
            e.printStackTrace();
        }

    }

    private static int getBlockDistance(Block block1, Block block2) {
        return Math.abs(block2.getIndex() - block1.getIndex());
    }

    private static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

}