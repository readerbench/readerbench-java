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
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.SimilarityType;
import services.semanticModels.word2vec.Word2VecModel;
import webService.ReaderBenchServer;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class SimilarityAnalysisCombined {

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
    private final Word2VecModel word2Vec;

    private final Map<Integer, Map<Integer, Map<SimilarityFormula, Map<SimilarityType, Integer>>>> totalSimDetected;
    private final Map<Integer, Map<Integer, Map<SimilarityFormula, Map<SimilarityType, Double>>>> percentageSimDetected;
    private final Map<Integer, Map<Integer, Map<SimilarityFormula, Map<SimilarityType, Integer>>>> totalSimInBlock;
    private final Map<Integer, Map<Integer, Map<SimilarityFormula, Map<SimilarityType, Double>>>> percentageSimInBlock;
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
     * @param word2Vec
     */
    public SimilarityAnalysisCombined(String path, Lang lang,
            boolean usePOSTagging, boolean computeDialogism,
            List<Integer> windowSizes, List<Integer> timeFrames,
            LSA lsa, LDA lda, Word2VecModel word2Vec) {
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
        this.word2Vec = word2Vec;

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
        capTabel.append("utt_text").append(CSCLConstants.CSV_DELIM); // contribution text
        capTabel.append("utt_time").append(CSCLConstants.CSV_DELIM); // contribution time
        capTabel.append("ref_id").append(CSCLConstants.CSV_DELIM); // reference id
        capTabel.append("ref_participant").append(CSCLConstants.CSV_DELIM); // participant
        capTabel.append("ref_text").append(CSCLConstants.CSV_DELIM); // reference text
        capTabel.append("ref_time").append(CSCLConstants.CSV_DELIM); // reference time
        capTabel.append("utt_ref_time_diff").append(CSCLConstants.CSV_DELIM); // (contribution, reference) time diff

        // reference within window sizes (in terms of distance)
        for (Integer windowSize : windowSizes) {
            for (Integer timeFrame : timeFrames) {
                for (SimilarityFormula formula : formulas) {
                    capTabel.append("max_sim_").append(formula.getAcronym()).append("_distance_").append(windowSize).append("_time_").append(timeFrame).append(CSCLConstants.CSV_DELIM); // max sim
                    capTabel.append("max_sim_id_").append(formula.getAcronym()).append("_distance_").append(windowSize).append("_time_").append(timeFrame).append(CSCLConstants.CSV_DELIM); // id of max sim utt
                    capTabel.append("max_sim_participant_").append(formula.getAcronym()).append("_distance_").append(windowSize).append("_time_").append(timeFrame).append(CSCLConstants.CSV_DELIM); // participant of max sim utt
                    capTabel.append("max_sim_ref_detected_").append(formula.getAcronym()).append("_distance_").append(windowSize).append("_time_").append(timeFrame).append(CSCLConstants.CSV_DELIM); // ref detected
                    capTabel.append("max_sim_ref_in_block_").append(formula.getAcronym()).append("_distance_").append(windowSize).append("_time_").append(timeFrame).append(CSCLConstants.CSV_DELIM); // ref in block
                }
            }
        }
        capTabel.append('\n');
        return capTabel.toString();
    }

    /**
     *
     */
    public void process() {
        List<SimilarityType> methods = new ArrayList();
        methods.add(SimilarityType.LSA);
        methods.add(SimilarityType.LDA);
        methods.add(SimilarityType.LEACOCK_CHODOROW);
        methods.add(SimilarityType.WU_PALMER);
        methods.add(SimilarityType.PATH_SIM);
        methods.add(SimilarityType.WORD2VEC);

        List<SimilarityFormula> formulas = new ArrayList();
        formulas.add(SimilarityFormula.READERBENCH_SIM);
        formulas.add(SimilarityFormula.NORMALIZED_SIM);
        formulas.add(SimilarityFormula.MIHALCEA_SIM);

        for (Integer windowSize : windowSizes) {
            totalSimDetected.put(windowSize, new HashMap<>());
            percentageSimDetected.put(windowSize, new HashMap<>());
            totalSimInBlock.put(windowSize, new HashMap<>());
            percentageSimInBlock.put(windowSize, new HashMap<>());
            for (Integer timeFrame : timeFrames) {
                totalSimDetected.get(windowSize).put(timeFrame, new HashMap<>());
                percentageSimDetected.get(windowSize).put(timeFrame, new HashMap<>());
                totalSimInBlock.get(windowSize).put(timeFrame, new HashMap<>());
                percentageSimInBlock.get(windowSize).put(timeFrame, new HashMap<>());
                for (SimilarityFormula formula : formulas) {
                    totalSimDetected.get(windowSize).get(timeFrame).put(formula, new HashMap<>());
                    percentageSimDetected.get(windowSize).get(timeFrame).put(formula, new HashMap<>());
                    totalSimInBlock.get(windowSize).get(timeFrame).put(formula, new HashMap<>());
                    percentageSimInBlock.get(windowSize).get(timeFrame).put(formula, new HashMap<>());
                    for (SimilarityType method : methods) {
                        totalSimDetected.get(windowSize).get(timeFrame).get(formula).put(method, 0);
                        percentageSimDetected.get(windowSize).get(timeFrame).get(formula).put(method, 0.0);
                        totalSimInBlock.get(windowSize).get(timeFrame).get(formula).put(method, 0);
                        percentageSimInBlock.get(windowSize).get(timeFrame).get(formula).put(method, 0.0);

                    }
                }
            }
        }

        LOGGER.info("Starting conversation processing...");
        try {
            Map<SimilarityType, File> files = new HashMap<>();
            for (SimilarityType method : methods) {
                files.put(method, new File(path + "/similarity_scores_chosen_" + method.getAcronym() + ".csv"));
                try {
                    FileUtils.writeStringToFile(files.get(method), capTabel(formulas), "UTF-8");
                } catch (IOException e) {
                    LOGGER.log(Level.INFO, "Exception: {0}", e.getMessage());
                    e.printStackTrace();
                }
            }

            Map<SimilarityType, File> filesGeneral = new HashMap<>();
            for (SimilarityType method : methods) {
                filesGeneral.put(method, new File(path + "/similarity_stats_chosen_" + method.getAcronym() + ".csv"));
                try {
                    FileUtils.writeStringToFile(filesGeneral.get(method), capTabelStats(formulas), "UTF-8");
                } catch (IOException e) {
                    LOGGER.log(Level.INFO, "Exception: {0}", e.getMessage());
                    e.printStackTrace();
                }
            }

            Map<SimilarityType, String> modelPaths = new EnumMap<>(SimilarityType.class);
            modelPaths.put(SimilarityType.LSA, lsa.getPath());
            modelPaths.put(SimilarityType.LDA, lda.getPath());
            modelPaths.put(SimilarityType.WORD2VEC, word2Vec.getPath());

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

                    Map<Integer, Map<Integer, Map<SimilarityFormula, Map<SimilarityType, Integer>>>> simDetected = new HashMap<>();
                    Map<Integer, Map<Integer, Map<SimilarityFormula, Map<SimilarityType, Integer>>>> simInBlock = new HashMap<>();
                    for (Integer windowSize : windowSizes) {
                        simDetected.put(windowSize, new HashMap<>());
                        simInBlock.put(windowSize, new HashMap<>());
                        for (Integer timeFrame : timeFrames) {
                            simDetected.get(windowSize).put(timeFrame, new HashMap<>());
                            simInBlock.get(windowSize).put(timeFrame, new HashMap<>());
                            for (SimilarityFormula formula : formulas) {
                                simDetected.get(windowSize).get(timeFrame).put(formula, new HashMap<>());
                                simInBlock.get(windowSize).get(timeFrame).put(formula, new HashMap<>());
                                for (SimilarityType method : methods) {
                                    simDetected.get(windowSize).get(timeFrame).get(formula).put(method, 0);
                                    simInBlock.get(windowSize).get(timeFrame).get(formula).put(method, 0);
                                }
                            }
                        }
                    }

                    LOGGER.log(Level.INFO, "Processing chat {0}", filePath.getFileName());

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

                            Map<Integer, Map<Integer, Map<SimilarityFormula, Map<SimilarityType, Double>>>> max = new HashMap<>();
                            Map<Integer, Map<Integer, Map<SimilarityFormula, Map<SimilarityType, Integer>>>> refMax = new HashMap<>();
                            Map<Integer, Map<Integer, Map<SimilarityFormula, Map<SimilarityType, String>>>> participantMax = new HashMap<>();
                            // distance, time, formula (RB, norm, Mih), sim (WN, LSA, etc.), value

                            for (Integer windowSize : windowSizes) {
                                max.put(windowSize, new HashMap<>());
                                refMax.put(windowSize, new HashMap<>());
                                participantMax.put(windowSize, new HashMap<>());
                                for (Integer timeFrame : timeFrames) {
                                    max.get(windowSize).put(timeFrame, new HashMap());
                                    refMax.get(windowSize).put(timeFrame, new HashMap());
                                    participantMax.get(windowSize).put(timeFrame, new HashMap());
                                    for (SimilarityFormula formula : formulas) {
                                        max.get(windowSize).get(timeFrame).put(formula, new HashMap());
                                        refMax.get(windowSize).get(timeFrame).put(formula, new HashMap());
                                        participantMax.get(windowSize).get(timeFrame).put(formula, new HashMap());
                                        for (SimilarityType method : methods) {
                                            max.get(windowSize).get(timeFrame).get(formula).put(method, -1.0);
                                            refMax.get(windowSize).get(timeFrame).get(formula).put(method, -1);
                                            participantMax.get(windowSize).get(timeFrame).get(formula).put(method, null);

                                        }
                                    }
                                }
                            }

                            // windowSize
                            for (int j = i - 1; j >= i - maxWindowSize && j > 0; j--) {
                                secondUtt = (Utterance) c.getBlocks().get(j);
                                if (secondUtt != null) {
                                    // break if time between the two utterances is higher then the time frames
                                    if (firstUtt.getTime().after(secondUtt.getTime())) {
                                        DateUtils.addHours(secondUtt.getTime(), 24);
                                        //LOGGER.log(Level.INFO, "(Updated) First utt time: {0}; second utt time: {1}", new Object[]{firstUtt.getTime(), secondUtt.getTime()});
                                    }
                                    int diffTimpUttRef = (int) TimeHelper.getDateDiff(secondUtt.getTime(), firstUtt.getTime(), TimeUnit.SECONDS);
                                    // ignore if above max time frame
                                    if (diffTimpUttRef > maxTimeFrame) {
                                        break;
                                    }
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
                                                        sim = MihalceaSimilarity.compute(firstUtt, secondUtt, method, lsa, lda, word2Vec);
                                                        break;
                                                    default:
                                                        sim = -1;
                                                        break;
                                                }
                                            }
                                            for (Integer windowSize : windowSizes) {
                                                for (Integer timeFrame : timeFrames) {
                                                    if (distance <= windowSize && max.get(windowSize).get(timeFrame).get(formula).get(method) < sim) {
                                                        max.get(windowSize).get(timeFrame).get(formula).put(method, sim);
                                                        refMax.get(windowSize).get(timeFrame).get(formula).put(method, secondUtt.getIndex());
                                                        participantMax.get(windowSize).get(timeFrame).get(formula).put(method, secondUtt.getParticipant().getName());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    kDistance++;
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
                                for (Integer timeFrame : timeFrames) {
                                    if (refId != -1) {
                                        for (SimilarityFormula formula : formulas) {
                                            for (SimilarityType method : methods) {
                                                if (refId == refMax.get(windowSize).get(timeFrame).get(formula).get(method)) {
                                                    simDetected.get(windowSize).get(timeFrame).get(formula).put(method, simDetected.get(windowSize).get(timeFrame).get(formula).get(method) + 1);
                                                }
                                            }
                                        }
                                    }
                                    for (SimilarityFormula formula : formulas) {
                                        for (SimilarityType method : methods) {
                                            // max sim
                                            hmRowBuilder.get(method).append(max.get(windowSize).get(timeFrame).get(formula).get(method)).append(CSCLConstants.CSV_DELIM);
                                            // id of max sim
                                            hmRowBuilder.get(method).append(refMax.get(windowSize).get(timeFrame).get(formula).get(method)).append(CSCLConstants.CSV_DELIM);
                                            // max sim participant name
                                            hmRowBuilder.get(method).append((participantMax.get(windowSize).get(timeFrame).get(formula).get(method) != null) ? participantMax.get(windowSize).get(timeFrame).get(formula).get(method) : "").append(CSCLConstants.CSV_DELIM);
                                            // ref detected?                                    
                                            hmRowBuilder.get(method).append(((refId != -1 && refId == refMax.get(windowSize).get(timeFrame).get(formula).get(method)) ? 1 : 0)).append(CSCLConstants.CSV_DELIM);
                                            // ref in block?
                                            Integer minRef = (int) Math.min(refId, refMax.get(windowSize).get(timeFrame).get(formula).get(method));
                                            Integer maxRef = (int) Math.max(refId, refMax.get(windowSize).get(timeFrame).get(formula).get(method));
                                            boolean isInBlock = SimilarityHelper.isInBlock(c, minRef, maxRef);
                                            if (isInBlock) {
                                                simInBlock.get(windowSize).get(timeFrame).get(formula).put(method, simInBlock.get(windowSize).get(timeFrame).get(formula).get(method) + 1);
                                            }
                                            hmRowBuilder.get(method).append((isInBlock) ? 1 : 0).append(CSCLConstants.CSV_DELIM);
                                        }
                                    }
                                }
                            }

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
                                            .append(CSCLConstants.CSV_DELIM)
                                            .append(CSCLConstants.CSV_DELIM)
                                            .append(CSCLConstants.CSV_DELIM)
                                            .append(CSCLConstants.CSV_DELIM);
                                    for (Integer windowSize : windowSizes) {
                                        for (Integer timeFrame : timeFrames) {
                                            for (SimilarityFormula formula : formulas) {
                                                hmRowBuilder.get(method).append(CSCLConstants.CSV_DELIM)
                                                        .append(CSCLConstants.CSV_DELIM)
                                                        .append(CSCLConstants.CSV_DELIM);
                                                hmRowBuilder.get(method).append(simDetected.get(windowSize).get(timeFrame).get(formula).get(method)).append(CSCLConstants.CSV_DELIM);
                                                hmRowBuilder.get(method).append(simInBlock.get(windowSize).get(timeFrame).get(formula).get(method)).append(CSCLConstants.CSV_DELIM);
                                                totalSimDetected.get(windowSize).get(timeFrame).get(formula).put(method, totalSimDetected.get(windowSize).get(timeFrame).get(formula).get(method) + simDetected.get(windowSize).get(timeFrame).get(formula).get(method));
                                                percentageSimDetected.get(windowSize).get(timeFrame).get(formula).put(method, (explicitLinks > 0) ? (totalSimDetected.get(windowSize).get(timeFrame).get(formula).get(method) * 1.0 / explicitLinks) : 0);
                                                totalSimInBlock.get(windowSize).get(timeFrame).get(formula).put(method, totalSimInBlock.get(windowSize).get(timeFrame).get(formula).get(method) + simInBlock.get(windowSize).get(timeFrame).get(formula).get(method));
                                                percentageSimInBlock.get(windowSize).get(timeFrame).get(formula).put(method, (explicitLinks > 0) ? (totalSimInBlock.get(windowSize).get(timeFrame).get(formula).get(method) * 1.0 / explicitLinks) : 0);
                                            }
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
                            for (Integer timeFrame : timeFrames) {
                                for (SimilarityFormula formula : formulas) {
                                    hmRowBuilderGeneral.get(method).append(simDetected.get(windowSize).get(timeFrame).get(formula).get(method)).append(CSCLConstants.CSV_DELIM);
                                    hmRowBuilderGeneral.get(method).append(explicitLinks > 0 ? simDetected.get(windowSize).get(timeFrame).get(formula).get(method) * 1.0 / explicitLinks : 0).append(CSCLConstants.CSV_DELIM);
                                }
                            }
                        }
                        for (Integer windowSize : windowSizes) {
                            for (Integer timeFrame : timeFrames) {
                                for (SimilarityFormula formula : formulas) {
                                    hmRowBuilderGeneral.get(method).append(simInBlock.get(windowSize).get(timeFrame).get(formula).get(method)).append(CSCLConstants.CSV_DELIM);
                                    hmRowBuilderGeneral.get(method).append(explicitLinks > 0 ? simInBlock.get(windowSize).get(timeFrame).get(formula).get(method) * 1.0 / explicitLinks : 0).append(CSCLConstants.CSV_DELIM);
                                }
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
                        .append(CSCLConstants.CSV_DELIM)
                        .append(CSCLConstants.CSV_DELIM)
                        .append(CSCLConstants.CSV_DELIM)
                        .append(CSCLConstants.CSV_DELIM)
                        .append(CSCLConstants.CSV_DELIM);
                for (Integer windowSize : windowSizes) {
                    for (Integer timeFrame : timeFrames) {
                        for (SimilarityFormula formula : formulas) {
                            hmRowBuilder.get(method).append(CSCLConstants.CSV_DELIM)
                                    .append(CSCLConstants.CSV_DELIM)
                                    .append(CSCLConstants.CSV_DELIM);
                            hmRowBuilder.get(method).append(totalSimDetected.get(windowSize).get(timeFrame).get(formula).get(method)).append(CSCLConstants.CSV_DELIM);
                            hmRowBuilder.get(method).append(totalSimInBlock.get(windowSize).get(timeFrame).get(formula).get(method)).append(CSCLConstants.CSV_DELIM);
                        }
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
                    for (Integer timeFrame : timeFrames) {
                        for (SimilarityFormula formula : formulas) {
                            hmRowBuilderGeneral.get(method).append(totalSimDetected.get(windowSize).get(timeFrame).get(formula).get(method)).append(CSCLConstants.CSV_DELIM);
                            hmRowBuilderGeneral.get(method).append(percentageSimDetected.get(windowSize).get(timeFrame).get(formula).get(method)).append(CSCLConstants.CSV_DELIM);
                        }
                    }
                }
                for (Integer windowSize : windowSizes) {
                    for (Integer timeFrame : timeFrames) {
                        for (SimilarityFormula formula : formulas) {
                            hmRowBuilderGeneral.get(method).append(totalSimInBlock.get(windowSize).get(timeFrame).get(formula).get(method)).append(CSCLConstants.CSV_DELIM);
                            hmRowBuilderGeneral.get(method).append(percentageSimInBlock.get(windowSize).get(timeFrame).get(formula).get(method)).append(CSCLConstants.CSV_DELIM);
                        }
                    }
                }
                hmRowBuilderGeneral.get(method).append('\n');

                hmRowBuilderGeneral.get(method).append("avg").append(CSCLConstants.CSV_DELIM).append(CSCLConstants.CSV_DELIM).append(CSCLConstants.CSV_DELIM);
                for (Integer windowSize : windowSizes) {
                    for (Integer timeFrame : timeFrames) {
                        for (SimilarityFormula formula : formulas) {
                            hmRowBuilderGeneral.get(method).append(CSCLConstants.CSV_DELIM);
                            hmRowBuilderGeneral.get(method).append(CSCLConstants.CSV_DELIM);
                        }
                    }
                }
                hmRowBuilderGeneral.get(method).append('\n');

                hmRowBuilderGeneral.get(method).append("stdev").append(CSCLConstants.CSV_DELIM).append(CSCLConstants.CSV_DELIM).append(CSCLConstants.CSV_DELIM);
                for (Integer windowSize : windowSizes) {
                    for (Integer timeFrame : timeFrames) {
                        for (SimilarityFormula formula : formulas) {
                            hmRowBuilderGeneral.get(method).append(CSCLConstants.CSV_DELIM);
                            hmRowBuilderGeneral.get(method).append(CSCLConstants.CSV_DELIM);
                        }
                    }
                }
                hmRowBuilderGeneral.get(method).append('\n');

                hmRowBuilderGeneral.get(method).append("max").append(CSCLConstants.CSV_DELIM).append(CSCLConstants.CSV_DELIM).append(CSCLConstants.CSV_DELIM);
                for (Integer windowSize : windowSizes) {
                    for (Integer timeFrame : timeFrames) {
                        for (SimilarityFormula formula : formulas) {
                            hmRowBuilderGeneral.get(method).append(CSCLConstants.CSV_DELIM);
                            hmRowBuilderGeneral.get(method).append(CSCLConstants.CSV_DELIM);
                        }
                    }
                }
                hmRowBuilderGeneral.get(method).append('\n');

                hmRowBuilderGeneral.get(method).append("min").append(CSCLConstants.CSV_DELIM).append(CSCLConstants.CSV_DELIM).append(CSCLConstants.CSV_DELIM);
                for (Integer windowSize : windowSizes) {
                    for (Integer timeFrame : timeFrames) {
                        for (SimilarityFormula formula : formulas) {
                            hmRowBuilderGeneral.get(method).append(CSCLConstants.CSV_DELIM);
                            hmRowBuilderGeneral.get(method).append(CSCLConstants.CSV_DELIM);
                        }
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
            for (Integer timeFrame : timeFrames) {
                for (SimilarityFormula formula : formulas) {
                    capTabel.append("ref_no_distance_").append(formula.getAcronym()).append("_").append(windowSize).append("_").append(timeFrame).append(CSCLConstants.CSV_DELIM);
                    capTabel.append("ref_percentage_distance_").append(formula.getAcronym()).append("_").append(windowSize).append("_").append(timeFrame).append(CSCLConstants.CSV_DELIM);
                }
            }
        }
        for (Integer windowSize : windowSizes) {
            for (Integer timeFrame : timeFrames) {
                for (SimilarityFormula formula : formulas) {
                    capTabel.append("block_no_distance_").append(formula.getAcronym()).append("_").append(windowSize).append("_").append(timeFrame).append(CSCLConstants.CSV_DELIM);
                    capTabel.append("block_percentage_distance_").append(formula.getAcronym()).append("_").append(windowSize).append("_").append(timeFrame).append(CSCLConstants.CSV_DELIM);
                }
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
        Word2VecModel word2Vec = Word2VecModel.loadWord2Vec(CSCLConstants.WORD2VEC_PATH, Lang.en);

        List<Integer> windowSizes = new ArrayList<>();
        windowSizes.add(10);
        windowSizes.add(5);

        List<Integer> timeFrames = new ArrayList<>(); // in seconds
        timeFrames.add(1 * 60);
        timeFrames.add(2 * 60);

        SimilarityAnalysisCombined corpusSample = new SimilarityAnalysisCombined(
                CSCLConstants.CSCL_CORPUS,
                Lang.getLang("English"),
                CSCLConstants.USE_POSTAGGING,
                CSCLConstants.DIALOGISM,
                windowSizes,
                timeFrames,
                lsa,
                lda,
                word2Vec);
        corpusSample.process();
    }
}
