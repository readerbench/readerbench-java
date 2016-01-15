package view.widgets.article.utils;

import services.commons.Formatting;


public class TwoAuthorsDistanceContainer implements Comparable<TwoAuthorsDistanceContainer> {
	private SingleAuthorContainer firstAuthor;
	private SingleAuthorContainer secondAuthor;
	private double similarity;

	public TwoAuthorsDistanceContainer(SingleAuthorContainer firstAuthor, SingleAuthorContainer secondAuthor,
			double similarity) {
		super();
		this.firstAuthor = firstAuthor;
		this.secondAuthor = secondAuthor;
		this.similarity = similarity;
	}

	public SingleAuthorContainer getFirstAuthor() {
		return firstAuthor;
	}
	public SingleAuthorContainer getSecondAuthor() {
		return secondAuthor;
	}
	public double getSimilarity() {
		return this.similarity;
	}
	
	@Override
	public int compareTo(TwoAuthorsDistanceContainer o) {
		return (new Double(o.getSimilarity())).compareTo(new Double (this.getSimilarity()));
		
	}
	@Override
	public boolean equals(Object obj) {
		if (this == null || obj == null)
			return false;
		TwoAuthorsDistanceContainer o = (TwoAuthorsDistanceContainer) obj;
		return 
				(this.getFirstAuthor().isSameAuthor(o.getFirstAuthor().getAuthor()) && this.getSecondAuthor().isSameAuthor(o.getSecondAuthor().getAuthor())) ||
				(this.getFirstAuthor().isSameAuthor(o.getSecondAuthor().getAuthor()) && this.getSecondAuthor().isSameAuthor(o.getFirstAuthor().getAuthor()));
	}

	@Override
	public String toString() {
		return Formatting.formatNumber(this.getSimilarity()) + ":\n\t- "
				+ this.getFirstAuthor().getAuthor().getAuthorName() + "\n\t- "
				+ this.getSecondAuthor().getAuthor().getAuthorName() + "\n";
	}
}