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
package com.readerbench.coreservices.semanticmodels.word2vec;

import com.readerbench.coreservices.data.AnalysisElement;
import com.readerbench.coreservices.data.Word;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.coreservices.commons.VectorAlgebra;
import com.readerbench.coreservices.nlp.stemmer.Stemmer;
import com.readerbench.coreservices.semanticmodels.data.ISemanticModel;
import com.readerbench.coreservices.semanticmodels.data.SimilarityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author Stefan Ruseti, Mihai Dascalu
 */
public class Word2VecModel implements ISemanticModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(Word2VecModel.class);

    private static final List<Word2VecModel> LOADED_WORD2VEC_MODELS = new ArrayList<>();
    private static final Set<Lang> AVAILABLE_FOR = EnumSet.of(Lang.en);

    private final Lang language;
    private final String path;
    private final int noDimensions;
    private final Map<Word, double[]> wordVectors;

    private Word2VecModel(String path, Lang language, Map<String, List<Double>> model) {
        this.language = language;
        this.path = path;
        this.wordVectors = model.keySet().stream()
                .map(w -> new Word(w, w, Stemmer.stemWord(w, language), null, null, language))
                .collect(Collectors.toMap(
                        Function.identity(),
                        w -> model.get(w.getLemma()).stream().mapToDouble(d -> d).toArray()));
        this.noDimensions = wordVectors.values().stream()
                .findFirst()
                .map(v -> v.length)
                .get();
    }

    private Word2VecModel(String path, Lang language, int dim) {
        this.language = language;
        this.path = path;
        this.wordVectors = new HashMap<>();
        this.noDimensions = dim;
    }

    public static Word2VecModel loadFromTextFile(String path, Lang language) {
        try (BufferedReader in = new BufferedReader(new FileReader(path + "/word2vec.model"))) {
            String[] line = in.readLine().split(" ");
            int nWords = Integer.parseInt(line[0]);
            int dim = Integer.parseInt(line[1]);
            Word2VecModel model = new Word2VecModel(path, language, dim);
            for (int i = 0; i < nWords; i++) {
                line = in.readLine().split(" ");
                String label = line[0];
                Word word = new Word(label, label, Stemmer.stemWord(label, language), null, null, language);
                model.wordVectors.put(word, Arrays.stream(line, 1, line.length)
                        .mapToDouble(Double::parseDouble)
                        .toArray());
            }
            return model;
        } catch (FileNotFoundException ex) {
            LOGGER.error(ex.getMessage());
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
        return null;
    }

    public static Word2VecModel loadWord2Vec(String path, Lang language) {
        for (Word2VecModel w2v : LOADED_WORD2VEC_MODELS) {
            if (path.equals(w2v.getPath())) {
                return w2v;
            }
        }
        Word2VecModel w2v = loadFromTextFile(path, language);
        LOADED_WORD2VEC_MODELS.add(w2v);
        return w2v;
    }

    @Override
    public double getSimilarity(double[] v1, double[] v2) {
        return VectorAlgebra.cosineSimilarity(v1, v2);
    }

    @Override
    public double getSimilarity(AnalysisElement e1, AnalysisElement e2) {
        return this.getSimilarity(e1.getModelRepresentation(SimilarityType.WORD2VEC), e2.getModelRepresentation(SimilarityType.WORD2VEC));
    }

    @Override
    public TreeMap<Word, Double> getSimilarConcepts(Word w, double minThreshold) {
        return getSimilarConcepts(w.getModelRepresentation(SimilarityType.WORD2VEC), minThreshold);
    }

    @Override
    public TreeMap<Word, Double> getSimilarConcepts(AnalysisElement e, double minThreshold) {
        return getSimilarConcepts(e.getModelRepresentation(SimilarityType.WORD2VEC), minThreshold);
    }

    private TreeMap<Word, Double> getSimilarConcepts(double[] vector, double minThreshold) {
        TreeMap<Word, Double> similarConcepts = new TreeMap<>();
        double sim;
        for (Map.Entry<Word, double[]> entry : wordVectors.entrySet()) {
            sim = VectorAlgebra.cosineSimilarity(vector, entry.getValue());
            if (sim >= minThreshold) {
                similarConcepts.put(entry.getKey(), sim);
            }
        }
        return similarConcepts;
    }

    @Override
    public Map<Word, double[]> getWordRepresentations() {
        return wordVectors;
    }

    @Override
    public double[] getWordRepresentation(Word w) {
        return wordVectors.get(w);
    }

    @Override
    public Set<Word> getWordSet() {
        return wordVectors.keySet();
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Lang getLanguage() {
        return language;
    }

    public static Set<Lang> getAvailableLanguages() {
        return AVAILABLE_FOR;
    }

    @Override
    public SimilarityType getType() {
        return SimilarityType.WORD2VEC;
    }

    @Override
    public BiFunction<double[], double[], Double> getSimilarityFuction() {
        return VectorAlgebra::cosineSimilarity;
    }

    @Override
    public int getNoDimensions() {
        return noDimensions;
    }
}
