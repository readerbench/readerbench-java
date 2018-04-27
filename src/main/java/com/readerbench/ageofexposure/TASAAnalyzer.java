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
package com.readerbench.ageofexposure;

import com.readerbench.datasourceprovider.data.Word;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.datasourceprovider.commons.Formatting;
import com.readerbench.coreservices.commons.VectorAlgebra;
import com.readerbench.coreservices.semanticmodels.lda.LDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class TASAAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TASAAnalyzer.class);

    private final double MIN_SEM_SIMILARITY = 0.3;
    private final double MIN_THRESHOLD = 0.4;
    private final double MAX_THRESHOLD = 0.7;
    private final double THRESHOLD_INCREMENT = 0.1;

    private final String path;
    private final int noThreads;
    private Map<Integer, LDA> models;
    private LDA matureModel;
    private int noGrades;
    private Map<Word, List<Double>> AoEEvolution;

    public TASAAnalyzer(String path, int noThreads) {
        super();
        this.path = path;
        this.noThreads = noThreads;
    }

    public void loadModels() {
        noGrades = (new File(path)).listFiles((File pathname) -> pathname.isDirectory()).length;

        /* Load LDA Models */
        models = new TreeMap<>();
        for (int i = 0; i < noGrades; i++) {
            String classPath = path + "/grade" + i;
            LOGGER.info("Loading model {}...", classPath);
            models.put(i, LDA.loadLDA(classPath, Lang.en));
            LOGGER.info("Loaded model with {} topics.", models.get(i).getNoDimensions());
        }
    }

    public Integer[] computeMatchTask(LDA modelA, LDA modelB) {
        LOGGER.info("Matching {} to {}...", new Object[]{modelA.getPath(), modelB.getPath()});

        /* Find best topic matches */
        TopicMatchGraph graph = new TopicMatchGraph(modelA.getNoDimensions() + modelB.getNoDimensions());
        int i, j, x, y;
        double distance;

        for (i = 0; i < modelA.getNoDimensions(); i++) {
            for (j = 0; j < modelB.getNoDimensions(); j++) {
                distance = LDASupport.topicDistance(modelA, i, modelB, j);
                if (distance <= 1 - MIN_SEM_SIMILARITY) {
                    x = i;
                    y = j + modelA.getNoDimensions();
                    graph.addEdge(x, y, distance);
                }
            }
        }
        /* Compute Matches */
        Integer[] matches = graph.computeAssociations(modelA.getNoDimensions());
        for (i = 0; i < matches.length; i++) {
            if (matches[i] != null) {
                matches[i] -= modelA.getNoDimensions();
            }
        }

        return matches;
    }

    public void performMatching() throws InterruptedException, ExecutionException {
        /* Load "Mature" Model = Last complexity level */
        matureModel = models.get(noGrades - 1);
        LDA intermediateModel;

        ExecutorService taskPool = Executors.newFixedThreadPool(noThreads);
        List<Future<Integer[]>> asyncResults = new LinkedList<>();

        /* Match topics with mature topics */
        for (int cLevel = 0; cLevel < noGrades - 1; cLevel++) {
            intermediateModel = models.get(cLevel);

            final LDA finalIntermediateModel = intermediateModel;
            final LDA finalMatureModel = matureModel;

            Callable<Integer[]> task = () -> computeMatchTask(finalIntermediateModel, finalMatureModel);
            asyncResults.add(taskPool.submit(task));
        }

        Integer[] matches;
        List<Double> stats;
        AoEEvolution = new HashMap<>();
        for (Word analyzedWord : matureModel.getWordRepresentations().keySet()) {
            AoEEvolution.put(analyzedWord, new ArrayList<>());
        }

        for (int cLevel = 0; cLevel < noGrades - 1; cLevel++) {
            LOGGER.info("Building word distributions for grade level {}...", cLevel);
            intermediateModel = models.get(cLevel);
            matches = asyncResults.remove(0).get();

            Map<Integer, Map<Word, Double>> intermediateModelDistrib = new TreeMap<>();
            Map<Integer, Map<Word, Double>> matureModelDistrib = new TreeMap<>();

            for (int i = 0; i < intermediateModel.getNoDimensions(); i++) {
                intermediateModelDistrib.put(i, LDASupport.getWordWeights(intermediateModel, i));
                if (matches[i] != null) {
                    matureModelDistrib.put(i, LDASupport.getWordWeights(matureModel, matches[i]));
                } else {
                    matureModelDistrib.put(i, null);
                }
            }

            /*
			 * Iterate all words from mature space and extract topic
			 * distribution
             */
            LOGGER.info("Matching all words for grade level {} ...", cLevel);
            for (Word analyzedWord : matureModel.getWordRepresentations().keySet()) {
                double intermediateTopicDistr[] = new double[intermediateModel.getNoDimensions()];
                double matureTopicDistr[] = new double[intermediateModel.getNoDimensions()];

                double sumI = 0, sumM = 0, noI = 0, noM = 0;
                for (int i = 0; i < intermediateTopicDistr.length; i++) {
                    if (intermediateModelDistrib.get(i).containsKey(analyzedWord)) {
                        intermediateTopicDistr[i] = intermediateModelDistrib.get(i).get(analyzedWord);
                        if (intermediateTopicDistr[i] > 0) {
                            noI++;
                        }
                    }
                    if (matureModelDistrib.get(i) != null && matureModelDistrib.get(i).containsKey(analyzedWord)) {
                        matureTopicDistr[i] = matureModelDistrib.get(i).get(analyzedWord);
                        if (matureTopicDistr[i] > 0) {
                            noM++;
                        }
                    }
                    sumI += intermediateTopicDistr[i];
                    sumM += matureTopicDistr[i];
                }
                /* Normalize topic distribution */
                // for (i = 0; i < intermediateModel.getNumTopics(); i++) {
                // if (sumI != 0)
                // intermediateTopicDistr[i] /= sumI;
                // if (sumM != 0)
                // matureTopicDistr[i] /= sumM;
                // }

                stats = AoEEvolution.get(analyzedWord);
                double similarity = 0;
                if (sumI != 0 && sumM != 0) {
                    similarity = VectorAlgebra.cosineSimilarity(intermediateTopicDistr, matureTopicDistr);
                }
                if (similarity == 1 && noI == 1 && noM == 1) {
                    similarity = (sumM + sumI) / 2;
                }
                stats.add(similarity);
            }
        }
        taskPool.shutdown();
    }

    public static Map<String, Double> getWordAcquisitionAge(String normFile) {
        Map<String, Double> aoaWords = new HashMap<>();
        LOGGER.info("Loading file {}...", normFile);

        /* Compute the AgeOfAcquisition Dictionary */
        String tokens[];
        String line;
        String word;
        try {
            try (BufferedReader br = new BufferedReader(
                    new FileReader("resources/config/EN/word lists/AoA/" + normFile))) {
                while ((line = br.readLine()) != null) {
                    tokens = line.split(",");
                    word = tokens[0].trim().replaceAll(" ", "");

                    if (tokens[1].equals("NA")) {
                        continue;
                    }

                    Double.parseDouble(tokens[1]);
                    aoaWords.put(word, Double.parseDouble(tokens[1]));
                }
            }
        } catch (IOException | NumberFormatException e) {
            LOGGER.error(e.getMessage());
        }
        return aoaWords;
    }

    public void writeResults() {
        // determine word acquisition ages
        Map<String, Double> birdAoA = getWordAcquisitionAge("Bird.csv");
        Map<String, Double> bristolAoA = getWordAcquisitionAge("Bristol.csv");
        Map<String, Double> corteseAoA = getWordAcquisitionAge("Cortese.csv");
        Map<String, Double> kupermanAoA = getWordAcquisitionAge("Kuperman.csv");
        Map<String, Double> shockAoA = getWordAcquisitionAge("Shock.csv");

        try {
            BufferedWriter loweValues;
            try (BufferedWriter loweStats = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(path + "/AoE stats bipartite graph.csv")), "UTF-8"), 32768)) {
                loweValues = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(new File(path + "/AoE word bipartite graph.csv")), "UTF-8"), 32768);
                // create header
                String content = "Word,Bird_AoA,Bristol_AoA,Cortese_AoA,Kuperman_AoA,Shock_AoA";
                loweStats.write(content);
                loweValues.write(content);
                for (int i = 0; i < noGrades - 1; i++) {
                    loweStats.write(",Grades_1_" + (i + 1));
                }
                content = ",InverseAverage,InverseLinearRegressionSlope";
                for (double i = MIN_THRESHOLD; i <= MAX_THRESHOLD; i += THRESHOLD_INCREMENT) {
                    content += ",IndexAboveThreshold(" + i + ")";
                }
                for (double i = MIN_THRESHOLD; i <= MAX_THRESHOLD; i += THRESHOLD_INCREMENT) {
                    content += ",IndexPolynomialFitAboveThreshold(" + i + ")";
                }
                content += ",InflectionPointPolynomial\n";
                loweStats.write(content);
                loweValues.write(content);
                List<Double> stats;
                for (Word analyzedWord : matureModel.getWordRepresentations().keySet()) {
                    stats = AoEEvolution.get(analyzedWord);
                    content = analyzedWord.getExtendedLemma() + ",";
                    // AoA indices
                    if (birdAoA.containsKey(analyzedWord.getLemma())) {
                        content += birdAoA.get(analyzedWord.getLemma());
                    }
                    content += ",";
                    if (bristolAoA.containsKey(analyzedWord.getLemma())) {
                        content += bristolAoA.get(analyzedWord.getLemma());
                    }
                    content += ",";
                    if (corteseAoA.containsKey(analyzedWord.getLemma())) {
                        content += corteseAoA.get(analyzedWord.getLemma());
                    }
                    content += ",";
                    if (kupermanAoA.containsKey(analyzedWord.getLemma())) {
                        content += kupermanAoA.get(analyzedWord.getLemma());
                    }
                    content += ",";
                    if (shockAoA.containsKey(analyzedWord.getLemma())) {
                        content += shockAoA.get(analyzedWord.getLemma());
                    }
                    loweStats.write(content);
                    loweValues.write(content);
                    for (Double d : stats) {
                        loweStats.write("," + Formatting.formatNumber(d));
                    }
                    double value = WordComplexityIndices.getInverseAverage(stats);
                    if (Math.round(value * 100) / 100 != 1) {
                        content = "," + WordComplexityIndices.getInverseAverage(stats);
                        content += "," + WordComplexityIndices.getInverseLinearRegressionSlope(stats);
                        for (double i = MIN_THRESHOLD; i <= MAX_THRESHOLD; i += THRESHOLD_INCREMENT) {
                            value = WordComplexityIndices.getIndexAboveThreshold(stats, i);
                            if (value != -1) {
                                content += "," + value;
                            } else {
                                content += ",";
                            }
                        }

                        for (double i = MIN_THRESHOLD; i <= MAX_THRESHOLD; i += THRESHOLD_INCREMENT) {
                            value = WordComplexityIndices.getIndexPolynomialFitAboveThreshold(stats, i);
                            if (value != -1) {
                                content += "," + value;
                            } else {
                                content += ",";
                            }
                        }
                        content += "," + WordComplexityIndices.getInflectionPointPolynomial(stats);
                        loweStats.write(content);
                        loweValues.write(content);
                    }
                    content = "\n";
                    loweStats.write(content);
                    loweValues.write(content);
                }
            }
            loweValues.close();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    public static void main(String args[]) throws Exception {
        

        TASAAnalyzer ta = new TASAAnalyzer("resources/in/AoE HDP", 6);
        // TASAAnalyzer ta = new TASAAnalyzer("resources/in/AoE 100", 6);
        ta.loadModels();
        ta.performMatching();
        ta.writeResults();
    }
}
