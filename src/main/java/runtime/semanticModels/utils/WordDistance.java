package runtime.semanticModels.utils;

public class WordDistance implements Comparable<WordDistance>, java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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

	@Override
	public int compareTo(WordDistance otherDistance) {
		if(this.similarity < otherDistance.similarity) {
			return -1;
		}
		if(this.similarity > otherDistance.similarity) {
			return 1;
		}
		return 0;
	}
}