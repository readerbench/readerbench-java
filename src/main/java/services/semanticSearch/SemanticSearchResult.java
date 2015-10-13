package services.semanticSearch;

import DAO.AbstractDocument;

public class SemanticSearchResult implements Comparable<SemanticSearchResult> {
	private AbstractDocument doc;
	private double relevance;

	public SemanticSearchResult(AbstractDocument doc, double relevance) {
		super();
		this.doc = doc;
		this.relevance = relevance;
	}

	public AbstractDocument getDoc() {
		return doc;
	}

	public void setDoc(AbstractDocument doc) {
		this.doc = doc;
	}

	public double getRelevance() {
		return relevance;
	}

	public void setRelevance(double relevance) {
		this.relevance = relevance;
	}

	@Override
	public int compareTo(SemanticSearchResult o) {
		return (int) Math.signum(o.getRelevance() - this.getRelevance());
	}

}
