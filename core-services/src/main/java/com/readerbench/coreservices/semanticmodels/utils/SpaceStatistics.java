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
package com.readerbench.coreservices.semanticmodels.utils;

import com.readerbench.coreservices.nlp.parsing.Parsing;
import com.readerbench.coreservices.data.Word;
import com.readerbench.coreservices.semanticmodels.SemanticModel;
import com.readerbench.datasourceprovider.commons.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class SpaceStatistics {

    private static final double MINIMUM_IMPOSED_THRESHOLD = 0.3d;

    private static final Logger LOGGER = LoggerFactory.getLogger(SpaceStatistics.class);

    private final SemanticModel semModel;
    private final int noWords;
    private List<WordPairSimilarity> relevantWordPairList;
    private WordSimilarityContainer wordSimilarityContainer;

    public SpaceStatistics(SemanticModel semModel) {
        LOGGER.info("Loading " + semModel.getName() + "...");
        this.semModel = semModel;
        this.noWords = semModel.getWordSet().size();
    }

    public void buildWordDistances() {
        LOGGER.info("No. words in semantic model dictionary:\t" + noWords);
        double sim;
        double s00 = 0, s10 = 0, s20 = 0;
        double s01 = 0, s11 = 0, s21 = 0;
        List<WordPairSimilarity> allWordSimilarityPairList = new ArrayList<>();
        for (Map.Entry<Word, double[]> e1 : semModel.getWordRepresentations().entrySet()) {
            for (Map.Entry<Word, double[]> e2 : semModel.getWordRepresentations().entrySet()) {
                if (e1.getKey().getLemma().compareTo(e2.getKey().getLemma()) > 0) {
                    sim = semModel.getSimilarity(e1.getValue(), e2.getValue());
                    s00++;
                    s10 += sim;
                    s20 += Math.pow(sim, 2);
                    if (sim >= MINIMUM_IMPOSED_THRESHOLD) {
                        WordPairSimilarity pairSimilarity = new WordPairSimilarity(e1.getKey().getLemma(), e2.getKey().getLemma(), sim);
                        allWordSimilarityPairList.add(pairSimilarity);
                        s01++;
                        s11 += sim;
                        s21 += Math.pow(sim, 2);
                    }
                }
            }
        }
        double avg = -1, stdev = -1;
        if (s00 != 0) {
            avg = s10 / s00;
            stdev = Math.sqrt(s00 * s20 - Math.pow(s10, 2)) / s00;
        }
        LOGGER.info("No. potential word associations:\t" + Formatting.formatNumber(s00));
        LOGGER.info("Average similarity for all word associations:\t" + Formatting.formatNumber(avg));
        LOGGER.info("Stdev similarity for all word associations:\t" + Formatting.formatNumber(stdev));

        avg = -1;
        stdev = -1;
        if (s01 != 0) {
            avg = s11 / s01;
            stdev = Math.sqrt(s01 * s21 - Math.pow(s11, 2)) / s01;
        }
        LOGGER.info("No. word associations (above minimum threshold):\t" + Formatting.formatNumber(s01));
        LOGGER.info("Average similarity for all word associations (above minimum threshold):\t" + Formatting.formatNumber(avg));
        LOGGER.info("Stdev similarity for all word associations (above minimum threshold):\t" + Formatting.formatNumber(stdev));

        // add only significant edges
        double threshold = avg - stdev;
        double s02 = 0, s12 = 0, s22 = 0;

        this.relevantWordPairList = new ArrayList<>();
        this.wordSimilarityContainer = new WordSimilarityContainer();

        for (WordPairSimilarity pair : allWordSimilarityPairList) {
            if (pair.getSimilarity() >= threshold) {
                relevantWordPairList.add(pair);
                wordSimilarityContainer.indexDistance(pair.getWord1(), pair.getWord2(), pair.getSimilarity());
                s02++;
                s12 += pair.getSimilarity();
                s22 += Math.pow(pair.getSimilarity(), 2);
            }
        }
        avg = -1;
        stdev = -1;
        if (s02 != 0) {
            avg = s12 / s02;
            stdev = Math.sqrt(s02 * s22 - Math.pow(s12, 2)) / s02;
        }
        LOGGER.info("No significant word associations (above avg-stdev):\t" + Formatting.formatNumber(s02));
        LOGGER.info("Average similarity for significant word associations (above avg-stdev):\t" + Formatting.formatNumber(avg));
        LOGGER.info("Stdev similarity for significant word associations (above avg-stdev):\t" + Formatting.formatNumber(stdev));
    }

    public WordSimilarityContainer getWordSimilarityContainer() {
        buildWordDistances();
        return this.wordSimilarityContainer;
    }

    public SemanticModel getSemModel() {
        return semModel;
    }

    /**
     * Compares all pairs of concepts from the baseline to all subsequent
     * corpora
     *
     * The first space is the baseline
     *
     * @param pathToOutput
     * @param corpora
     */
    public static void compareSpaces(String pathToOutput, List<SpaceStatistics> corpora) {
        corpora.get(0).buildWordDistances();
        LOGGER.info("Writing comparisons based on baseline corpus ...");
        File output = new File(pathToOutput);

        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"),
                32768)) {
            out.write("Word 1, Word 2");
            for (SpaceStatistics space : corpora) {
                out.write("," + space.getSemModel().getName());
            }

            for (WordPairSimilarity c : corpora.get(0).relevantWordPairList) {
                if (c.getSimilarity() > 0) {
                    String outputString = "\n" + c.getWord1() + "," + c.getWord2();
                    boolean viableEntry = true;
                    for (SpaceStatistics space : corpora) {
                        double similarity = space.getSemModel().getSimilarity(
                                Parsing.getWordFromConcept(c.getWord1(), space.getSemModel().getLanguage()),
                                Parsing.getWordFromConcept(c.getWord2(), space.getSemModel().getLanguage()));
                        if (similarity > 0) {
                            outputString += "," + similarity;
                        } else {
                            viableEntry = false;
                            break;
                        }
                    }
                    if (viableEntry) {
                        out.write(outputString);
                    }
                }
            }
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    /**
     * Determine word linkage power corpora
     *
     * The first space is the baseline
     *
     */
    private enum SNAIndices {
        SIM_DEGREE
    }

    public static void determineWLP(String pathToOutput, SpaceStatistics space, SemanticModel referenceSpace) {
        LOGGER.info("Writing word linkage power statistics based on baseline corpus ...");
        File output = new File(pathToOutput);

        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"),
                32768)) {
            out.write("Word");
            for (SNAIndices index : SNAIndices.values()) {
                out.write("," + index.name() + "(" + space.getSemModel().getName() + ")");
            }
            for (SNAIndices index : SNAIndices.values()) {
                out.write(",NORM_" + index.name() + "(" + space.getSemModel().getName() + ")");
            }

            Map<String, EnumMap<SNAIndices, Double>> wlps = new TreeMap<>();

            space.getSemModel().getWordSet().stream().forEach((w) -> {
                EnumMap<SNAIndices, Double> scores = new EnumMap<>(SNAIndices.class);
                scores.put(SNAIndices.SIM_DEGREE, 0d);
                wlps.put(w.getLemma(), scores);
            });

            space.relevantWordPairList.stream().map((c) -> {
                wlps.get(c.getWord1()).put(SNAIndices.SIM_DEGREE,
                        wlps.get(c.getWord1()).get(SNAIndices.SIM_DEGREE) + c.getSimilarity());
                return c;
            }).forEach((c) -> {
                wlps.get(c.getWord2()).put(SNAIndices.SIM_DEGREE,
                        wlps.get(c.getWord2()).get(SNAIndices.SIM_DEGREE) + c.getSimilarity());
            });

            LOGGER.info("Writing final results...");
            // print results
            for (Word w : referenceSpace.getWordSet()) {
                out.write("\n" + w.getLemma());
                if (wlps.containsKey(w.getLemma())) {
                    for (SNAIndices index : SNAIndices.values()) {
                        out.write("," + wlps.get(w.getLemma()).get(index));
                    }
                    for (SNAIndices index : SNAIndices.values()) {
                        out.write("," + wlps.get(w.getLemma()).get(index) / space.getSemModel().getWordSet().size());
                    }
                } else {
                    out.write(",,,,");
                }
            }
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }

    }
}
