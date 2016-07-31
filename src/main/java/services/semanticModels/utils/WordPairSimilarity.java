package services.semanticModels.utils;

public class WordPairSimilarity {
	private String word1;
	private String word2;
	private double similarity;
	public WordPairSimilarity(String word1, String word2, double similarity) {
		this.word1 = word1;
		this.word2 = word2;
		this.similarity = similarity;
	}
	
	public double getSimilarity() {
		return this.similarity;
	}
	public String getWord1() {
		return this.word1;
	}
	public String getWord2() {
		return this.word2;
	}
}