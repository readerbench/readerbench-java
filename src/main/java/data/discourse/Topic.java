/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package data.discourse;

import java.io.Serializable;

import data.AnalysisElement;
import data.Word;
import services.commons.VectorAlgebra;
import services.semanticModels.LDA.LDA;

/**
 * 
 * @author Mihai Dascalu
 */
public class Topic implements Comparable<Topic>, Serializable {
	private static final long serialVersionUID = -2955989168004509623L;

	private Word word;
	private double relevance;
	private double termFrequency;
	private double lsaSim;
	private double ldaSim;

	public Topic(Word word, AnalysisElement e) {
		this.word = word;
		updateRelevance(e);
	}

	public void updateRelevance(AnalysisElement e) {
		termFrequency = 1 + Math.log(e.getWordOccurences().get(word));
		// do not consider Idf in order to limit corpus specificity
		// double inverseDocumentFrequency = word.getIdf();
		if (e.getLDA() == null && e.getLSA() == null) {
			this.relevance = termFrequency;
			return;
		}

				// determine importance within analysis element
		lsaSim = VectorAlgebra.cosineSimilarity(word.getLSAVector(), e.getLSAVector());
		ldaSim = LDA.getSimilarity(word.getLDAProbDistribution(), e.getLDAProbDistribution());
		this.relevance = termFrequency * SemanticCohesion.getAggregatedSemanticMeasure(lsaSim, ldaSim);
	}

	public double getTermFrequency() {
		return termFrequency;
	}

	public double getLSASim() {
		return lsaSim;
	}

	public double getLDASim() {
		return ldaSim;
	}

	public Topic(Word word, double relevance) {
		super();
		this.word = word;
		this.relevance = relevance;
	}

	public Word getWord() {
		return word;
	}

	public void setWord(Word word) {
		this.word = word;
	}

	public double getRelevance() {
		return relevance;
	}

	public void setRelevance(double relevance) {
		this.relevance = relevance;
	}

	@Override
	public boolean equals(Object obj) {
		Topic t = (Topic) obj;
		return this.getWord().getStem().equals(t.getWord().getStem());
	}

	@Override
	public int compareTo(Topic o) {
		return (int) Math.signum(o.getRelevance() - this.getRelevance());
	}

	@Override
	public String toString() {
		return "{" + word + ", relevance=" + getRelevance() + "}";
	}
}
