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

import data.Lang;
import data.cscl.CSCLConstants;
import data.cscl.Conversation;
import data.cscl.SimilarityFormula;
import data.cscl.Utterance;
import data.discourse.SemanticCohesion;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.time.DateUtils;
import org.openide.util.Exceptions;
import services.commons.Formatting;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.SimilarityType;
import webService.ReaderBenchServer;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class SimilarityAnalysis {

    public static final Logger LOGGER = Logger.getLogger("");

    private final String path;
    private final Lang lang;
    private final boolean usePOSTagging;
    private final boolean computeDialogism;
    private List<Integer> windowSizes = null;
    private final int maxWindowSize;
    private List<Integer> timeFrames = null;
    private final int maxTimeFrame;
    private final LSA lsa;
    private final LDA lda;

    private final Map<Integer, Map<SimilarityFormula, Map<SimilarityType, Map<Integer, Integer>>>> totalSimDetected;
    private final Map<Integer, Map<SimilarityFormula, Map<SimilarityType, Map<Integer, Double>>>> percentageSimDetected;
    private final Map<Integer, Map<SimilarityFormula, Map<SimilarityType, Map<Integer, Integer>>>> totalSimInBlock;
    private final Map<Integer, Map<SimilarityFormula, Map<SimilarityType, Map<Integer, Double>>>> percentageSimInBlock;
    private Map<String, Integer> chatContributions;
    private Map<String, Integer> chatExplicitLinks;

    /**
     *
     * @param path
     * @param lang
     * @param usePOSTagging
     * @param computeDialogism
     * @param windowSizes
     * @param timeFrames
     * @param lsa
     * @param lda
     */
    public SimilarityAnalysis(String path, Lang lang,
            boolean usePOSTagging, boolean computeDialogism,
            List<Integer> windowSizes, List<Integer> timeFrames,
            LSA lsa, LDA lda) {
        this.path = path;
        this.lang = lang;
        this.usePOSTagging = usePOSTagging;
        this.computeDialogism = computeDialogism;
        Collections.sort(windowSizes, Collections.reverseOrder());
        this.windowSizes = windowSizes;
        this.maxWindowSize = Collections.max(windowSizes);
        Collections.sort(timeFrames, Collections.reverseOrder());
        this.timeFrames = timeFrames;
        this.maxTimeFrame = Collections.max(timeFrames);
        this.lsa = lsa;
        this.lda = lda;

        totalSimDetected = new HashMap<>();
        percentageSimDetected = new HashMap<>();
        totalSimInBlock = new HashMap<>();
        percentageSimInBlock = new HashMap<>();
        chatContributions = new HashMap<>();
        chatExplicitLinks = new HashMap<>();
    }

    /**
     *
     * @param formulas
     * @return
     */
    private String capTabel(List<SimilarityFormula> formulas) {
        StringBuilder capTabel = new StringBuilder();
        capTabel.append("chat").append(CSCLConstants.CSV_DELIM);
        capTabel.append("utt_id").append(CSCLConstants.CSV_DELIM);
        capTabel.append("utt_participant").append(CSCLConstants.CSV_DELIM);
        for (int i = 1; i <= maxWindowSize; i++) {
            capTabel.append("d").append(i).append(CSCLConstants.CSV_DELIM);
            capTabel.append("d").append(i).append("_norm").append(CSCLConstants.CSV_DELIM);
            capTabel.append("d").append(i).append("_mihalcea").append(CSCLConstants.CSV_DELIM);
        }
        capTabel.append("utt_text").append(CSCLConstants.CSV_DELIM); // contribution text
        capTabel.append("utt_time").append(CSCLConstants.CSV_DELIM); // contribution time
        capTabel.append("ref_id").append(CSCLConstants.CSV_DELIM); // reference id
        capTabel.append("ref_participant").append(CSCLConstants.CSV_DELIM); // participant
        capTabel.append("ref_text").append(CSCLConstants.CSV_DELIM); // reference text
        capTabel.append("ref_time").append(CSCLConstants.CSV_DELIM); // reference time
        capTabel.append("utt_ref_time_diff").append(CSCLConstants.CSV_DELIM); // (contribution, reference) time diff

        // reference within window sizes (in terms of distance)
        for (Integer windowSize : windowSizes) {
            for (SimilarityFormula formula : formulas) {
                capTabel.append("max_sim_").append(formula.getAcronym()).append("_distance_").append(windowSize).append(CSCLConstants.CSV_DELIM); // max sim
                capTabel.append("max_sim_id_").append(formula.getAcronym()).append("_distance_").append(windowSize).append(CSCLConstants.CSV_DELIM); // id of max sim utt
                capTabel.append("max_sim_participant_").append(formula.getAcronym()).append("_distance_").append(windowSize).append(CSCLConstants.CSV_DELIM); // participant of max sim utt
                capTabel.append("max_sim_ref_detected_").append(formula.getAcronym()).append("_distance_").append(windowSize).append(CSCLConstants.CSV_DELIM); // ref detected
                capTabel.append("max_sim_ref_in_block_").append(formula.getAcronym()).append("_distance_").append(windowSize).append(CSCLConstants.CSV_DELIM); // ref in block
            }
        }

        // references within time frames
        for (Integer timeFrame : timeFrames) {
            for (SimilarityFormula formula : formulas) {
                capTabel.append("max_sim_").append(formula.getAcronym()).append("_time_").append(timeFrame).append(CSCLConstants.CSV_DELIM); // max sim
                capTabel.append("max_sim_id_").append(formula.getAcronym()).append("_time_").append(timeFrame).append(CSCLConstants.CSV_DELIM); // id of max sim utt
                capTabel.append("max_sim_participant_").append(formula.getAcronym()).append("_time_").append(timeFrame).append(CSCLConstants.CSV_DELIM); // participant of max sim utt
                capTabel.append("max_sim_ref_detected_").append(formula.getAcronym()).append("_time_").append(timeFrame).append(CSCLConstants.CSV_DELIM); // ref detected
                capTabel.append("max_sim_ref_in_block_").append(formula.getAcronym()).append("_time_").append(timeFrame).append(CSCLConstants.CSV_DELIM); // ref in block
            }
        }
        capTabel.append('\n');
        return capTabel.toString();
    }

    /**
     *
     */
    public void process() {
        List<Integer> analysisTypes = new ArrayList();
        analysisTypes.add(CSCLConstants.DISTANCE_ANALYSIS);
        analysisTypes.add(CSCLConstants.TIME_ANALYSIS);

        List<SimilarityType> methods = new ArrayList();
        methods.add(SimilarityType.LSA);
        methods.add(SimilarityType.LDA);
        methods.add(SimilarityType.LEACOCK_CHODOROW);
        methods.add(SimilarityType.WU_PALMER);
        methods.add(SimilarityType.PATH_SIM);

        List<SimilarityFormula> formulas = new ArrayList();
        formulas.add(SimilarityFormula.READERBENCH_SIM);
        formulas.add(SimilarityFormula.NORMALIZED_SIM);
        formulas.add(SimilarityFormula.MIHALCEA_SIM);

        for (Integer analysisType : analysisTypes) {
            totalSimDetected.put(analysisType, new HashMap<>());
            percentageSimDetected.put(analysisType, new HashMap<>());
            totalSimInBlock.put(analysisType, new HashMap<>());
            percentageSimInBlock.put(analysisType, new HashMap<>());
            for (SimilarityFormula formula : formulas) {
                totalSimDetected.get(analysisType).put(formula, new HashMap<>());
                percentageSimDetected.get(analysisType).put(formula, new HashMap<>());
                totalSimInBlock.get(analysisType).put(formula, new HashMap<>());
                percentageSimInBlock.get(analysisType).put(formula, new HashMap<>());
                for (SimilarityType method : methods) {
                    totalSimDetected.get(analysisType).get(formula).put(method, new HashMap<>());
                    percentageSimDetected.get(analysisType).get(formula).put(method, new HashMap<>());
                    totalSimInBlock.get(analysisType).get(formula).put(method, new HashMap<>());
                    percentageSimInBlock.get(analysisType).get(formula).put(method, new HashMap<>());
                }
            }
        }
        for (SimilarityFormula formula : formulas) {
            for (SimilarityType method : methods) {
                for (Integer windowSize : windowSizes) {
                    totalSimDetected.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).put(windowSize, 0);
                    percentageSimDetected.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).put(windowSize, 0.0);
                    totalSimInBlock.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).put(windowSize, 0);
                    percentageSimInBlock.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).put(windowSize, 0.0);
                }
                for (Integer timeFrame : timeFrames) {
                    totalSimDetected.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).put(timeFrame, 0);
                    percentageSimDetected.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).put(timeFrame, 0.0);
                    totalSimInBlock.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).put(timeFrame, 0);
                    percentageSimInBlock.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).put(timeFrame, 0.0);
                }
            }
        }

        LOGGER.info("Starting conversation processing...");
        try {
            Map<SimilarityType, File> files = new HashMap<>();
            for (SimilarityType method : methods) {
                files.put(method, new File(path + "/similarity_scores_" + method.getAcronym() + ".csv"));
                try {
                    FileUtils.writeStringToFile(files.get(method), capTabel(formulas), "UTF-8");
                } catch (IOException e) {
                    LOGGER.log(Level.INFO, "Exception: {0}", e.getMessage());
                    e.printStackTrace();
                }
            }

            Map<SimilarityType, File> filesGeneral = new HashMap<>();
            for (SimilarityType method : methods) {
                filesGeneral.put(method, new File(path + "/similarity_stats_" + method.getAcronym() + ".csv"));
                try {
                    FileUtils.writeStringToFile(filesGeneral.get(method), capTabelStats(formulas), "UTF-8");
                } catch (IOException e) {
                    LOGGER.log(Level.INFO, "Exception: {0}", e.getMessage());
                    e.printStackTrace();
                }
            }

            Files.walk(Paths.get(path)).forEach((Path filePath) -> {
                Integer explicitLinks = 0;
                String filePathString = filePath.toString();
                String fileExtension = FilenameUtils.getExtension(filePathString);
                if (filePathString.contains("in.xml") && fileExtension.compareTo("xml") == 0) {
                    chatExplicitLinks.put(filePath.getFileName().toString(), 0);
                    Map<SimilarityType, StringBuilder> hmRowBuilderGeneral = new HashMap<>();
                    for (SimilarityType method : methods) {
                        hmRowBuilderGeneral.put(method, new StringBuilder());
                    }

                    Map<Integer, Map<SimilarityFormula, Map<SimilarityType, Map<Integer, Integer>>>> simDetected = new HashMap<>();
                    Map<Integer, Map<SimilarityFormula, Map<SimilarityType, Map<Integer, Integer>>>> simInBlock = new HashMap<>();
                    for (Integer analysisType : analysisTypes) {
                        simDetected.put(analysisType, new HashMap<>());
                        simInBlock.put(analysisType, new HashMap<>());
                        for (SimilarityFormula formula : formulas) {
                            simDetected.get(analysisType).put(formula, new HashMap<>());
                            simInBlock.get(analysisType).put(formula, new HashMap<>());
                            for (SimilarityType method : methods) {
                                simDetected.get(analysisType).get(formula).put(method, new HashMap<>());
                                simInBlock.get(analysisType).get(formula).put(method, new HashMap<>());
                            }
                        }
                    }
                    for (Integer windowSize : windowSizes) {
                        for (SimilarityType method : methods) {
                            for (SimilarityFormula formula : formulas) {
                                simDetected.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).put(windowSize, 0);
                                simInBlock.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).put(windowSize, 0);
                            }
                        }
                    }
                    for (Integer timeFrame : timeFrames) {
                        for (SimilarityType method : methods) {
                            for (SimilarityFormula formula : formulas) {
                                simDetected.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).put(timeFrame, 0);
                                simInBlock.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).put(timeFrame, 0);
                            }
                        }
                    }

                    LOGGER.log(Level.INFO, "Processing chat {0}", filePath.getFileName());
                    Map<SimilarityType, String> modelPaths = new EnumMap<>(SimilarityType.class);
                    modelPaths.put(SimilarityType.LSA, lsa.getPath());
                    modelPaths.put(SimilarityType.LDA, lda.getPath());

                    Conversation c = Conversation.load(filePathString, modelPaths, lang, usePOSTagging);
                    c.computeAll(computeDialogism);

                    Utterance firstUtt, secondUtt;
                    for (int i = 1; i < c.getBlocks().size(); i++) {
                        firstUtt = (Utterance) c.getBlocks().get(i);
                        if (firstUtt != null) {
                            Map<SimilarityType, StringBuilder> hmRowBuilder = new HashMap<>();
                            for (SimilarityType method : methods) {
                                hmRowBuilder.put(method, new StringBuilder());
                                hmRowBuilder.get(method)
                                        .append(filePath.getFileName()).append(CSCLConstants.CSV_DELIM)
                                        .append(firstUtt.getIndex()).append(CSCLConstants.CSV_DELIM)
                                        .append(firstUtt.getParticipant().getName()).append(CSCLConstants.CSV_DELIM);
                            }

                            int kDistance = 0;
                            int kTime = 0;

                            Map<Integer, Map<SimilarityFormula, Map<SimilarityType, Map<Integer, Double>>>> max = new HashMap<>();
                            Map<Integer, Map<SimilarityFormula, Map<SimilarityType, Map<Integer, Integer>>>> refMax = new HashMap<>();
                            Map<Integer, Map<SimilarityFormula, Map<SimilarityType, Map<Integer, String>>>> participantMax = new HashMap<>();
                            for (Integer analysisType : analysisTypes) {
                                max.put(analysisType, new HashMap<>());
                                refMax.put(analysisType, new HashMap<>());
                                participantMax.put(analysisType, new HashMap<>());
                                for (SimilarityFormula formula : formulas) {
                                    max.get(analysisType).put(formula, new HashMap<>());
                                    refMax.get(analysisType).put(formula, new HashMap<>());
                                    participantMax.get(analysisType).put(formula, new HashMap<>());
                                    for (SimilarityType method : methods) {
                                        max.get(analysisType).get(formula).put(method, new HashMap<>());
                                        refMax.get(analysisType).get(formula).put(method, new HashMap<>());
                                        participantMax.get(analysisType).get(formula).put(method, new HashMap<>());
                                    }
                                }
                            }

                            for (Integer windowSize : windowSizes) {
                                for (SimilarityFormula formula : formulas) {
                                    for (SimilarityType method : methods) {
                                        max.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).put(windowSize, -1.0);
                                        refMax.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).put(windowSize, -1);
                                        participantMax.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).put(windowSize, null);
                                    }
                                }
                            }
                            for (Integer timeFrame : timeFrames) {
                                for (SimilarityFormula formula : formulas) {
                                    for (SimilarityType method : methods) {
                                        max.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).put(timeFrame, -1.0);
                                        refMax.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).put(timeFrame, -1);
                                        participantMax.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).put(timeFrame, null);
                                    }
                                }
                            }

                            // windowSize
                            for (int j = i - 1; j >= i - maxWindowSize && j > 0; j--) {
                                secondUtt = (Utterance) c.getBlocks().get(j);
                                if (secondUtt != null) {
                                    int distance = i - j;
                                    double sim;
                                    // Mihalcea end
                                    SemanticCohesion sc = new SemanticCohesion(firstUtt, secondUtt);
                                    for (SimilarityType method : methods) {
                                        for (SimilarityFormula formula : formulas) {
                                            if (null == formula) {
                                                sim = -1;
                                            } else {
                                                switch (formula) {
                                                    case READERBENCH_SIM:
                                                        sim = sc.getSemanticSimilarities().get(method);
                                                        break;
                                                    case NORMALIZED_SIM:
                                                        sim = sc.getSemanticSimilarities().get(method) / (i - j + 1);
                                                        break;
                                                    case MIHALCEA_SIM:
                                                        sim = MihalceaSimilarity.compute(firstUtt, secondUtt, method, lsa, lda);
                                                        break;
                                                    default:
                                                        sim = -1;
                                                        break;
                                                }
                                            }
                                            hmRowBuilder.get(method).append(Formatting.formatNumber(sim)).append(CSCLConstants.CSV_DELIM);
                                            for (Integer windowSize : windowSizes) {
                                                if (distance <= windowSize && max.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).get(windowSize) < sim) {
                                                    max.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).put(windowSize, sim);
                                                    refMax.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).put(windowSize, secondUtt.getIndex());
                                                    participantMax.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).put(windowSize, secondUtt.getParticipant().getName());
                                                }
                                            }
                                        }
                                    }
                                    kDistance++;
                                }
                            }

                            for (int j = kDistance; j < maxWindowSize; j++) {
                                for (SimilarityType method : methods) {
                                    hmRowBuilder.get(method).append(CSCLConstants.CSV_DELIM)
                                            .append(CSCLConstants.CSV_DELIM)
                                            .append(CSCLConstants.CSV_DELIM);
                                }
                            }

                            // time iteration
                            for (int j = i - 1; j > 0; j--) {
                                secondUtt = (Utterance) c.getBlocks().get(j);
                                if (secondUtt != null) {
                                    if (firstUtt.getTime().after(secondUtt.getTime())) {
                                        DateUtils.addHours(secondUtt.getTime(), 24);
                                        //LOGGER.log(Level.INFO, "(Updated) First utt time: {0}; second utt time: {1}", new Object[]{firstUtt.getTime(), secondUtt.getTime()});
                                    }
                                    int diffTimpUttRef = (int) TimeHelper.getDateDiff(secondUtt.getTime(), firstUtt.getTime(), TimeUnit.SECONDS);
                                    // ignore if above max time frame
                                    if (diffTimpUttRef > maxTimeFrame) {
                                        continue;
                                    }
                                    double sim;
                                    // Mihalcea end
                                    SemanticCohesion sc = new SemanticCohesion(firstUtt, secondUtt);
                                    for (SimilarityType method : methods) {
                                        for (SimilarityFormula formula : formulas) {
                                            if (null == formula) {
                                                sim = -1;
                                            } else {
                                                switch (formula) {
                                                    case READERBENCH_SIM:
                                                        sim = sc.getSemanticSimilarities().get(method);
                                                        break;
                                                    case NORMALIZED_SIM:
                                                        sim = sc.getSemanticSimilarities().get(method) / (i - j + 1);
                                                        break;
                                                    case MIHALCEA_SIM:
                                                        sim = MihalceaSimilarity.compute(firstUtt, secondUtt, method, lsa, lda);
                                                        break;
                                                    default:
                                                        sim = -1;
                                                        break;
                                                }
                                            }
                                            //hmRowBuilder.get(method).append(Formatting.formatNumber(sim)).append(CSCLConstants.CSV_DELIM);
                                            for (Integer timeFrame : timeFrames) {
                                                if (diffTimpUttRef <= timeFrame && max.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).get(timeFrame) < sim) {
                                                    max.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).put(timeFrame, sim);
                                                    refMax.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).put(timeFrame, secondUtt.getIndex());
                                                    participantMax.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).put(timeFrame, secondUtt.getParticipant().getName());
                                                }
                                            }
                                        }
                                    }
                                    kTime++;
                                }
                            }

                            for (SimilarityType method : methods) {
                                // utterance text
                                hmRowBuilder.get(method).append(firstUtt.getProcessedText()).append(CSCLConstants.CSV_DELIM);
                                // utterance time
                                hmRowBuilder.get(method).append(firstUtt.getTime()).append(CSCLConstants.CSV_DELIM);
                            }

                            double refId = -1;
                            if (firstUtt.getRefBlock() != null && firstUtt.getRefBlock().getIndex() != 0) {
                                Utterance refUtt = (Utterance) c.getBlocks().get(firstUtt.getRefBlock().getIndex());
                                if (refUtt != null) {
                                    explicitLinks++;
                                    refId = refUtt.getIndex();
                                    if (firstUtt.getTime().after(refUtt.getTime())) {
                                        DateUtils.addHours(refUtt.getTime(), 24);
                                        //LOGGER.log(Level.INFO, "(Updated) First utt time: {0}; ref utt time: {1}", new Object[]{firstUtt.getTime(), refUtt.getTime()});
                                    }
                                    int diffTimpUttRef = (int) TimeHelper.getDateDiff(refUtt.getTime(), firstUtt.getTime(), TimeUnit.SECONDS);
                                    //LOGGER.log(Level.INFO, "Difference in seconds: {0}", diffTimpUttRef);
                                    for (SimilarityType method : methods) {
                                        // referred utterance id
                                        hmRowBuilder.get(method).append(refId).append(CSCLConstants.CSV_DELIM);
                                        // referred participant name
                                        hmRowBuilder.get(method).append(refUtt.getParticipant().getName()).append(CSCLConstants.CSV_DELIM);
                                        // referred utterance text
                                        hmRowBuilder.get(method).append(refUtt.getProcessedText()).append(CSCLConstants.CSV_DELIM);
                                        // referred utterance time
                                        hmRowBuilder.get(method).append(refUtt.getTime()).append(CSCLConstants.CSV_DELIM);
                                        // difference between ref_time and utt_time
                                        hmRowBuilder.get(method).append(diffTimpUttRef).append(CSCLConstants.CSV_DELIM);
                                    }
                                }
                            } // if ref id is not set, fill empty fields
                            else {
                                for (SimilarityType method : methods) {
                                    // referred utterance id
                                    hmRowBuilder.get(method).append("").append(CSCLConstants.CSV_DELIM);
                                    // referred participant name
                                    hmRowBuilder.get(method).append("").append(CSCLConstants.CSV_DELIM);
                                    // referred utterance text
                                    hmRowBuilder.get(method).append("").append(CSCLConstants.CSV_DELIM);
                                    // referred utterance time
                                    hmRowBuilder.get(method).append("").append(CSCLConstants.CSV_DELIM);
                                    // difference between ref_time and utt_time
                                    hmRowBuilder.get(method).append("").append(CSCLConstants.CSV_DELIM);
                                }
                            }
                            for (Integer windowSize : windowSizes) {
                                if (refId != -1) {
                                    for (SimilarityType method : methods) {
                                        for (SimilarityFormula formula : formulas) {
                                            if (refId == refMax.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).get(windowSize)) {
                                                simDetected.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).put(windowSize, simDetected.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).get(windowSize) + 1);
                                            }
                                        }
                                    }
                                }
                                for (SimilarityType method : methods) {
                                    for (SimilarityFormula formula : formulas) {
                                        // max sim
                                        hmRowBuilder.get(method).append(max.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).get(windowSize)).append(CSCLConstants.CSV_DELIM);
                                        // id of max sim
                                        hmRowBuilder.get(method).append(refMax.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).get(windowSize)).append(CSCLConstants.CSV_DELIM);
                                        // max sim participant name
                                        hmRowBuilder.get(method).append((participantMax.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).get(windowSize) != null) ? participantMax.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).get(windowSize) : "").append(CSCLConstants.CSV_DELIM);
                                        // ref detected?                                    
                                        hmRowBuilder.get(method).append(((refId != -1 && refId == refMax.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).get(windowSize)) ? 1 : 0)).append(CSCLConstants.CSV_DELIM);
                                        // ref in block?
                                        Integer minRef = (int) Math.min(refId, refMax.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).get(windowSize));
                                        Integer maxRef = (int) Math.max(refId, refMax.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).get(windowSize));
                                        boolean isInBlock = SimilarityHelper.isInBlock(c, minRef, maxRef);
                                        if (isInBlock) {
                                            simInBlock.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).put(windowSize, simInBlock.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).get(windowSize) + 1);
                                        }
                                        hmRowBuilder.get(method).append((isInBlock) ? 1 : 0).append(CSCLConstants.CSV_DELIM);
                                    }
                                }
                            }

                            for (Integer timeFrame : timeFrames) {
                                if (refId != -1) {
                                    for (SimilarityType method : methods) {
                                        for (SimilarityFormula formula : formulas) {
                                            if (refId == refMax.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).get(timeFrame)) {
                                                simDetected.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).put(timeFrame, simDetected.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).get(timeFrame) + 1);
                                            }
                                        }
                                    }
                                }
                                for (SimilarityType method : methods) {
                                    for (SimilarityFormula formula : formulas) {
                                        // max sim
                                        hmRowBuilder.get(method).append(max.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).get(timeFrame)).append(CSCLConstants.CSV_DELIM);
                                        // id of max sim
                                        hmRowBuilder.get(method).append(refMax.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).get(timeFrame)).append(CSCLConstants.CSV_DELIM);
                                        // max sim participant name
                                        hmRowBuilder.get(method).append((participantMax.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).get(timeFrame) != null) ? participantMax.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).get(timeFrame) : "").append(CSCLConstants.CSV_DELIM);
                                        // ref detected?                                    
                                        hmRowBuilder.get(method).append(((refId != -1 && refId == refMax.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).get(timeFrame)) ? 1 : 0)).append(CSCLConstants.CSV_DELIM);
                                        // ref in block?
                                        Integer minRef = (int) Math.min(refId, refMax.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).get(timeFrame));
                                        Integer maxRef = (int) Math.max(refId, refMax.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).get(timeFrame));
                                        boolean isInBlock = SimilarityHelper.isInBlock(c, minRef, maxRef);
                                        if (isInBlock) {
                                            simInBlock.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).put(timeFrame, simInBlock.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).get(timeFrame) + 1);
                                        }
                                        hmRowBuilder.get(method).append((isInBlock) ? 1 : 0).append(CSCLConstants.CSV_DELIM);
                                    }
                                }
                            }
                            // 3 x cautat alta referinta in blocul dat de
                            // replicile aceleiasi
                            // daca toate replicile intre minim si maxim
                            // (ref_id, max_det_algoritm) au ac speaker
                            // pe viitor : un bloc de replici ale aceleasi
                            // persoane sa fie un singur paragraf

                            for (SimilarityType method : methods) {
                                hmRowBuilder.get(method).append("\n");
                            }

                            if (i == c.getBlocks().size() - 1) {
                                // totals
                                for (SimilarityType method : methods) {
                                    hmRowBuilder.get(method).append(CSCLConstants.CSV_DELIM)
                                            .append(CSCLConstants.CSV_DELIM)
                                            .append(CSCLConstants.CSV_DELIM)
                                            .append(CSCLConstants.CSV_DELIM)
                                            .append(CSCLConstants.CSV_DELIM)
                                            .append(CSCLConstants.CSV_DELIM)
                                            .append(CSCLConstants.CSV_DELIM);
                                    for (int j = 0; j < maxWindowSize; j++) {
                                        hmRowBuilder.get(method).append(CSCLConstants.CSV_DELIM)
                                                .append(CSCLConstants.CSV_DELIM)
                                                .append(CSCLConstants.CSV_DELIM);
                                    }
                                    hmRowBuilder.get(method).append(CSCLConstants.CSV_DELIM)
                                            .append(CSCLConstants.CSV_DELIM)
                                            .append(CSCLConstants.CSV_DELIM);
                                    for (Integer windowSize : windowSizes) {
                                        for (SimilarityFormula formula : formulas) {
                                            hmRowBuilder.get(method).append(CSCLConstants.CSV_DELIM)
                                                    .append(CSCLConstants.CSV_DELIM)
                                                    .append(CSCLConstants.CSV_DELIM);
                                            hmRowBuilder.get(method).append(simDetected.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).get(windowSize)).append(CSCLConstants.CSV_DELIM);
                                            hmRowBuilder.get(method).append(simInBlock.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).get(windowSize)).append(CSCLConstants.CSV_DELIM);
                                            totalSimDetected.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).put(windowSize, totalSimDetected.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).get(windowSize) + simDetected.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).get(windowSize));
                                            percentageSimDetected.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).put(windowSize, (explicitLinks > 0) ? (totalSimDetected.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).get(windowSize) * 1.0 / explicitLinks) : 0);
                                            totalSimInBlock.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).put(windowSize, totalSimInBlock.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).get(windowSize) + simInBlock.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).get(windowSize));
                                            percentageSimInBlock.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).put(windowSize, (explicitLinks > 0) ? (totalSimInBlock.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).get(windowSize) * 1.0 / explicitLinks) : 0);
                                        }
                                    }
                                    for (Integer timeFrame : timeFrames) {
                                        for (SimilarityFormula formula : formulas) {
                                            hmRowBuilder.get(method).append(CSCLConstants.CSV_DELIM)
                                                    .append(CSCLConstants.CSV_DELIM)
                                                    .append(CSCLConstants.CSV_DELIM);
                                            hmRowBuilder.get(method).append(simDetected.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).get(timeFrame)).append(CSCLConstants.CSV_DELIM);
                                            hmRowBuilder.get(method).append(simInBlock.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).get(timeFrame)).append(CSCLConstants.CSV_DELIM);
                                            totalSimDetected.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).put(timeFrame, totalSimDetected.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).get(timeFrame) + simDetected.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).get(timeFrame));
                                            percentageSimDetected.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).put(timeFrame, (explicitLinks > 0) ? (totalSimDetected.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).get(timeFrame) * 1.0 / explicitLinks) : 0);
                                            totalSimInBlock.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).put(timeFrame, totalSimInBlock.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).get(timeFrame) + simInBlock.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).get(timeFrame));
                                            percentageSimInBlock.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).put(timeFrame, (explicitLinks > 0) ? (totalSimInBlock.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).get(timeFrame) * 1.0 / explicitLinks) : 0);
                                        }
                                    }
                                    hmRowBuilder.get(method).append("\n");
                                }
                            }

                            try {
                                for (SimilarityType method : methods) {
                                    FileUtils.writeStringToFile(files.get(method), hmRowBuilder.get(method).toString(), "UTF-8", true);
                                }
                            } catch (IOException e) {
                                LOGGER.log(Level.INFO, "Exception: {0}", e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    }

                    chatContributions.put(filePath.getFileName().toString(), c.getNoBlocks());
                    chatExplicitLinks.put(filePath.getFileName().toString(), explicitLinks);
                    for (SimilarityType method : methods) {
                        hmRowBuilderGeneral.get(method).append(filePath.getFileName()).append(CSCLConstants.CSV_DELIM);
                        hmRowBuilderGeneral.get(method).append(c.getNoBlocks()).append(CSCLConstants.CSV_DELIM);
                        hmRowBuilderGeneral.get(method).append(explicitLinks).append(CSCLConstants.CSV_DELIM);
                        for (Integer windowSize : windowSizes) {
                            for (SimilarityFormula formula : formulas) {
                                hmRowBuilderGeneral.get(method).append(simDetected.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).get(windowSize)).append(CSCLConstants.CSV_DELIM);
                                hmRowBuilderGeneral.get(method).append(explicitLinks > 0 ? simDetected.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).get(windowSize) * 1.0 / explicitLinks : 0).append(CSCLConstants.CSV_DELIM);
                            }
                        }
                        for (Integer windowSize : windowSizes) {
                            for (SimilarityFormula formula : formulas) {
                                hmRowBuilderGeneral.get(method).append(simInBlock.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).get(windowSize)).append(CSCLConstants.CSV_DELIM);
                                hmRowBuilderGeneral.get(method).append(explicitLinks > 0 ? simInBlock.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).get(windowSize) * 1.0 / explicitLinks : 0).append(CSCLConstants.CSV_DELIM);
                            }
                        }
                        for (Integer timeFrame : timeFrames) {
                            for (SimilarityFormula formula : formulas) {
                                hmRowBuilderGeneral.get(method).append(simDetected.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).get(timeFrame)).append(CSCLConstants.CSV_DELIM);
                                hmRowBuilderGeneral.get(method).append(explicitLinks > 0 ? simDetected.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).get(timeFrame) * 1.0 / explicitLinks : 0).append(CSCLConstants.CSV_DELIM);
                            }
                        }
                        for (Integer timeFrame : timeFrames) {
                            for (SimilarityFormula formula : formulas) {
                                hmRowBuilderGeneral.get(method).append(simInBlock.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).get(timeFrame)).append(CSCLConstants.CSV_DELIM);
                                hmRowBuilderGeneral.get(method).append(explicitLinks > 0 ? simInBlock.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).get(timeFrame) * 1.0 / explicitLinks : 0).append(CSCLConstants.CSV_DELIM);
                            }
                        }
                        hmRowBuilderGeneral.get(method).append("\n");
                        try {
                            FileUtils.writeStringToFile(filesGeneral.get(method), hmRowBuilderGeneral.get(method).toString(), "UTF-8", true);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            });

            Map<SimilarityType, StringBuilder> hmRowBuilder = new HashMap<>();
            for (SimilarityType method : methods) {
                hmRowBuilder.put(method, new StringBuilder());
                // append totals
                hmRowBuilder.get(method).append(CSCLConstants.CSV_DELIM)
                        .append(CSCLConstants.CSV_DELIM)
                        .append(CSCLConstants.CSV_DELIM)
                        .append(CSCLConstants.CSV_DELIM)
                        .append(CSCLConstants.CSV_DELIM)
                        .append(CSCLConstants.CSV_DELIM);
                for (int i = 0; i < maxWindowSize; i++) {
                    hmRowBuilder.get(method).append(CSCLConstants.CSV_DELIM)
                            .append(CSCLConstants.CSV_DELIM)
                            .append(CSCLConstants.CSV_DELIM);
                }
                hmRowBuilder.get(method).append(CSCLConstants.CSV_DELIM)
                        .append(CSCLConstants.CSV_DELIM)
                        .append(CSCLConstants.CSV_DELIM)
                        .append(CSCLConstants.CSV_DELIM);
                for (Integer windowSize : windowSizes) {
                    for (SimilarityFormula formula : formulas) {
                        hmRowBuilder.get(method).append(CSCLConstants.CSV_DELIM)
                                .append(CSCLConstants.CSV_DELIM)
                                .append(CSCLConstants.CSV_DELIM);
                        hmRowBuilder.get(method).append(totalSimDetected.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).get(windowSize)).append(CSCLConstants.CSV_DELIM);
                        hmRowBuilder.get(method).append(totalSimInBlock.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).get(windowSize)).append(CSCLConstants.CSV_DELIM);
                    }
                }
                for (Integer timeFrame : timeFrames) {
                    for (SimilarityFormula formula : formulas) {
                        hmRowBuilder.get(method).append(CSCLConstants.CSV_DELIM)
                                .append(CSCLConstants.CSV_DELIM)
                                .append(CSCLConstants.CSV_DELIM);
                        hmRowBuilder.get(method).append(totalSimDetected.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).get(timeFrame)).append(CSCLConstants.CSV_DELIM);
                        hmRowBuilder.get(method).append(totalSimInBlock.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).get(timeFrame)).append(CSCLConstants.CSV_DELIM);
                    }
                }
                hmRowBuilder.get(method).append('\n');
                FileUtils.writeStringToFile(files.get(method), hmRowBuilder.get(method).toString(), "UTF-8", true);
            }

            Map<SimilarityType, StringBuilder> hmRowBuilderGeneral = new HashMap<>();

            List<Integer> totalContributions = new ArrayList<>(chatContributions.values());
            int totalContribs = totalContributions.stream().mapToInt(Integer::intValue).sum();
            List<Integer> totalEplicitLinks = new ArrayList<>(chatExplicitLinks.values());
            int totalExplicit = totalEplicitLinks.stream().mapToInt(Integer::intValue).sum();

            for (SimilarityType method : methods) {
                hmRowBuilderGeneral.put(method, new StringBuilder());
                hmRowBuilderGeneral.get(method).append("Total").append(CSCLConstants.CSV_DELIM);
                hmRowBuilderGeneral.get(method).append(totalContribs).append(CSCLConstants.CSV_DELIM);
                hmRowBuilderGeneral.get(method).append(totalExplicit).append(CSCLConstants.CSV_DELIM);
                for (Integer windowSize : windowSizes) {
                    for (SimilarityFormula formula : formulas) {
                        hmRowBuilderGeneral.get(method).append(totalSimDetected.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).get(windowSize)).append(CSCLConstants.CSV_DELIM);
                        hmRowBuilderGeneral.get(method).append(percentageSimDetected.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).get(windowSize)).append(CSCLConstants.CSV_DELIM);
                    }
                }
                for (Integer windowSize : windowSizes) {
                    for (SimilarityFormula formula : formulas) {
                        hmRowBuilderGeneral.get(method).append(totalSimInBlock.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).get(windowSize)).append(CSCLConstants.CSV_DELIM);
                        hmRowBuilderGeneral.get(method).append(percentageSimInBlock.get(CSCLConstants.DISTANCE_ANALYSIS).get(formula).get(method).get(windowSize)).append(CSCLConstants.CSV_DELIM);
                    }
                }
                for (Integer timeFrame : timeFrames) {
                    for (SimilarityFormula formula : formulas) {
                        hmRowBuilderGeneral.get(method).append(totalSimDetected.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).get(timeFrame)).append(CSCLConstants.CSV_DELIM);
                        hmRowBuilderGeneral.get(method).append(percentageSimDetected.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).get(timeFrame)).append(CSCLConstants.CSV_DELIM);
                    }
                }
                for (Integer timeFrame : timeFrames) {
                    for (SimilarityFormula formula : formulas) {
                        hmRowBuilderGeneral.get(method).append(totalSimInBlock.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).get(timeFrame)).append(CSCLConstants.CSV_DELIM);
                        hmRowBuilderGeneral.get(method).append(percentageSimInBlock.get(CSCLConstants.TIME_ANALYSIS).get(formula).get(method).get(timeFrame)).append(CSCLConstants.CSV_DELIM);
                    }
                }
                hmRowBuilderGeneral.get(method).append('\n');

                hmRowBuilderGeneral.get(method).append("avg").append(CSCLConstants.CSV_DELIM).append(CSCLConstants.CSV_DELIM).append(CSCLConstants.CSV_DELIM);
                for (Integer windowSize : windowSizes) {
                    for (SimilarityFormula formula : formulas) {
                        hmRowBuilderGeneral.get(method).append(CSCLConstants.CSV_DELIM);
                        hmRowBuilderGeneral.get(method).append(CSCLConstants.CSV_DELIM);
                    }
                }
                for (Integer timeFrame : timeFrames) {
                    for (SimilarityFormula formula : formulas) {
                        hmRowBuilderGeneral.get(method).append(CSCLConstants.CSV_DELIM);
                        hmRowBuilderGeneral.get(method).append(CSCLConstants.CSV_DELIM);
                    }
                }
                hmRowBuilderGeneral.get(method).append('\n');

                hmRowBuilderGeneral.get(method).append("stdev").append(CSCLConstants.CSV_DELIM).append(CSCLConstants.CSV_DELIM).append(CSCLConstants.CSV_DELIM);
                for (Integer windowSize : windowSizes) {
                    for (SimilarityFormula formula : formulas) {
                        hmRowBuilderGeneral.get(method).append(CSCLConstants.CSV_DELIM);
                        hmRowBuilderGeneral.get(method).append(CSCLConstants.CSV_DELIM);
                    }
                }
                for (Integer timeFrame : timeFrames) {
                    for (SimilarityFormula formula : formulas) {
                        hmRowBuilderGeneral.get(method).append(CSCLConstants.CSV_DELIM);
                        hmRowBuilderGeneral.get(method).append(CSCLConstants.CSV_DELIM);
                    }
                }
                hmRowBuilderGeneral.get(method).append('\n');

                hmRowBuilderGeneral.get(method).append("max").append(CSCLConstants.CSV_DELIM).append(CSCLConstants.CSV_DELIM).append(CSCLConstants.CSV_DELIM);
                for (Integer windowSize : windowSizes) {
                    for (SimilarityFormula formula : formulas) {
                        hmRowBuilderGeneral.get(method).append(CSCLConstants.CSV_DELIM);
                        hmRowBuilderGeneral.get(method).append(CSCLConstants.CSV_DELIM);
                    }
                }
                for (Integer timeFrame : timeFrames) {
                    for (SimilarityFormula formula : formulas) {
                        hmRowBuilderGeneral.get(method).append(CSCLConstants.CSV_DELIM);
                        hmRowBuilderGeneral.get(method).append(CSCLConstants.CSV_DELIM);
                    }
                }
                hmRowBuilderGeneral.get(method).append('\n');

                hmRowBuilderGeneral.get(method).append("min").append(CSCLConstants.CSV_DELIM).append(CSCLConstants.CSV_DELIM).append(CSCLConstants.CSV_DELIM);
                for (Integer windowSize : windowSizes) {
                    for (SimilarityFormula formula : formulas) {
                        hmRowBuilderGeneral.get(method).append(CSCLConstants.CSV_DELIM);
                        hmRowBuilderGeneral.get(method).append(CSCLConstants.CSV_DELIM);
                    }
                }
                for (Integer timeFrame : timeFrames) {
                    for (SimilarityFormula formula : formulas) {
                        hmRowBuilderGeneral.get(method).append(CSCLConstants.CSV_DELIM);
                        hmRowBuilderGeneral.get(method).append(CSCLConstants.CSV_DELIM);
                    }
                }
                hmRowBuilderGeneral.get(method).append('\n');
                FileUtils.writeStringToFile(filesGeneral.get(method), hmRowBuilderGeneral.get(method).toString(), "UTF-8", true);
            }
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "Exception: {0}", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Exception: {0}", e.getMessage());
            e.printStackTrace();
        }
        LOGGER.log(Level.INFO, "Finished processing for the folder: {0}", path);
    }

    private String capTabelStats(List<SimilarityFormula> formulas) {
        StringBuilder capTabel = new StringBuilder();
        capTabel.append("chat").append(CSCLConstants.CSV_DELIM);
        capTabel.append("contributuions").append(CSCLConstants.CSV_DELIM);
        capTabel.append("explicit links").append(CSCLConstants.CSV_DELIM);
        for (Integer windowSize : windowSizes) {
            for (SimilarityFormula formula : formulas) {
                capTabel.append("ref_no_distance_").append(formula.getAcronym()).append("_").append(windowSize).append(CSCLConstants.CSV_DELIM);
                capTabel.append("ref_percentage_distance_").append(formula.getAcronym()).append("_").append(windowSize).append(CSCLConstants.CSV_DELIM);
            }
        }
        for (Integer windowSize : windowSizes) {
            for (SimilarityFormula formula : formulas) {
                capTabel.append("block_no_distance_").append(formula.getAcronym()).append("_").append(windowSize).append(CSCLConstants.CSV_DELIM);
                capTabel.append("block_percentage_distance_").append(formula.getAcronym()).append("_").append(windowSize).append(CSCLConstants.CSV_DELIM);
            }
        }
        for (Integer timeFrame : timeFrames) {
            for (SimilarityFormula formula : formulas) {
                capTabel.append("ref_no_time_").append(formula.getAcronym()).append("_").append(timeFrame).append(CSCLConstants.CSV_DELIM);
                capTabel.append("ref_percentage_time_").append(formula.getAcronym()).append("_").append(timeFrame).append(CSCLConstants.CSV_DELIM);
            }
        }
        for (Integer timeFrame : timeFrames) {
            for (SimilarityFormula formula : formulas) {
                capTabel.append("block_no_time_").append(formula.getAcronym()).append("_").append(timeFrame).append(CSCLConstants.CSV_DELIM);
                capTabel.append("block_percentage_time").append(formula.getAcronym()).append("_").append(timeFrame).append(CSCLConstants.CSV_DELIM);
            }
        }
        capTabel.append('\n');
        return capTabel.toString();
    }

    /**
     *
     * @param args
     */
    public static void main(String args[]) {
        ReaderBenchServer.initializeDB();

        // TASA_LAK was used before!
        LSA lsa = LSA.loadLSA(CSCLConstants.LSA_PATH, Lang.en);
        LDA lda = LDA.loadLDA(CSCLConstants.LDA_PATH, Lang.en);

        List<Integer> windowSizes = new ArrayList<>();
        windowSizes.add(20);
        windowSizes.add(10);
        windowSizes.add(5);
        windowSizes.add(3);

        List<Integer> timeFrames = new ArrayList<>(); // in seconds
        timeFrames.add(30);
        timeFrames.add(1 * 60);
        timeFrames.add(2 * 60);
        timeFrames.add(3 * 60);
        timeFrames.add(5 * 60);
        //timeFrames.add(Integer.MAX_VALUE); // delete to limit number of previous contributions tested

        SimilarityAnalysis corpusSample = new SimilarityAnalysis(
                CSCLConstants.CSCL_CORPUS,
                Lang.getLang("English"),
                CSCLConstants.USE_POSTAGGING,
                CSCLConstants.DIALOGISM,
                windowSizes,
                timeFrames,
                lsa,
                lda);
        corpusSample.process();
    }
}
