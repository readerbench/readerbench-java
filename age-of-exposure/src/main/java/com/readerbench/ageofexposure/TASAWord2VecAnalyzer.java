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

import com.readerbench.coreservices.commons.Formatting;
import com.readerbench.coreservices.commons.VectorAlgebra;
import com.readerbench.coreservices.semanticModels.word2vec.Word2VecModel;
import com.readerbench.datasourceprovider.data.Word;
import com.readerbench.datasourceprovider.pojo.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class TASAWord2VecAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TASAWord2VecAnalyzer.class);

    private final double MIN_THRESHOLD = 0.2;
    private final double MAX_THRESHOLD = 0.7;
    private final double THRESHOLD_INCREMENT = 0.1;

    private final String path;
    private final Lang lang;
    private Map<Integer, Word2VecModel> models;
    private int noClasses;
    private HashMap<Word, double[]> referenceVectors;
    private Set<Word> wordList;
    private Map<Word, List<Double>> AoEEvolution;

    public TASAWord2VecAnalyzer(String path, Lang lang) {
        this.path = path;
        this.lang = lang;
    }

    public void loadModels() {
        noClasses = (new File(path)).listFiles((File pathname) -> {
            if (pathname.isDirectory()) {
                return true;
            }
            return false;
        }).length;

        /* Load LDA Models */
        models = new TreeMap<>();
        for (int i = 0; i < noClasses; i++) {
            String classPath = path + "/grade" + i;
            LOGGER.info("Loading model " + classPath + " ...");
            models.put(i, Word2VecModel.loadWord2Vec(classPath, lang));
        }
    }

    @SuppressWarnings("unchecked")
    public void computeReferenceVectors() throws IOException, ClassNotFoundException {
        Word2VecModel matureModel = models.get(noClasses - 1);
        wordList = matureModel.getWordSet();

        referenceVectors = new HashMap<>();
        AoEEvolution = new TreeMap<>();

        for (Word word : wordList) {
            int index = 0;
            double[] similarityArray = new double[referenceVectors.size()];
            for (Word referenceWord : wordList) {
                similarityArray[index] = matureModel.getSimilarity(word, referenceWord);
                index++;
            }
            referenceVectors.put(word, similarityArray);
            AoEEvolution.put(word, new ArrayList<>());
        }

        try (ObjectOutputStream vectorFile = new ObjectOutputStream(new FileOutputStream(path + "reference_vectors.bin"))) {
            vectorFile.writeObject(referenceVectors);
        }
    }

    public void performMatching() throws InterruptedException, ExecutionException, FileNotFoundException {
        
        /* Match topics with mature topics */
        for (int cLevel = 0; cLevel < noClasses - 1; cLevel++) {
            Word2VecModel intermediateModel = models.get(cLevel);
            PrintStream outFile = new PrintStream(new FileOutputStream(path + "/grade" + cLevel + "/similarity.csv"));
            for (Word word : wordList) {
                List<Double> wordEvolution = AoEEvolution.get(word);
                if (!intermediateModel.getWordSet().contains(word)) {
                    outFile.println(word + ",0.0");
                    wordEvolution.add(0.0);
                    continue;
                }

                int index = 0;
                double[] similarityArray = new double[wordList.size()];
                for (Word referenceWord : wordList) {
                    similarityArray[index] = intermediateModel.getSimilarity(word, referenceWord);
                    similarityArray[index] = 0;
                    index++;
                }

                double cosine = VectorAlgebra.cosineSimilarity(similarityArray, referenceVectors.get(word));
                outFile.println(word + "," + cosine);
                wordEvolution.add(cosine);
            }
            outFile.close();
        }
    }

    public void writeResults() {
        // determine word acquisition ages
        Map<String, Double> birdAoA = TASAAnalyzer.getWordAcquisitionAge("Bird.csv");
        Map<String, Double> bristolAoA = TASAAnalyzer.getWordAcquisitionAge("Bristol.csv");
        Map<String, Double> corteseAoA = TASAAnalyzer.getWordAcquisitionAge("Cortese.csv");
        Map<String, Double> kupermanAoA = TASAAnalyzer.getWordAcquisitionAge("Kuperman.csv");
        Map<String, Double> shockAoA = TASAAnalyzer.getWordAcquisitionAge("Shock.csv");

        BufferedWriter loweValues;
        try (BufferedWriter loweStats = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(new File(path + "/AoE word2vec stats full matching.csv")), "UTF-8"),
                32768)) {
            loweValues = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(new File(path + "/AoE word2vec word full matching.csv")), "UTF-8"),
                    32768);
            // create header
            String content = "Word,Bird_AoA,Bristol_AoA,Cortese_AoA,Kuperman_AoA,Shock_AoA";
            loweStats.write(content);
            loweValues.write(content);
            for (int i = 0; i < noClasses - 1; i++) {
                loweStats.write(",C_1_" + (i + 1));
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
            for (Word analyzedWord : AoEEvolution.keySet()) {
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
            loweValues.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws Exception {
        

        TASAWord2VecAnalyzer analyzer = new TASAWord2VecAnalyzer("resources/in/AoE 100", Lang.en);
        analyzer.loadModels();
        analyzer.computeReferenceVectors();
        analyzer.performMatching();
        analyzer.writeResults();
    }
}
