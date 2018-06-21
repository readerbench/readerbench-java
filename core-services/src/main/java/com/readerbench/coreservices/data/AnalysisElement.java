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
package com.readerbench.coreservices.data;

import com.readerbench.coreservices.keywordmining.Keyword;
import com.readerbench.coreservices.semanticmodels.SimilarityType;
import com.readerbench.coreservices.data.cscl.Utterance;
import com.readerbench.coreservices.semanticmodels.SemanticModel;
import com.readerbench.datasourceprovider.pojo.Lang;
//import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

/**
 * This abstract class is the base for all type of elements. It is extended
 * later for all processing elements in the following hierarchical order:
 * Document > Block > Utterance.
 *
 * @author Mihai Dascalu
 */
public abstract class AnalysisElement implements Serializable {

    private static final long serialVersionUID = -8110285459013257550L;

    //private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AnalysisElement.class);

    private int index;
    protected transient Map<SimilarityType, SemanticModel> semanticModels;
    protected Map<SimilarityType, double[]> modelVectors;

    private Lang language;
    // the upper level element in the analysis hierarchy: document > block / utterance > sentence
    protected AnalysisElement container;
    private String text;
    private String processedText; // lemmas without stop-words and punctuation
    private String alternateText; // text used for display in different colors
    private Map<Word, Integer> wordOccurences;
    private double score;
    private double[] voiceDistribution;
    private double[] extendedVoiceDistribution;
    // specificity score computed for a specific class of topics

    private List<Keyword> topics;
    private List<Keyword> inferredConcepts;

    /**
     *
     */
    public AnalysisElement() {
        this.processedText = "";
        this.alternateText = "";
        this.wordOccurences = new TreeMap<>();
        this.topics = new ArrayList<>();
        this.inferredConcepts = new ArrayList<>();
        this.semanticModels = new EnumMap<>(SimilarityType.class);
        this.modelVectors = new EnumMap<>(SimilarityType.class);
    }

    /**
     * @param models
     * @param language
     */
    public AnalysisElement(List<SemanticModel> models, Lang language) {
        this();
        if (models != null) {
            models.stream().filter((model) -> (model != null)).forEach((model) -> {
                semanticModels.put(model.getSimilarityType(), model);
            });
        }
        this.language = language;
    }

    /**
     * @param elem
     * @param index
     * @param models
     * @param language
     */
    public AnalysisElement(AnalysisElement elem, int index, List<SemanticModel> models, Lang language) {
        this(models, language);
        this.index = index;
        this.container = elem;
    }

    /**
     * @param text
     * @param models
     * @param language
     */
    public AnalysisElement(String text, List<SemanticModel> models, Lang language) {
        this(models, language);
        this.text = text;
        this.alternateText = text;
    }

    /**
     * @param elem
     * @param index
     * @param text
     * @param models
     * @param language
     */
    public AnalysisElement(AnalysisElement elem, int index, String text, List<SemanticModel> models, Lang language) {
        this(elem, index, models, language);
        this.text = text;
        this.alternateText = text;
    }

    /**
     * Determine vector representations for all semantic models by relying on Tf
     * * individual word representation
     */
    public void determineSemanticDimensions() {
        for (Map.Entry<SimilarityType, SemanticModel> e : semanticModels.entrySet()) {
            double[] vec = new double[e.getValue().getNoDimensions()];
            for (Word word : wordOccurences.keySet()) {
                if (word.getModelRepresentation(e.getKey()) != null) {
                    double tf = (1 + Math.log(wordOccurences.get(word)));
                    double[] v = word.getModelRepresentation(e.getKey());
                    for (int i = 0; i < e.getValue().getNoDimensions(); i++) {
                        vec[i] += tf * v[i];
                    }
                }
            }
            modelVectors.put(e.getKey(), vec);
        }
    }

    /**
     * Determines number of occurrences for each word in a list of analysis
     * elements. The method goes through all elements and, for each word,
     * increments the number of occurrences in a local variable. TODO: word
     * occurrences from (Documents from Blocks), (Blocks from Sentences)
     *
     * @param elements The list of analysis elements
     */
    public void determineWordOccurences(List<? extends AnalysisElement> elements) {
        // add all word occurrences from lower level analysis elements
        // (Documents from Blocks), (Blocks from Utterances)
        wordOccurences = new TreeMap<>();
        elements.stream().filter((el) -> (el != null)).forEach((el) -> {
            el.getWordOccurences().keySet().stream().forEach((w) -> {
                if (wordOccurences.containsKey(w)) {
                    wordOccurences.put(w, wordOccurences.get(w) + el.getWordOccurences().get(w));
                } else {
                    wordOccurences.put(w, el.getWordOccurences().get(w));
                }
            });
        });
    }

