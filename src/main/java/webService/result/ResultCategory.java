package webService.result;

import java.util.Comparator;

public class ResultCategory implements Comparable<ResultCategory> {

	private String name;
	private double relevance;

	public ResultCategory(String name, double relevance) {
		this.name = name;
		this.relevance = relevance;
	}

	public String getName() {
		return name;
	}

	public double getRelevance() {
		return relevance;
	}

	@Override
	public int compareTo(ResultCategory o) {
		// Reverse order
		return (int) Math.signum(this.getRelevance() - o.getRelevance());
	}

	public static Comparator<ResultCategory> ResultCategoryRelevanceComparator = new Comparator<ResultCategory>() {

		public int compare(ResultCategory o1, ResultCategory o2) {
			// descending order
			return o2.compareTo(o1);
		}

	};

}
