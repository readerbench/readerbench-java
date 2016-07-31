package services.semanticModels.utils;

public class WordSimilarity implements java.io.Serializable, Comparable<WordSimilarity> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String wordLemma;
	private double similarity;

	public WordSimilarity(String wordLemma, double similarity) {
		super();
		this.wordLemma = wordLemma;
		this.similarity = similarity;
	}

	public String getWordLemma() {
		return wordLemma;
	}

	public double getSimilarity() {
		return similarity;
	}

	@Override
	public int compareTo(WordSimilarity otherWordSimilarity) {
		return (int)Math.signum(otherWordSimilarity.similarity - this.similarity);
	}
}