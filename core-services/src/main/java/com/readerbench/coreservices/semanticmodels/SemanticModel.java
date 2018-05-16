/**
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
package com.readerbench.coreservices.semanticmodels;

import com.readerbench.coreservices.data.AnalysisElement;
import com.readerbench.coreservices.data.Word;
import com.readerbench.coreservices.nlp.parsing.Parsing;
import com.readerbench.datasourceprovider.pojo.Lang;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mihai Dascalu
 */
public class SemanticModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(SemanticModel.class);
    private static final String SEMANTIC_MODELS_PATH = "semantic-models-%s-%s/%s.model";

    private final String name;
    private final Lang language;
    private final SimilarityType similarityType;
    private int noDimensions;
    private final Map<Word, double[]> wordRepresentations;
    private static final List<SemanticModel> LOADED_MODELS = new ArrayList<>();

    private SemanticModel(String name, Lang lang, SimilarityType similarityType) {
        this.name = name.toLowerCase();
        this.language = lang;
        this.similarityType = similarityType;
        this.wordRepresentations = new TreeMap<>();
    }

    public static SemanticModel loadModel(String name, Lang lang, SimilarityType similarityType) {
        SemanticModel semModel = new SemanticModel(name, lang, similarityType);
        for (SemanticModel model : LOADED_MODELS) {
            if (semModel.equals(model)) {
                return model;
            }
        }

        semModel.importModel(String.format(SEMANTIC_MODELS_PATH, name.toLowerCase(), lang, similarityType.getAcronym()));
        if (semModel.getWordRepresentations().isEmpty()) {
            return null;
        }
        return semModel;
    }

    public static List<SemanticModel> loadModels(String name, Lang lang) {
        List<SemanticModel> models = Arrays.asList(SimilarityType.LSA, SimilarityType.LDA, SimilarityType.WORD2VEC)
                .stream()
                .map((simType) -> loadModel(name, lang, simType))
                .filter((model) -> (model != null))
                .collect(Collectors.toList());
        if (models.isEmpty()) {
            return null;
        }
        return models;
    }

    private void importModel(String fileName) {
        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream(fileName); BufferedReader in = new BufferedReader(new InputStreamReader(input, "UTF-8"))) {
            String[] line = in.readLine().split(" ");
            int nWords = Integer.parseInt(line[0]);
            noDimensions = Integer.parseInt(line[1]);
            for (int i = 0; i < nWords; i++) {
                line = in.readLine().split(" ");
                String label = line[0];
                Word word = Parsing.getWordFromConcept(label, language);
                wordRepresentations.put(word, Arrays.stream(line, 1, line.length)
                        .mapToDouble(Double::parseDouble)
                        .toArray());
            }
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    public void exportToCSV(String fileName) {
        try (PrintWriter out = new PrintWriter(fileName)) {
            out.println(wordRepresentations.size() + " " + this.getNoDimensions());
            for (Map.Entry<Word, double[]> entry : this.getWordRepresentations().entrySet()) {
                String v = Arrays.stream(entry.getValue())
                        .mapToObj(d -> d + "")
                        .collect(Collectors.joining(" "));
                out.println(entry.getKey().getText() + " " + v);
            }
        } catch (FileNotFoundException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    public double getSimilarity(AnalysisElement e1, AnalysisElement e2) {
        return this.getSimilarity(e1.getModelRepresentation(similarityType), e2.getModelRepresentation(similarityType));
    }

    public double getSimilarity(double[] v1, double[] v2) {
        return (v1 == null || v2 == null) ? 0 : getSimilarityFuction().apply(v1, v2);
    }

    public BiFunction<double[], double[], Double> getSimilarityFuction() {
        return similarityType.getSimilarityFuction();
    }

    public TreeMap<Word, Double> getSimilarConcepts(AnalysisElement e, double minThreshold) {
        return getSimilarConcepts(e.getModelRepresentation(similarityType), minThreshold);
    }

    public TreeMap<Word, Double> getSimilarConcepts(double[] vector, double minThreshold) {
        TreeMap<Word, Double> similarConcepts = new TreeMap<>();
        for (Entry<Word, double[]> e : wordRepresentations.entrySet()) {
            double sim = getSimilarity(vector, e.getValue());
            if (sim >= minThreshold) {
                similarConcepts.put(e.getKey(), sim);
            }
        }
        return similarConcepts;
    }

    public Set<Word> getWordSet() {
        return wordRepresentations.keySet();
    }

    public Map<Word, double[]> getWordRepresentations() {
        return wordRepresentations;
    }

    public double[] getWordRepresentation(Word w) {
        return wordRepresentations.get(w);
    }

    public String getName() {
        return name;
    }

    public Lang getLanguage() {
        return language;
    }

    public SimilarityType getSimilarityType() {
        return similarityType;
    }

    public int getNoDimensions() {
        return noDimensions;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + Objects.hashCode(this.name);
        hash = 61 * hash + Objects.hashCode(this.language);
        hash = 61 * hash + Objects.hashCode(this.similarityType);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SemanticModel other = (SemanticModel) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (this.language != other.language) {
            return false;
        }
        if (this.similarityType != other.similarityType) {
            return false;
        }
        return true;
    }
}
