/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO.discourse;

import java.io.Serializable;

import edu.cmu.lti.jawjaw.pobj.Lang;
import services.commons.VectorAlgebra;
import services.semanticModels.LDA.LDA;
import DAO.AnalysisElement;
import DAO.AbstractDocument;
import DAO.Word;

/**
 * 
 * @author Mihai Dascalu
 */
public class Topic implements Comparable<Topic>, Serializable {
	private static final long serialVersionUID = -2955989168004509623L;

	private Word word;
	private double relevance;

	public Topic(Word word, AnalysisElement e, AbstractDocument document) {
		this.word = word;
		updateRelevance(e, document);
	}

	public void updateRelevance(AnalysisElement e, AbstractDocument document) {
		double termFrequency = 1 + Math.log(e.getWordOccurences().get(word));

		// double inverseDocumentFrequency = word.getIdf(); //eliminated Idf in
		// order to limit corpus specificity
		double lsa, lda;

		if (e.getLDA() == null && e.getLSA() == null) {
			this.relevance = termFrequency;
			return;
		}

		double[] probDistrib = word.getLDAProbDistribution();

		// determine importance within semantic chain
		double relevanceSemanticChain = 0;

		if (e.getLanguage().equals(Lang.eng) || e.getLanguage().equals(Lang.fr)) {
			SemanticChain semanticChain = word.getSemanticChain();
			double semanticChainLength = 0;
			if (semanticChain != null && semanticChain.getWords().size() > 0) {
				semanticChainLength = 1 + Math.log(semanticChain.getWords()
						.size());
				lsa = VectorAlgebra.cosineSimilarity(word.getLSAVector(),
						semanticChain.getLSAVector());
				lda = LDA.getSimilarity(probDistrib,
						semanticChain.getLDAProbDistribution());
				relevanceSemanticChain = termFrequency
						* SemanticCohesion.getAggregatedSemanticMeasure(lsa,
								lda);

				// use also semantic chain proximity to the overall document
				lsa = VectorAlgebra.cosineSimilarity(
						semanticChain.getLSAVector(), document.getLSAVector());
				lda = LDA.getSimilarity(semanticChain.getLDAProbDistribution(),
						document.getLDAProbDistribution());
				relevanceSemanticChain *= semanticChainLength
						* SemanticCohesion.getAggregatedSemanticMeasure(lsa,
								lda);
			}
		}

		// determine importance within analysis element
		lsa = VectorAlgebra.cosineSimilarity(word.getLSAVector(),
				e.getLSAVector());
		lda = LDA.getSimilarity(probDistrib, e.getLDAProbDistribution());
		double relevanceAnalysisElement = termFrequency
				* SemanticCohesion.getAggregatedSemanticMeasure(lsa, lda);

		// determine importance within the document
		if (document.getWordOccurences().get(word) == null)
			termFrequency = 0;
		else
			termFrequency = 1 + Math
					.log(document.getWordOccurences().get(word));
		lsa = VectorAlgebra.cosineSimilarity(word.getLSAVector(),
				document.getLSAVector());
		lda = LDA.getSimilarity(probDistrib, document.getLDAProbDistribution());
		double relevanceDocument = termFrequency
				* SemanticCohesion.getAggregatedSemanticMeasure(lsa, lda);

		this.relevance += relevanceSemanticChain + relevanceAnalysisElement
				+ relevanceDocument;
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
