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
package services.semanticModels.LSA;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualTreeBidiMap;
import org.openide.util.Exceptions;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

import data.AnalysisElement;
import data.Lang;
import data.Word;
import services.commons.ObjectManipulation;
import services.commons.VectorAlgebra;
import services.semanticModels.ISemanticModel;
import services.semanticModels.SimilarityType;

/**
 *
 * @author Mihai Dascalu
 */
public class LSA implements ISemanticModel {

    static final Logger LOGGER = Logger.getLogger("");

    private static final List<LSA> LOADED_LSA_SPACES = new LinkedList<>();
    public static final int LOWER_BOUND = 50;
    public static final double LSA_THRESHOLD = 0.25;
    public int K = 300;
    public static final int NO_KNN_NEIGHBOURS = 100;

    private static final Set<Lang> AVAILABLE_FOR = EnumSet.of(Lang.en, Lang.es, Lang.fr, Lang.la, Lang.ro);

    private Lang language;
    private String path;
    private double[][] Uk;
    private double[] Sk;
    private double[][] Vtk;
    private BidiMap<Word, Integer> words;
    private Map<Word, Double> mapIdf;
    private double[] vectorSpaceMean;
    private Map<Word, double[]> wordVectors;

    public static LSA loadLSA(String path, Lang language) {
        try {
            for (LSA lsa : LOADED_LSA_SPACES) {
                if (path.equals(lsa.getPath())) {
                    return lsa;
                }
            }

            LOGGER.log(Level.INFO, "Loading LSA semantic space {0} ...", path);
            LSA lsaLoad = new LSA();
            lsaLoad.setLanguage(language);
            lsaLoad.setPath(path);
            lsaLoad.loadWordList(path);
            lsaLoad.loadIdf(path);
            lsaLoad.Uk = (double[][]) ObjectManipulation.loadObject(path + "/U.ser");
            //update K if different dimensionality
            lsaLoad.K = lsaLoad.Uk[0].length;
            lsaLoad.determineSpaceMean();
            lsaLoad.determineWordRepresentations();
            LOADED_LSA_SPACES.add(lsaLoad);
            return lsaLoad;
        } catch (IOException | ClassNotFoundException ex) {
            Exceptions.printStackTrace(ex);
            LOGGER.info("Error during vector space loading!");
            return null;
        }
    }

    protected void loadIdf(String path) throws FileNotFoundException, IOException {
        // loads IDf matrix from file
        FileInputStream inputFile = new FileInputStream(path + "/idf.txt");
        InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
        try (BufferedReader in = new BufferedReader(ir)) {
            mapIdf = new TreeMap<>();
            String str_linie;
            StringTokenizer strk;
            while ((str_linie = in.readLine()) != null) {
                strk = new StringTokenizer(str_linie, " ");
                mapIdf.put(Word.getWordFromConcept(strk.nextToken(), language), Double.parseDouble(strk.nextToken()));
            }
        }
    }

    protected void loadWordList(String path) throws FileNotFoundException, IOException {
        // loads IDf matrix from file
        FileInputStream inputFile = new FileInputStream(path + "/wordlist.txt");
        InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
        try (BufferedReader in = new BufferedReader(ir)) {
            words = new DualTreeBidiMap<>();
            String str_linie;
            StringTokenizer strk;

            while ((str_linie = in.readLine()) != null) {
                strk = new StringTokenizer(str_linie, " ");
                words.put(Word.getWordFromConcept(strk.nextToken(), language), Integer.parseInt(strk.nextToken()));
            }
        }
    }

    public double getWordIDf(Word word) {
        double idf = 0;
        if (words.containsKey(word)) {
            // words exist in learning space
            idf = Math.log(mapIdf.get(word));
        } else {
            // extract all words from the semantic space that have the same
            // stem
            int no = 0;
            for (Word w : words.keySet()) {
                if (w.getStem().equals(word.getStem())) {
                    idf += 1 / mapIdf.get(w);
                    no++;
                }
            }
            if (no != 0) {
                idf = Math.log(no / idf);
            }
        }
        return idf;
    }

    public double[] getWordVector(Word word) {
        double[] vector = new double[K];
        if (words.containsKey(word)) {
            // words exist in learning space
            int index = words.get(word);
            System.arraycopy(Uk[index], 0, vector, 0, K);
        } else {
            // extract all words from the semantic space that have the same
            // stem
            int no = 0;
            for (Word w : words.keySet()) {
                if (w.getStem().equals(word.getStem())) {
                    int index = words.get(w);
                    for (int i = 0; i < K; i++) {
                        vector[i] += Uk[index][i];
                    }
                    no++;
                }
            }
            if (no != 0) {
                for (int i = 0; i < K; i++) {
                    vector[i] /= no;
                }
            }
        }
        return vector;
    }

