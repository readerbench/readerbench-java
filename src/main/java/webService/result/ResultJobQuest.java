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
package webService.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResultJobQuest extends Result {

    private Map<String, String> socialNetworksLinksFound;
    
    private List<String> feedback;
    private ResultTopic concepts;
    private Map<String, Integer> wordOccurences;
    private List<ResultTextualComplexity> textualComplexity;
    private Integer images;
    private double avgImagesPerPage;
    private Integer colors;
    private Integer pages;

    private Integer words;

    private Integer fontTypes; // contains font types with text styles
    private Integer fontTypesSimple; // contains font types withouth text styles
    private Integer fontSizes;
    private Double minFontSize;
    private Double maxFontSize;
    private Integer totalCharacters;
    
    private List<String> veryPositiveWords;
    private List<String> positiveWords;
    private List<String> negativeWords;
    private List<String> veryNegativeWords;
    
    private double fanWeightedAverage;
    private Map<String, List<String>> liwcEmotions;
    private List<ResultKeyword> keywords;
    private double keywordsDocumentRelevance;
    private Map<String, Double> keywordsDocumentSimilarity;
    
    private double scoreGlobal;
    private double scoreVisual;
    private double scoreContent;
    
    public static final Logger LOGGER = Logger.getLogger("");

    public ResultJobQuest() {
        super();
        feedback = new ArrayList<>();
    }

    public ResultJobQuest(
            ResultTopic concepts,
            Map<String, Integer> wordOccurences,
            List<ResultTextualComplexity> textualComplexity,
            Integer images,
            double avgImagesPerPage,
            Integer colors,
            double avgColorsPerPage,
            Integer pages,
            Integer words,
            List<String> positiveWords, List<String> negativeWords,
            Map<String, List<String>> liwcEmotions,
            double fanWeightedAverage,
            List<ResultKeyword> keywords,
            double keywordsDocumentRelevance
    ) {
        super();
        this.concepts = concepts;
        this.wordOccurences = wordOccurences;
        this.textualComplexity = textualComplexity;
        this.images = images;
        this.avgImagesPerPage = avgImagesPerPage;
        this.colors = colors;
        this.pages = pages;
        this.words = words;
        this.positiveWords = positiveWords;
        this.negativeWords = negativeWords;
        this.fanWeightedAverage = fanWeightedAverage;
        this.liwcEmotions = liwcEmotions;
        this.keywords = keywords;
        this.keywordsDocumentRelevance = keywordsDocumentRelevance;
        this.feedback = new ArrayList<>();
        this.keywordsDocumentSimilarity = new HashMap<>();
    }
    
    public void addFeedback(List<String> fb) {
        fb.forEach((error) -> {
            LOGGER.log(Level.INFO, "Incerc sa adaug {0}", error);
            feedback.add(error);
        });
    }

    public Map<String, String> getSocialNetworksLinksFound() {
        return socialNetworksLinksFound;
    }

    public void setSocialNetworksLinksFound(Map<String, String> socialNetworksLinksFound) {
        this.socialNetworksLinksFound = socialNetworksLinksFound;
    }
    
    public List<ResultKeyword> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<ResultKeyword> keywords) {
        this.keywords = keywords;
    }

    public double getKeywordsDocumentRelevance() {
        return keywordsDocumentRelevance;
    }

    public void setKeywordsDocumentRelevance(double keywordsDocumentRelevance) {
        this.keywordsDocumentRelevance = keywordsDocumentRelevance;
    }

    public Integer getWords() {
        return words;
    }

    public void setWords(Integer words) {
        this.words = words;
    }

    public double getFanWeightedAverage() {
        return fanWeightedAverage;
    }

    public void setFanWeightedAverage(double fanWeightedAverage) {
        this.fanWeightedAverage = fanWeightedAverage;
    }

    public Map<String, List<String>> getLiwcEmotions() {
        return liwcEmotions;
    }

    public void setLiwcEmotions(Map<String, List<String>> liwcEmotions) {
        this.liwcEmotions = liwcEmotions;
    }

    public ResultTopic getConcepts() {
        return concepts;
    }

    public void setConcepts(ResultTopic concepts) {
        this.concepts = concepts;
    }

    public Map<String, Integer> getWordOccurences() {
        return wordOccurences;
    }

    public void setWordOccurences(Map<String, Integer> wordOccurences) {
        this.wordOccurences = wordOccurences;
    }

    public List<ResultTextualComplexity> getTextualComplexity() {
        return textualComplexity;
    }

    public void setTextualComplexity(List<ResultTextualComplexity> textualComplexity) {
        this.textualComplexity = textualComplexity;
    }

    public Integer getImages() {
        return images;
    }

    public void setImages(Integer images) {
        this.images = images;
    }

    public Integer getColors() {
        return colors;
    }

    public void setColors(Integer colors) {
        this.colors = colors;
    }

    public double getAvgImagesPerPage() {
        return avgImagesPerPage;
    }

    public void setAvgImagesPerPage(double avgImagesPerPage) {
        this.avgImagesPerPage = avgImagesPerPage;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }
    
    public List<String> getVeryPositiveWords() {
        return veryPositiveWords;
    }

    public void setVeryPositiveWords(List<String> veryPositiveWords) {
        this.veryPositiveWords = veryPositiveWords;
    }    

    public List<String> getPositiveWords() {
        return positiveWords;
    }

    public void setPositiveWords(List<String> positiveWords) {
        this.positiveWords = positiveWords;
    }

    public List<String> getNegativeWords() {
        return negativeWords;
    }

    public void setNegativeWords(List<String> negativeWords) {
        this.negativeWords = negativeWords;
    }
    
    public List<String> getVeryNegativeWords() {
        return veryNegativeWords;
    }

    public void setVeryNegativeWords(List<String> veryNegativeWords) {
        this.veryNegativeWords = veryNegativeWords;
    }

    public Integer getFontTypes() {
        return fontTypes;
    }

    public void setFontTypes(Integer fontTypes) {
        this.fontTypes = fontTypes;
    }

    public Integer getFontTypesSimple() {
        return fontTypesSimple;
    }

    public void setFontTypesSimple(Integer fontTypesSimple) {
        this.fontTypesSimple = fontTypesSimple;
    }

    public Integer getFontSizes() {
        return fontSizes;
    }

    public void setFontSizes(Integer fontSizes) {
        this.fontSizes = fontSizes;
    }

    public Double getMinFontSize() {
        return minFontSize;
    }

    public void setMinFontSize(Double minFontSize) {
        this.minFontSize = minFontSize;
    }

    public Double getMaxFontSize() {
        return maxFontSize;
    }

    public void setMaxFontSize(Double maxFontSize) {
        this.maxFontSize = maxFontSize;
    }

    public Integer getTotalCharacters() {
        return totalCharacters;
    }

    public void setTotalCharacters(Integer totalCharacters) {
        this.totalCharacters = totalCharacters;
    }

    public Map<String, Double> getKeywordsDocumentSimilarity() {
        return keywordsDocumentSimilarity;
    }

    public void setKeywordsDocumentSimilarity(Map<String, Double> keywordsDocumentSimilarity) {
        this.keywordsDocumentSimilarity = keywordsDocumentSimilarity;
    }

    public double getScoreGlobal() {
        return scoreGlobal;
    }
    
    public void setScoreGlobal(double scoreGlobal) {
        this.scoreGlobal = scoreGlobal;
    }

    public double getScoreVisual() {
        return scoreVisual;
    }

    public void setScoreVisual(double scoreVisual) {
        this.scoreVisual = scoreVisual;
    }
    
    public double getScoreContent() {
        return scoreContent;
    }

    public void setScoreContent(double scoreContent) {
        this.scoreContent = scoreContent;
    }
    
}
