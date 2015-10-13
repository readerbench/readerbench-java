package services.readingStrategies;

import java.awt.Color;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import services.commons.VectorAlgebra;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.WordNet.OntologySupport;
import DAO.AnalysisElement;
import DAO.Block;
import DAO.Sentence;
import DAO.Word;
import DAO.discourse.SemanticCohesion;

public class InferredKnowledgeStrategy {
	private static final Color COLOR_INFERRED_CONCEPTS = new Color(255, 102, 0);
	private static double SIMILARITY_THRESHOLD_KI = 0.33;
	private static final double INTER_WORD_SIMILARITY_WEIGHT = 2;
	private static final double WORD_DOCUMENT_SIMILARITY_WEIGHT = 1;

	private int addAssociations(Word word, AnalysisElement e, String usedColor, String annotationText) {
		word.getReadingStrategies()[ReadingStrategies.INFERRED_KNOWLEDGE] = true;
		int noOccurences = StringUtils.countMatches(" " + e.getAlternateText() + " ", " " + word.getText() + " ");
		e.setAlternateText(
				PatternMatching.colorTextStar(e.getAlternateText(), word.getText(), usedColor, annotationText));

		// recheck just to be sure
		noOccurences += StringUtils.countMatches(" " + e.getAlternateText() + " ", " " + word.getText() + " ");
		e.setAlternateText(
				PatternMatching.colorTextStar(e.getAlternateText(), word.getText(), usedColor, annotationText));
		if (noOccurences > 0)
			return 1;
		return 0;
	}

	public int getInferredConcepts(Block v, List<Sentence> sentences) {
		String usedColor = Integer.toHexString(COLOR_INFERRED_CONCEPTS.getRGB());
		usedColor = usedColor.substring(2, usedColor.length());

		int noOccur = 0;

		// determine vectors for collections of sentences
		String textSentences = "";
		double[] vectorSentences = new double[LSA.K];

		for (Sentence s : sentences) {
			for (int i = 0; i < LSA.K; i++) {
				vectorSentences[i] += s.getLSAVector()[i];
			}
			textSentences += s.getProcessedText() + " ";
		}
		double[] probDistribSentences = v.getLDA().getProbDistribution(textSentences);

		for (Word w1 : v.getWordOccurences().keySet()) {
			// only for words that have not been previously marked as
			// paraphrases and not previously identified as inferred concepts
			if (!w1.getReadingStrategies()[ReadingStrategies.PARAPHRASE]
					&& !w1.getReadingStrategies()[ReadingStrategies.INFERRED_KNOWLEDGE]) {
				// determine if alternative paraphrasing exists
				boolean hasAssociations = false;
				loopsentence: for (Sentence s : sentences) {
					for (Word w2 : s.getWordOccurences().keySet()) {
						// check for identical lemmas or synonyms
						if (w1.getLemma().equals(w2.getLemma())
								|| OntologySupport.areSynonyms(w1, w2, v.getLanguage())) {
							hasAssociations = true;
							break loopsentence;
						}
					}
				}
				// use only potential inferred concepts
				if (!hasAssociations) {
					// determine maximum likelihood
					double maxSim = 0;
					double[] probDistrib1 = w1.getLDAProbDistribution();
					double[] vector1 = v.getLSA().getWordVector(w1);
					Word maxSimWord = null;

					// add similarity to sentences as a measure of importance of
					// the
					// word
					double simLSASentences = VectorAlgebra.cosineSimilarity(vector1, vectorSentences);
					double simLDASentences = LDA.getSimilarity(probDistrib1, probDistribSentences);
					double simSentences = SemanticCohesion.getAggregatedSemanticMeasure(simLSASentences,
							simLDASentences);

					for (Sentence s : sentences) {
						for (Word w2 : s.getWordOccurences().keySet()) {
							// determine semantic proximity
							double simLSAWord1 = VectorAlgebra.cosineSimilarity(vector1, v.getLSA().getWordVector(w2));
							double simLDAWord1 = LDA.getSimilarity(probDistrib1, w2.getLDAProbDistribution());
							double simWMWord1 = OntologySupport.semanticSimilarity(w1, w2, OntologySupport.WU_PALMER);

							double simWord1 = SemanticCohesion.getCohesionMeasure(simWMWord1, simLSAWord1, simLDAWord1);

							// determine importance of counterpart word
							double simLSAWord2 = VectorAlgebra.cosineSimilarity(v.getLSA().getWordVector(w2),
									vectorSentences);
							double simLDAWord2 = LDA.getSimilarity(w2.getLDAProbDistribution(), probDistribSentences);
							double simWord2 = SemanticCohesion.getAggregatedSemanticMeasure(simLSAWord2, simLDAWord2);

							double sim = 1.0 / (INTER_WORD_SIMILARITY_WEIGHT + 2 * WORD_DOCUMENT_SIMILARITY_WEIGHT)
									* (INTER_WORD_SIMILARITY_WEIGHT * simWord1
											+ WORD_DOCUMENT_SIMILARITY_WEIGHT * simWord2
											+ WORD_DOCUMENT_SIMILARITY_WEIGHT * simSentences);
							if (maxSim < sim) {
								maxSim = sim;
								maxSimWord = w2;
							}
						}
					}

					if (maxSim >= SIMILARITY_THRESHOLD_KI) {
						noOccur += addAssociations(w1, v, usedColor, maxSimWord.getLemma());
					}
				}
			}
		}
		return noOccur;
	}

	public static void setSimilarityThresholdKI(double similarityThreshold) {
		SIMILARITY_THRESHOLD_KI = similarityThreshold;
	}
}