    /**
     * @return map of (word, no_occurrences) associations
     */
    public Map<Word, Integer> getWordOccurences() {
        return wordOccurences;
    }

    /**
     * @param wordOccurences map of (word, no_occurrences) associations to be
     * set
     */
    public void setWordOccurences(Map<Word, Integer> wordOccurences) {
        this.wordOccurences = wordOccurences;
    }

    /**
     * @return the language the text is written in
     */
    public Lang getLanguage() {
        return language;
    }

    /**
     * @param language the language the text is written in to be set
     */
    public final void setLanguage(Lang language) {
        this.language = language;
    }

    /**
     * @return initial score for the analysis element
     */
    public double getScore() {
        return score;
    }

    /**
     * @param score score for the analysis element to be set
     */
    public void setScore(double score) {
        this.score = score;
    }

    /**
     * @return parsed text
     */
    public String getText() {
        return text == null ? "" : text;
    }

    /**
     * @param text parsed text to be set
     */
    public final void setText(String text) {
        this.text = text;
        this.alternateText = text;
    }

    /**
     * @return processed text
     */
    public String getProcessedText() {
        return processedText == null ? "" : processedText;
    }

    /**
     * @param processedText processed text to be set
     */
    public void setProcessedText(String processedText) {
        this.processedText = processedText;
    }

    /**
     * @return current analysis element index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @param index current index to be set
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * @return
     */
    public AnalysisElement getContainer() {
        return container;
    }

    /**
     * @param container
     */
    public void setContainer(AnalysisElement container) {
        this.container = container;
    }

    /**
     * @return
     */
    public String getAlternateText() {
        return alternateText == null ? "" : alternateText;
    }

    /**
     * @param alternateText
     */
    public void setAlternateText(String alternateText) {
        this.alternateText = alternateText;
    }

    /**
     * @return
     */
    public List<Keyword> getTopics() {
        return topics;
    }

    /**
     * @param topics
     */
    public void setTopics(List<Keyword> topics) {
        this.topics = topics;
    }

    /**
     * @return
     */
    public List<Keyword> getInferredConcepts() {
        return inferredConcepts;
    }

    /**
     * @param inferredConcepts
     */
    public void setInferredConcepts(List<Keyword> inferredConcepts) {
        this.inferredConcepts = inferredConcepts;
    }

    /**
     * @return
     */
    public double[] getVoiceDistribution() {
        return voiceDistribution;
    }

    /**
     * @param voiceDistribution
     */
    public void setVoiceDistribution(double[] voiceDistribution) {
        this.voiceDistribution = voiceDistribution;
    }

    public double[] getExtendedVoiceDistribution() {
        return extendedVoiceDistribution;
    }

    public void setExtendedVoiceDistribution(double[] extendedVoiceDistribution) {
        this.extendedVoiceDistribution = extendedVoiceDistribution;
    }

    public List<SemanticModel> getSemanticModelsAsList() {
        return new ArrayList<>(semanticModels.values());
    }

    public Map<SimilarityType, SemanticModel> getSemanticModels() {
        return semanticModels;
    }

    public final void setSemanticModels(List<SemanticModel> models) {
        semanticModels = new EnumMap<>(SimilarityType.class);
        for (SemanticModel model : models) {
            semanticModels.put(model.getSimilarityType(), model);
        }
    }

    public Map<SimilarityType, double[]> getModelVectors() {
        return modelVectors;
    }

    public void setModelVectors(Map<SimilarityType, double[]> modelVectors) {
        this.modelVectors = modelVectors;
    }

    public double[] getModelRepresentation(SimilarityType type) {
        return modelVectors.get(type);
    }

    public SemanticModel getSemanticModel(SimilarityType type) {
        return semanticModels.get(type);
    }

    public List<NGram> getBiGrams() {
        return new ArrayList<>();
    }

    public List<NGram> getNGrams(int n) {
        return new ArrayList<>();
    }

    @Override
    public int hashCode() {
        if (this instanceof Utterance) {
            return Objects.hash(this.index, this.text, ((Utterance) this).getTime(), this.getContainer());
        }
        return Objects.hash(this.index, this.text, this.getContainer());
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
        final AnalysisElement other = (AnalysisElement) obj;
        if (this.index != other.index) {
            return false;
        }
        if (!Objects.equals(this.text, other.text)) {
            return false;
        }
        if (this instanceof Utterance && other instanceof Utterance && !Objects.equals(((Utterance) this).getTime(), ((Utterance) other).getTime())) {
            return false;
        }
        return Objects.equals(this.getContainer(), other.getContainer());
    }
}
