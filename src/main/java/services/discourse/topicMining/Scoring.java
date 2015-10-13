package services.discourse.topicMining;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import DAO.AbstractDocument;
import DAO.Block;
import DAO.Sentence;
import DAO.Word;
import DAO.discourse.Topic;

/**
 * 
 * @author Mihai Dascalu
 */
public class Scoring {
	static Logger logger = Logger.getLogger(Scoring.class);

	private static double determineIndividualScore(Sentence s, AbstractDocument d) {
		if (s != null && !s.getWords().isEmpty()) {
			// determine cummulative word importance in terms of topics coverage
			double importance = 0;
			for (Word w : s.getWordOccurences().keySet()) {
				Topic t = new Topic(w, 0);
				int index = d.getTopics().indexOf(t);
				if (index >= 0) {
					Topic coveredTopic = d.getTopics().get(index);
					double tf = s.getWordOccurences().get(w);
					// double idf = s.getLSA().getMapIdf().get(w);
					importance += (1 + Math.log(tf)) * coveredTopic.getRelevance();
				}
			}
			return Math.log(1 + importance);
		}
		return 0;
	}

	public static void score(AbstractDocument d) {
		// custom build scores for blocks and for the document as a whole using
		// a bottom-up approach
		logger.info("Scoring document > blocks > sentences");
		// determinUtteranceIndividualScore(d.getTitle());

		for (int i = 0; i < d.getBlocks().size(); i++) {
			Block b = d.getBlocks().get(i);
			if (b != null) {
				// determine overall scores for each utterance
				for (int j = 0; j < b.getSentences().size(); j++) {
					Sentence s = b.getSentences().get(j);
					if (s != null) {
						s.setIndividualScore(
								determineIndividualScore(s, d) * b.getSentenceBlockDistances()[j].getCohesion()
										* d.getBlockDocDistances()[i].getCohesion());
						s.setOverallScore(s.getIndividualScore());
					}
				}
			}
		}

		// determine overall scores for each utterance in terms of
		// intra-block relations
		for (int i = 0; i < d.getBlocks().size(); i++) {
			Block b = d.getBlocks().get(i);
			if (b != null) {
				for (int j = 0; j < b.getSentences().size(); j++) {
					Sentence s = b.getSentences().get(j);
					if (s != null) {
						double sentenceScore = s.getIndividualScore();

						// add other related utterances' importances
						for (int k = 0; k < b.getSentences().size(); k++) {
							if (j != k && b.getPrunnedSentenceDistances()[j][k] != null) {
								sentenceScore += b.getSentences().get(k).getIndividualScore()
										* b.getPrunnedSentenceDistances()[j][k].getCohesion();
								// * b.getSentenceBlockDistances()[j]
								// .getCohesion();
								// * d.getBlockDocDistances()[i]
								// .getCohesion();
							}
						}
						s.setOverallScore(sentenceScore);
					}
				}

				// determine block individual score
				double blockScore = 0;
				// use weighted values
				for (int j = 0; j < b.getSentences().size(); j++) {
					Sentence s = b.getSentences().get(j);
					if (s != null) {
						blockScore += s.getOverallScore();
					}
				}
				b.setIndividualScore(blockScore);
				b.setOverallScore(b.getIndividualScore());
			}
		}

		// augment first and last sentence scores taking into
		// consideration the previous and the next cohesion metric
		ArrayList<Double> augmentations = new ArrayList<Double>(d.getBlocks().size());
		for (int i = 0; i < d.getBlocks().size(); i++)
			augmentations.add(0d);
		for (int i = 0; i < d.getBlocks().size(); i++) {
			Block b = d.getBlocks().get(i);
			if (b != null) {
				if (b.getPrevSentenceBlockDistance() != null & b.getSentences().size() > 0
						&& ((Block) b.getPrevSentenceBlockDistance().getDestination()).getSentences().size() > 0) {
					Block referredBlock = ((Block) b.getPrevSentenceBlockDistance().getDestination());
					Sentence firstBlockSentence = b.getSentences().get(0);

					double augmentation = referredBlock.getIndividualScore()
							* b.getPrevSentenceBlockDistance().getCohesion()
							* d.getBlockDocDistances()[i].getCohesion();
					augmentations.set(referredBlock.getIndex(),
							augmentations.get(referredBlock.getIndex()) + augmentation);
					firstBlockSentence.setOverallScore(firstBlockSentence.getOverallScore() + augmentation);
				}

				if (b.getNextSentenceBlockDistance() != null & b.getSentences().size() > 0
						&& ((Block) b.getNextSentenceBlockDistance().getSource()).getSentences().size() > 0) {
					Block referredBlock = ((Block) b.getNextSentenceBlockDistance().getSource());
					Sentence lastBlockSentence = b.getSentences().get(b.getSentences().size() - 1);
					double augmentation = referredBlock.getIndividualScore()
							* b.getNextSentenceBlockDistance().getCohesion()
							* d.getBlockDocDistances()[i].getCohesion();
					augmentations.set(referredBlock.getIndex(),
							augmentations.get(referredBlock.getIndex()) + augmentation);
					lastBlockSentence.setOverallScore(lastBlockSentence.getOverallScore() + augmentation);
				}
			}
		}
		for (int i = 0; i < augmentations.size(); i++) {
			Block b = d.getBlocks().get(i);
			if (b != null) {
				b.setOverallScore(b.getOverallScore() + augmentations.get(i));
			}
		}

		// for (int i = 0; i < d.getBlocks().size(); i++) {
		// Block b = d.getBlocks().get(i);
		// if (b != null) {
		// double oldScore = b.getOverallScore();
		// for (int j = 0; j < d.getBlocks().size(); j++) {
		// if (i != j
		// && d.getPrunnedBlockDistances()[i][j] != null
		// && d.getPrunnedBlockDistances()[i][j].getCohesion() > 0) {
		// b.setOverallScore(b.getOverallScore()
		// + d.getBlocks().get(j).getIndividualScore()
		// * d.getPrunnedBlockDistances()[i][j]
		// .getCohesion()); // *
		// // d.getBlockDocDistances()[i].getCohesion());
		// }
		// }
		//
		// double augmentationFactor = 1;
		// if (oldScore != 0)
		// augmentationFactor = b.getOverallScore() / oldScore;
		// // update all sentence scores with the corresponding
		// // augmentation
		// for (int j = 0; j < b.getSentences().size(); j++) {
		// Sentence s = b.getSentences().get(j);
		// s.setOverallScore(s.getOverallScore() * augmentationFactor);
		// }
		// // b.setOverallScore(b.getOverallScore()
		// // * d.getBlockDocDistances()[i].getCohesion());
		// }
		// }

		// determine document score
		double documentScore = 0;
		for (int i = 0; i < d.getBlocks().size(); i++) {
			Block b = d.getBlocks().get(i);
			if (b != null) {
				documentScore += b.getOverallScore();
			}
		}
		d.setOverallScore(documentScore);
	}
}
