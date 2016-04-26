package webService.result;

import java.util.Comparator;

public class ResultKeyword implements Comparable<ResultKeyword> {

	private String name;
	private int noOccurences;
	private double relevance;

	public ResultKeyword(String name, int noOccurences, double relevance) {
		this.name = name;
		this.noOccurences = noOccurences;
		this.relevance = relevance;
	}

	public String getName() {
		return name;
	}

	public int getNoOccurences() {
		return noOccurences;
	}

	public double getRelevance() {
		return relevance;
	}

	@Override
	public int compareTo(ResultKeyword o) {
		// Reverse order
		return (int) Math.signum(this.getRelevance() - o.getRelevance());
	}

	public static Comparator<ResultKeyword> ResultKeywordRelevanceComparator = new Comparator<ResultKeyword>() {

		public int compare(ResultKeyword o1, ResultKeyword o2) {
			// descending order
			return o2.compareTo(o1);
		}

	};

}