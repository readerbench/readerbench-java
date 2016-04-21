package services.discourse.topicMining;

import org.apache.log4j.Logger;

import data.AbstractDocument;
import data.Block;
import data.Sentence;
import data.Word;
import data.discourse.Topic;

/**
 * 
 * @author Mihai Dascalu
 */
public class Scoring {
	static Logger logger = Logger.getLogger(Scoring.class);

	private static double determineIndividualScore(Sentence s, AbstractDocument d) {
		if (s != null && !s.getWords().isEmpty()) {
			// determine cumulative word importance in terms of topics coverage
			double importance = 0;
			for (Word w : s.getWordOccurences().keySet()) {
				Topic t = new Topic(w, 0);
				int index = d.getTopics().indexOf(t);
				if (index >= 0) {
					Topic coveredTopic = d.getTopics().get(index);
					double tf = s.getWordOccurences().get(w);
					importance += (1 + Math.log(tf)) * coveredTopic.getRelevance();
				}
			}
			return importance;
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

						// add other related sentences' importance
						for (int k = 0; k < b.getSentences().size(); k++) {
							if (j != k && b.getPrunnedSentenceDistances()[j][k] != null) {
								sentenceScore += b.getSentences().get(k).getIndividualScore()
										* b.getPrunnedSentenceDistances()[j][k].getCohesion();
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
			}
		}

		// determine overall scores for each block in terms of
		// inter-block relations
		double[] blockScoreAugmentation = new double[d.getBlocks().size()];
		for (int i = 0; i < d.getBlocks().size(); i++) {
			for (int j = 0; j < d.getBlocks().size(); j++) {
				if (i != j && d.getBlocks().get(i) != null && d.getBlocks().get(j) != null
						&& d.getPrunnedBlockDistances()[j][i] != null) {
					blockScoreAugmentation[i] += d.getBlocks().get(j).getIndividualScore()
							* d.getPrunnedBlockDistances()[j][i].getCohesion();
				}
			}
		}
		for (int i = 0; i < d.getBlocks().size(); i++) {
			Block b = d.getBlocks().get(i);
			if (b != null) {
				b.setOverallScore(b.getIndividualScore() + blockScoreAugmentation[i]);
			}
		}

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
