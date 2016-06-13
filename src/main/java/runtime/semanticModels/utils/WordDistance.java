package runtime.semanticModels.utils;

public class WordDistance {
	private String word1;
	private String word2;
	private double similarity;

	public WordDistance(String word1, String word2, double similarity) {
		super();
		this.word1 = word1;
		this.word2 = word2;
		this.similarity = similarity;
	}

	public String getWord1() {
		return word1;
	}

	public String getWord2() {
		return word2;
	}

	public double getSimilarity() {
		return similarity;
	}
}