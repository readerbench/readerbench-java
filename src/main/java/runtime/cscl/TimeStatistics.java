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
import data.cscl.Conversation;
import data.cscl.TimeStats;
import data.cscl.Utterance;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DateUtils;
import org.openide.util.Exceptions;
import services.semanticModels.SimilarityType;

/**
 * Computes time statistics for CSCL chat conversations
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class TimeStatistics {

    private static final Logger LOGGER = Logger.getLogger("");
    private static FileHandler fh;
    private static final String CORPORA_PATH = "resources/in/corpus_v2/";
    private static int explicitLinks = 0;
    private static int sameSpeaker = 0;
    private static int differentSpeaker = 0;

    /**
     * Starts the conversation analysis for time statistics
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {

        try {
            fh = new FileHandler("time_statistics.log");
            LOGGER.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        }

        Map<String, TimeStats> timeStatsPerChat = new TreeMap<>();
        Map<Integer, TimeStats> timeStatsGlobal = new TreeMap<>();

        try {
            Files.walk(Paths.get(TimeStatistics.CORPORA_PATH)).forEach(filePath -> {
                String filePathString = filePath.toString();
                // TODO: replace with mimetype
                if (filePathString.contains("in.xml")) {
                    LOGGER.log(Level.INFO, "Processing file {0} ...", filePath.getFileName().toString());
                    Map<SimilarityType, String> modelPaths = new EnumMap<>(SimilarityType.class);
                    modelPaths.put(SimilarityType.LSA, "resources/config/EN/LSA/TASA");
                    modelPaths.put(SimilarityType.LDA, "resources/config/EN/LDA/TASA");

                    Lang lang = Lang.getLang("English");
                    LOGGER.log(Level.INFO, "Trying to load {0} file.", filePathString);
                    Conversation c = Conversation.load(filePathString, modelPaths, Lang.getLang("English"), false);
                    //c.computeAll(false);
                    //c.save(SaveType.SERIALIZED_AND_CSV_EXPORT);

                    LOGGER.log(Level.INFO, "Conversation has {0} blocks.", c.getBlocks().size());
                    for (int i = 0; i < c.getBlocks().size(); i++) {
                        Block block1 = c.getBlocks().get(i);
                        if (block1 != null) {
                            Utterance utterance1 = (Utterance) block1;
                            LOGGER.log(Level.INFO, "Processing contribution {0}", block1.getText());
                            if (block1.getRefBlock() != null && block1.getRefBlock().getIndex() != 0) {
                                Block block2 = c.getBlocks().get(block1.getRefBlock().getIndex());
                                if (block2 != null) {
                                    explicitLinks++;
                                    // new reference was found
                                    Utterance utterance2 = (Utterance) block2;
                                    LOGGER.log(Level.INFO, "First utt time: {0}; second utt time: {1}", new Object[]{utterance1.getTime(), utterance2.getTime()});
                                    if (utterance1.getTime().after(utterance2.getTime())) {
                                        DateUtils.addHours(utterance2.getTime(), 24);
                                        LOGGER.log(Level.INFO, "(Updated) First utt time: {0}; second utt time: {1}", new Object[]{utterance1.getTime(), utterance2.getTime()});
                                    }
                                    int timp = (int) getDateDiff(utterance2.getTime(), utterance1.getTime(),
                                            TimeUnit.SECONDS);
                                    LOGGER.log(Level.INFO, "Difference in seconds: {0}", timp);
                                    if (timeStatsGlobal.get(timp) == null) {
                                        timeStatsGlobal.put(timp, new TimeStats());
                                    }
                                    timeStatsGlobal.get(timp)
                                            .setExplicitLinks(timeStatsGlobal.get(timp).getExplicitLinks() + 1);
                                    LOGGER.log(Level.INFO, "Processing refered contribution {0}", block2.getText());

                                    // global information for the conversation
                                    // corpus
                                    if (utterance1.getParticipant() == utterance2.getParticipant()) {
                                        sameSpeaker++;
                                        LOGGER.log(Level.INFO, "Same participants.");
                                        timeStatsGlobal.get(timp)
                                                .setSameSpeaker(timeStatsGlobal.get(timp).getSameSpeaker() + 1);
                                    } else {
                                        differentSpeaker++;
                                        LOGGER.log(Level.INFO, "Different participants.");
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
            e.printStackTrace();
        }

        LOGGER.info("Printing final contribution times for conversations.");
        // printTimesToCSVFile(timeStatsGlobal, no_references);
        printConversationStatsToCSVFile(timeStatsGlobal, explicitLinks, sameSpeaker, differentSpeaker);
    }

    /**
     * Print CSCL time statistics to file
     *
     * @param timeStatsGlobal The HashMap containing the statistics
     */
    private static void printConversationStatsToCSVFile(Map<Integer, TimeStats> timeStatsGlobal,
            int totalExplicitLinks, int totalSameSpeaker, int totalDifferentSpeaker) {
        NumberFormat formatter = new DecimalFormat("#0.00");
        StringBuilder sb = new StringBuilder();
        sb.append("sep=,\ntime (min),time (s),explicit links,% of total explicit,same speaker,% of total same speaker,% of total explicit,different speaker,% of total different speaker,% of total explicit\n");
        int explicitLinks = 0;
        int sameSpeaker = 0;
        int differentSpeaker = 0;
        Map<Integer, Integer> hm = new HashMap<>();
        int seconds[] = {
            1, // 1 second
            2, // 2 seconds
            3, // 3 seconds
            5, // 5 seconds
            10, // 10 seconds
            20, // 20 seconds
            30, // 30 seconds
            60, // 1 minute
            90, // 1.5 minutes
            120, // 2 minutes
            150, // 2.5 minutes
            180, // 3 minutes
            240, // 4 minutes
            300, // 5 minutes
            420, // 7 minutes
            600, // 10 minutes
            900, // 15 minutes
            1200, // 20 minutes
            1800, // 30 minutes
            3600, // 1 hours
            5400, // 1.5 hours
            7200, // 2 hours
            Integer.MAX_VALUE // more than 2 hours
        };
        Map<Integer, TimeStats> timeStatsGrouped = new TreeMap<>();
        for (int i = 0; i < seconds.length; i++) {
            hm.put(seconds[i], 0);
            if (timeStatsGrouped.get(seconds[i]) == null) {
                timeStatsGrouped.put(seconds[i], new TimeStats());
            }
        }

        for (Map.Entry pair : timeStatsGlobal.entrySet()) {
            TimeStats cs = (TimeStats) pair.getValue();
            int secondsGlobal = (int) pair.getKey();
            sb.append(Math.floor((double) (secondsGlobal / 60.0)));
            sb.append(",");
            sb.append(secondsGlobal);
            sb.append(",");
            sb.append(cs.getExplicitLinks());
            sb.append(",");
            sb.append(formatter.format(cs.getExplicitLinks() / (double) totalExplicitLinks));
            sb.append(",");
            sb.append(cs.getSameSpeaker());
            sb.append(",");
            sb.append(formatter.format(cs.getSameSpeaker() / (double) totalSameSpeaker));
            sb.append(",");
            sb.append(formatter.format(cs.getSameSpeaker() / (double) totalExplicitLinks));
            sb.append(",");
            sb.append(cs.getDifferentSpeaker());
            sb.append(",");
            sb.append(formatter.format(cs.getDifferentSpeaker() / (double) totalDifferentSpeaker));
            sb.append(",");
            sb.append(formatter.format(cs.getDifferentSpeaker() / (double) totalExplicitLinks));
            sb.append("\n");
            explicitLinks += cs.getExplicitLinks();
            sameSpeaker += cs.getSameSpeaker();
            differentSpeaker += cs.getDifferentSpeaker();

            for (Map.Entry pairGeneral : hm.entrySet()) {
                int secs = (Integer) pairGeneral.getKey();
                if (secondsGlobal <= secs) {
                    timeStatsGrouped.get(secs).setExplicitLinks(
                            timeStatsGrouped.get(secs).getExplicitLinks()
                            + cs.getExplicitLinks());
                    timeStatsGrouped.get(secs).setSameSpeaker(
                            timeStatsGrouped.get(secs).getSameSpeaker()
                            + cs.getSameSpeaker());
                    timeStatsGrouped.get(secs).setDifferentSpeaker(
                            timeStatsGrouped.get(secs).getDifferentSpeaker()
                            + cs.getDifferentSpeaker());
                }
            }
        }
        // totals
        sb.append(",,");
        sb.append(explicitLinks);
        sb.append(",,");
        sb.append(sameSpeaker);
        sb.append(",,,");
        sb.append(differentSpeaker);
        sb.append("\n\n");

        // grouped
        explicitLinks = 0;
        sameSpeaker = 0;
        differentSpeaker = 0;
        sb.append("grouped\n");
        sb.append("time (hours), time (min),time (s),explicit links,% of total explicit,same speaker,% of total same speaker,% of total explicit,different speaker,% of total different speaker,% of total explicit\n");
        for (Map.Entry pair : timeStatsGrouped.entrySet()) {
            TimeStats ts = (TimeStats) pair.getValue();
            int secondsGrouped = (int) pair.getKey();
            sb.append(formatter.format((double) (secondsGrouped / (60.0 * 60.0))));
            sb.append(",");
            sb.append(formatter.format((double) (secondsGrouped / 60.0)));
            sb.append(",");
            sb.append(secondsGrouped);
            sb.append(",");
            sb.append(ts.getExplicitLinks());
            sb.append(",");
            sb.append(formatter.format(ts.getExplicitLinks() / (double) totalExplicitLinks));
            sb.append(",");
            sb.append(ts.getSameSpeaker());
            sb.append(",");
            sb.append(formatter.format(ts.getSameSpeaker() / (double) totalSameSpeaker));
            sb.append(",");
            sb.append(formatter.format(ts.getSameSpeaker() / (double) totalExplicitLinks));
            sb.append(",");
            sb.append(ts.getDifferentSpeaker());
            sb.append(",");
            sb.append(formatter.format(ts.getDifferentSpeaker() / (double) totalDifferentSpeaker));
            sb.append(",");
            sb.append(formatter.format(ts.getDifferentSpeaker() / (double) totalExplicitLinks));
            sb.append("\n");
//            explicitLinks += ts.getExplicitLinks();
//            sameSpeaker += ts.getSameSpeaker();
//            differentSpeaker += ts.getDifferentSpeaker();
        }
        // totals
//        sb.append(",,");
//        sb.append(explicitLinks);
//        sb.append(",");
//        sb.append(sameSpeaker);
//        sb.append(",");
//        sb.append(differentSpeaker);
//        sb.append("\n\n");

        try {
            File file = new File(CORPORA_PATH + "time_stats.csv");
            try {
                FileUtils.writeStringToFile(file, sb.toString());
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
            LOGGER.log(Level.INFO, "Printed conversation time stats to CSV file: {0}", file.getAbsolutePath());
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Exception: {0}", e.getMessage());
            Exceptions.printStackTrace(e);
        }
    }

    /**
     * Computes the difference between two dates
     *
     * @param date1 First datetime
     * @param date2 Second datetime
     * @param timeUnit Time unit
     * @return The difference in time units between dates
     */
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = Math.abs(date2.getTime() - date1.getTime());
        // if the difference between the date is negative, add one day
        if (diffInMillies < 0) {
            diffInMillies += 1000 * 60 * 60 * 24;
        }
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

}
