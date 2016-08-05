package webService.result;

import java.util.List;
import java.util.Map;

import data.discourse.SemanticCohesion;

public class ResultCv {
	
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
	
	private List<String> positiveWords;
	private List<String> negativeWords;
	private List<String> neutralWords;
	private double fanWeightedAverage;
	private Map<String, List<String>> liwcEmotions;
	private List<ResultKeyword> keywords;
	private double keywordsDocumentRelevance;
	
	public ResultCv() {
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

}
