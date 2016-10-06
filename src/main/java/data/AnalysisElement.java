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
package data;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import data.discourse.Keyword;
import data.sentiment.SentimentEntity;
import java.util.ArrayList;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.word2vec.Word2VecModel;

/**
 * This abstract class is the base for all type of elements. It is extended
 * later for all processing elements in the following hierarchical order:
 * Document > Block > Utterance.
 *
 * @author Mihai Dascalu
 */
public abstract class AnalysisElement implements Serializable {

    private static final long serialVersionUID = -8110285459013257550L;
    protected static final Logger LOGGER = Logger.getLogger(AnalysisElement.class);

    private int index;
    private transient LSA lsa;
    private transient LDA lda;
    private transient Word2VecModel w2vModel;
    private Lang language;
    // the upper level element in the analysis hierarchy: document > block / utterance > sentence
    private AnalysisElement container;
    private String text;
    private String processedText; // lemmas without stop-words and punctuation
    private String alternateText; // text used for display in different colors
    private Map<Word, Integer> wordOccurences;
    private double[] lsaVector;
    private double[] ldaProbDistribution;
    private double[] w2v;
    private double individualScore;
    private double overallScore;
    private double[] voiceDistribution;
    // specificity score computed for a specific class of topics
    private transient double specificity;

    private List<Keyword> topics;
    private List<Keyword> inferredConcepts;

    private transient SentimentEntity sentimentEntity;

    /**
     *
     */
    public AnalysisElement() {
        this.processedText = "";
        this.alternateText = "";
        this.lsaVector = new double[LSA.K];
        this.wordOccurences = new TreeMap<>();
        this.topics = new ArrayList<>();
        this.inferredConcepts = new ArrayList<>();
        this.sentimentEntity = new SentimentEntity();
    }

    /**
     * @param lsa
     * @param lda
     * @param language
     */
    public AnalysisElement(LSA lsa, LDA lda, Lang language) {
        this();
        this.lsa = lsa;
        this.lda = lda;
        this.language = language;
    }

    /**
     * @param elem
     * @param index
     * @param lsa
     * @param lda
     * @param language
     */
    public AnalysisElement(AnalysisElement elem, int index, LSA lsa, LDA lda, Lang language) {
        this(lsa, lda, language);
        this.index = index;
        this.container = elem;
    }

    /**
     * @param text
     * @param lsa
     * @param lda
     * @param language
     */
    public AnalysisElement(String text, LSA lsa, LDA lda, Lang language) {
        this(lsa, lda, language);
        this.text = text;
        this.alternateText = text;
    }

    /**
     * @param elem
     * @param index
     * @param text
     * @param lsa
     * @param lda
     * @param language
     */
    public AnalysisElement(AnalysisElement elem, int index, String text, LSA lsa, LDA lda, Lang language) {
        this(elem, index, lsa, lda, language);
        this.text = text;
        this.alternateText = text;
    }

    /**
     * Determines the LSA vector for the corresponding analysis element by using
     * local tf-idf * LSA Determines the LDA probability distribution. TODO:
     * explain better
     */
    public void determineSemanticDimensions() {
        // determine the vector for the corresponding analysis element by using
        // local TfIdf * LSA
        if (lsa != null) {
            for (Word word : wordOccurences.keySet()) {
                double factor = (1 + Math.log(wordOccurences.get(word)) * word.getIdf());
                for (int i = 0; i < LSA.K; i++) {
                    lsaVector[i] += word.getLSAVector()[i] * factor;
                }
            }
        }

        // determine LDA distribution
        if (lda != null) {
            this.ldaProbDistribution = lda.getProbDistribution(this);
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
     * @return Latent Semantic Analysis object
     */
    public LSA getLSA() {
        return lsa;
    }

    /**
     * @param lsa Latent Semantic Analysis object to be set
     */
    public final void setLSA(LSA lsa) {
        this.lsa = lsa;
    }

    /**
     * @return Latent Dirichlet Allocation object
     */
    public LDA getLDA() {
        return lda;
    }

    /**
     * @param lda Latent Dirichlet Allocation object to be set
     */
    public final void setLDA(LDA lda) {
        this.lda = lda;
    }

    /**
     * @return word2Vec object
     */
    public Word2VecModel getW2vModel() {
        return w2vModel;
    }

    /**
     * @param w2vModel word2Vec object to be set
     */
    public void setW2vModel(Word2VecModel w2vModel) {
        this.w2vModel = w2vModel;
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
     * @return Latent Semantic Analysis vector
     */
    public double[] getLSAVector() {
        return lsaVector;
    }

    /**
     * @param vector Latent Semantic Analysis vector to be set
     */
    public void setLSAVector(double[] vector) {
        this.lsaVector = vector;
    }

    /**
     * @return Latent Dirichlet Allocation Probability Distribution vector
     */
    public double[] getLDAProbDistribution() {
        return ldaProbDistribution;
    }

    /**
     * @param ldaProbDistribution Latent Dirichlet Allocation Probability
     * Distribution vector to be set
     */
    public void setLDAProbDistribution(double[] ldaProbDistribution) {
        this.ldaProbDistribution = ldaProbDistribution;
    }

    /**
     * @return word2vec vector
     */
    public double[] getWord2Vec() {
        return w2v;
    }

    /**
     * @param w2v word2vec vector to be set
     */
    public void setWord2Vec(double[] w2v) {
        this.w2v = w2v;
    }

    /**
     * @return initial score for the analysis element
     */
    public double getIndividualScore() {
        return individualScore;
    }

    /**
     * @param individualScore score for the analysis element to be set
     */
    public void setIndividualScore(double individualScore) {
        this.individualScore = individualScore;
    }

    /**
     * @return total score after augmentation from the cohesion graph
     */
    public double getOverallScore() {
        return overallScore;
    }

    /**
     * @param overallScore total score to be set
     */
    public void setOverallScore(double overallScore) {
        this.overallScore = overallScore;
    }

    /**
     * @return parsed text
     */
    public String getText() {
        return text;
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
        return processedText;
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
        return alternateText;
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
    public double getSpecificity() {
        return specificity;
    }

    /**
     * @param specificity
     */
    public void setSpecificity(double specificity) {
        this.specificity = specificity;
    }

    /**
     * @return
     */
    public double getCombinedScore() {
        return specificity * overallScore;
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

    /**
     * @return
     */
    public SentimentEntity getSentimentEntity() {
        return sentimentEntity;
    }

    /**
     * @param sentimentEntity
     */
    public void setSentimentEntity(SentimentEntity sentimentEntity) {
        this.sentimentEntity = sentimentEntity;
    }

}
