package webService.result;

import java.util.List;
import java.util.Map;

public class ResultCv {
	
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
			
			// volumetric statistics
			// TODO: add this parameter
			
			// inferred topics
			// TODO: add this parameter
			
			// FAN positive & negative words
			List<String> positiveWords,
			List<String> negativeWords,
			
			// LIWC emotions
			Map<String, List<String>> liwcEmotions
			
	) {
		
		this.concepts = concepts;
		this.wordOccurences = wordOccurences;
		this.textualComplexity = textualComplexity;
		this.images = images;
		this.colors = colors;
		this.pages = pages;
		this.positiveWords = positiveWords;
		this.negativeWords = negativeWords;
		this.liwcEmotions = liwcEmotions;
		
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

	private ResultTopic concepts;
	private Map<String, Integer> wordOccurences;
	private List<ResultTextualComplexity> textualComplexity;
	private Integer images;
	private Integer colors;
	private Integer pages;
	private List<String> positiveWords;
	private List<String> negativeWords;
	private Map<String, List<String>> liwcEmotions;

}
