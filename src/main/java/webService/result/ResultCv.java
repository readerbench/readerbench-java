package webService.result;

import java.util.List;
import java.util.Map;

public class ResultCv {
	
	private ResultTopic concepts;
	private Map<String, Integer> wordOccurences;
	private List<ResultTextualComplexity> textualComplexity;
	private Integer images;
	private Integer colors;
	private Integer pages;
	private Integer paragraphs;
	private Integer sentences;
	private Integer words;
	private Integer contentWords;
	private List<String> positiveWords;
	private List<String> negativeWords;
	private Map<String, List<String>> liwcEmotions;

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

			// number of colors
			Integer colors,

			// number of pages
			Integer pages,

			Integer paragraphs,

			Integer sentences,

			Integer words,

			Integer contentWords,

			// volumetric statistics
			// TODO: add this parameter

			// inferred topics
			// TODO: add this parameter

			// FAN positive & negative words
			List<String> positiveWords, List<String> negativeWords,

			// LIWC emotions
			Map<String, List<String>> liwcEmotions

	) {

		this.concepts = concepts;
		this.wordOccurences = wordOccurences;
		this.textualComplexity = textualComplexity;
		this.images = images;
		this.colors = colors;
		this.pages = pages;
		this.paragraphs = paragraphs;
		this.sentences = sentences;
		this.words = words;
		this.contentWords = contentWords;
		this.positiveWords = positiveWords;
		this.negativeWords = negativeWords;
		this.liwcEmotions = liwcEmotions;
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

}
