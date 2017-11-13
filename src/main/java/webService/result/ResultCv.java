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
import java.util.logging.Logger;

public class ResultCv extends Result {

    private String text;
    private String processedText;
    private Map<String, String> socialNetworksLinksFound;
    
    private List<String> feedback;
    private ResultTopic concepts;
    private Map<String, Integer> wordOccurences;
    private List<ResultTextualComplexity> textualComplexity;
    private Integer images;
    private double avgImagesPerPage;
    private Integer colors;
    private Integer pages;

    private Integer paragraphs;
    private Integer sentences;
    private Integer words;
    private Integer contentWords;

    private Integer fontTypes; // contains font types with text styles
    private Integer fontTypesSimple; // contains font types withouth text styles
    private Integer fontSizes;
    private Double minFontSize;
    private Double maxFontSize;
    private Integer totalCharacters;
    private Integer boldCharacters;
    private Double boldCharsCoverage;
    private Integer italicCharacters;
    private Double italicCharsCoverage;
    private Integer boldItalicCharacters;
    private Double boldItalicCharsCoverage;

    private List<String> positiveWords;
    private List<String> negativeWords;
    private List<String> neutralWords;
    private double fanWeightedAverage;
    private Map<String, List<String>> liwcEmotions;
    private List<ResultKeyword> keywords;
    private double keywordsDocumentRelevance;
    private Map<String, Double> keywordsDocumentSimilarity;
    
    public static Logger logger = Logger.getLogger("");

    public ResultCv() {
        super();
        feedback = new ArrayList<>();
    }

    public ResultCv(
            // topic extraction
            ResultTopic concepts,
            // basic word count
            Map<String, Integer> wordOccurences,
            // textual complexity
            List<ResultTextualComplexity> textualComplexity,
            // number of images
            Integer images,
            // average images per page
            double avgImagesPerPage,
            // number of colors
            Integer colors,
            // average colors per page
            double avgColorsPerPage,
            // number of pages
            Integer pages,
            // number of paragraphs
            Integer paragraphs,
            // number of sentences
            Integer sentences,
            // number of words
            Integer words,
            // number of content words
            Integer contentWords,
            // volumetric statistics
            // TODO: add this parameter

            // inferred topics
            // TODO: add this parameter

            // FAN positive, negative words and neutral words
            List<String> positiveWords, List<String> negativeWords, List<String> neutralWords,
            // LIWC emotions
            Map<String, List<String>> liwcEmotions,
            // FAN weighted average
            double fanWeightedAverage,
            // specific keywords
            List<ResultKeyword> keywords,
            // (specific keywords, document) relevance
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
        this.paragraphs = paragraphs;
        this.sentences = sentences;
        this.words = words;
        this.contentWords = contentWords;
        this.positiveWords = positiveWords;
        this.negativeWords = negativeWords;
        this.neutralWords = neutralWords;
        this.fanWeightedAverage = fanWeightedAverage;
        this.liwcEmotions = liwcEmotions;
        this.keywords = keywords;
        this.keywordsDocumentRelevance = keywordsDocumentRelevance;
        this.feedback = new ArrayList<>();
        this.keywordsDocumentSimilarity = new HashMap<>();
    }
    
    public void addFeedback(List<String> fb) {
        fb.forEach((error) -> {
            logger.info("Incerc sa adaug " + error);
            feedback.add(error);
        });
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getProcessedText() {
        return processedText;
    }

    public Map<String, String> getSocialNetworksLinksFound() {
        return socialNetworksLinksFound;
    }

    public void setSocialNetworksLinksFound(Map<String, String> socialNetworksLinksFound) {
        this.socialNetworksLinksFound = socialNetworksLinksFound;
    }
    
    public void setProcessedText(String processedText) {
        this.processedText = processedText;
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

    public Integer getParagraphs() {
        return paragraphs;
    }

    public void setParagraphs(Integer paragraphs) {
        this.paragraphs = paragraphs;
    }

    public Integer getSentences() {
        return sentences;
    }

    public void setSentences(Integer sentences) {
        this.sentences = sentences;
    }

    public Integer getWords() {
        return words;
    }

    public void setWords(Integer words) {
        this.words = words;
    }

    public Integer getContentWords() {
        return contentWords;
    }

    public void setContentWords(Integer contentWords) {
        this.contentWords = contentWords;
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

    public List<String> getNeutralWords() {
        return neutralWords;
    }

    public void setNeutralWords(List<String> neutralWords) {
        this.neutralWords = neutralWords;
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

    public Integer getBoldCharacters() {
        return boldCharacters;
    }

    public void setBoldCharacters(Integer boldCharacters) {
        this.boldCharacters = boldCharacters;
    }

    public Double getBoldCharsCoverage() {
        return boldCharsCoverage;
    }

    public void setBoldCharsCoverage(Double boldCharsCoverage) {
        this.boldCharsCoverage = boldCharsCoverage;
    }

    public Integer getItalicCharacters() {
        return italicCharacters;
    }

    public void setItalicCharacters(Integer italicCharacters) {
        this.italicCharacters = italicCharacters;
    }
    
    public Double getItalicCharsCoverage() {
        return italicCharsCoverage;
    }
    
    public void setItalicCharsCoverage(Double italicCharsCoverage) {
        this.italicCharsCoverage = italicCharsCoverage;
    }

    public Integer getBoldItalicCharacters() {
        return boldItalicCharacters;
    }

    public void setBoldItalicCharacters(Integer boldItalicCharacters) {
        this.boldItalicCharacters = boldItalicCharacters;
    }
    
    public Double getBoldItalicCharsCoverage() {
        return boldItalicCharsCoverage;
    }
    
    public void setBoldItalicCharsCoverage(Double boldItalicCharsCoverage) {
        this.boldItalicCharsCoverage = boldItalicCharsCoverage;
    }

    public Map<String, Double> getKeywordsDocumentSimilarity() {
        return keywordsDocumentSimilarity;
    }

    public void setKeywordsDocumentSimilarity(Map<String, Double> keywordsDocumentSimilarity) {
        this.keywordsDocumentSimilarity = keywordsDocumentSimilarity;
    }

}