    private void determineSpaceMean() {
        // determine space median
        vectorSpaceMean = new double[K];
        words.keySet().stream().forEach((w) -> {
            double idf = Math.log(mapIdf.get(w));
            int index = words.get(w);
            for (int i = 0; i < K; i++) {
                vectorSpaceMean[i] += Uk[index][i] * idf;
            }
        });
        for (int i = 0; i < K; i++) {
            vectorSpaceMean[i] /= words.size();
        }
    }

    private void determineWordRepresentations() {
        // determine space median
        wordVectors = new TreeMap<>();
        words.keySet().stream().forEach((w) -> {
            double[] vector = new double[K];
            int index = words.get(w);
            for (int i = 0; i < K; i++) {
                vector[i] += Uk[index][i];
            }
            wordVectors.put(w, vector);
        });
    }

    @Override
    public double getSimilarity(double[] v1, double[] v2) {
        return VectorAlgebra.cosineSimilarity(v1, v2);
    }

    @Override
    public double getSimilarity(AnalysisElement e1, AnalysisElement e2) {
        return this.getSimilarity(e1.getModelRepresentation(SimilarityType.LSA), e2.getModelRepresentation(SimilarityType.LSA));
    }

    @Override
    public TreeMap<Word, Double> getSimilarConcepts(Word w, double minThreshold) {
        return getSimilarConcepts(getWordVector(w), minThreshold);
    }

    @Override
    public TreeMap<Word, Double> getSimilarConcepts(AnalysisElement e, double minThreshold) {
        return getSimilarConcepts(e.getModelRepresentation(SimilarityType.LSA), minThreshold);
    }

    private TreeMap<Word, Double> getSimilarConcepts(double[] vector, double minThreshold) {
        TreeMap<Word, Double> similarConcepts = new TreeMap<>();
        double[] vector2;
        double sim;
        for (Word c : words.keySet()) {
            vector2 = getWordVector(c);
            sim = VectorAlgebra.cosineSimilarity(vector, vector2);
            if (sim >= minThreshold) {
                similarConcepts.put(c, sim);
            }
        }
        return similarConcepts;
    }

    public Word getMostSimilarConcept(Word w) {
        return getMostSimilarConcept(getWordVector(w));
    }

    public Word getMostSimilarConcept(double[] vector) {
        Word res = null;
        double sim, max = 0;

        for (Word c : words.keySet()) {
            sim = VectorAlgebra.cosineSimilarity(vector, getWordVector(c));
            if (sim >= max) {
                res = c;
                max = sim;
            }
        }

        return res;
    }

    @Override
    public Lang getLanguage() {
        return language;
    }

    public void setLanguage(Lang language) {
        this.language = language;
    }

    @Override
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public double[][] getUk() {
        return Uk;
    }

    public void setUk(double[][] uk) {
        Uk = uk;
    }

    public double[] getSk() {
        return Sk;
    }

    public void setSk(double[] sk) {
        Sk = sk;
    }

    public double[][] getVtk() {
        return Vtk;
    }

    public void setVtk(double[][] vtk) {
        Vtk = vtk;
    }

    public BidiMap<Word, Integer> getWords() {
        return words;
    }

    @Override
    public Set<Word> getWordSet() {
        return words.keySet();
    }

    @Override
    public Map<Word, double[]> getWordRepresentations() {
        return wordVectors;
    }

    @Override
    public double[] getWordRepresentation(Word w) {
        return wordVectors.get(w);
    }

    public void setWords(BidiMap<Word, Integer> words) {
        this.words = words;
    }

    public Map<Word, Double> getMapIdf() {
        return mapIdf;
    }

    public void setMapIdf(Map<Word, Double> mapIdf) {
        this.mapIdf = mapIdf;
    }

    public double[] getVectorSpaceMean() {
        return vectorSpaceMean;
    }

    public void setVectorSpaceMean(double[] vectorSpaceMean) {
        this.vectorSpaceMean = vectorSpaceMean;
    }

    public static Set<Lang> getAvailableLanguages() {
        return AVAILABLE_FOR;
    }

    @Override
    public SimilarityType getType() {
        return SimilarityType.LSA;
    }

    @Override
    public BiFunction<double[], double[], Double> getSimilarityFuction() {
        return VectorAlgebra::cosineSimilarity;
    }

    @Override
    public int getNoDimensions() {
        return this.K;
    }
}
