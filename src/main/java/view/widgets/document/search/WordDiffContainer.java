package view.widgets.document.search;

import java.util.List;

import data.Word;

public class WordDiffContainer implements Comparable<WordDiffContainer> {
	private Word wRef;
	private Word wSim;
	private double similarity;

	public WordDiffContainer(Word wRef, Word wSim, double similarity) {
		this.wRef = wRef;
		this.wSim = wSim;
		this.similarity = similarity;
	}

	public static double getScore(List<WordDiffContainer> l, Word wRef, Word wSim) {
		for (WordDiffContainer c : l) {
			if (c.getWRef().equals(wRef) && c.getWSim().equals(wSim))
				return c.getSimilarity();
		}
		return 0.0;
	}

	@Override
	public int compareTo(WordDiffContainer o) {
		return new Double(o.getSimilarity()).compareTo(new Double(this.getSimilarity()));
	}

	public Word getWRef() {
		return wRef;
	}

	public void setWRef(Word wRef) {
		this.wRef = wRef;
	}

	public Word getWSim() {
		return wSim;
	}

	public void setWSim(Word wSim) {
		this.wSim = wSim;
	}

	public double getSimilarity() {
		return similarity;
	}

	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}

}