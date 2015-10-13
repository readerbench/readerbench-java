package services.discourse.cohesion;

import org.apache.log4j.Logger;

import DAO.AbstractDocument;
import DAO.Block;
import DAO.Sentence;
import DAO.sentiment.SentimentEntity;

/**
 * 
 * @author Mihai Dascalu
 */
public class SentimentAnalysis {
	static Logger logger = Logger.getLogger(CohesionGraph.class);

	public static void weightSemanticValences(AbstractDocument d) {
		logger.info("Weighting sentiment valences ...");

		double avgDoc = 0, sumWeightsDoc = 0;
		// perform weighted sentiment per block and per document
		for (int i = 0; i < d.getBlocks().size(); i++) {
			Block b = d.getBlocks().get(i);
			if (b != null) {
				double avgBlock = 0, sumWeightsBlock = 0;
				for (int j = 0; j < b.getSentences().size(); j++) {
					Sentence s = b.getSentences().get(j);
					if (s.getSentimentEntity().getSentimentValue() != -1) {
						avgBlock += b.getSentenceBlockDistances()[j].getCohesion()
								* s.getSentimentEntity().getSentimentValue();
						sumWeightsBlock += b.getSentenceBlockDistances()[j].getCohesion();
					}
				}
				if (sumWeightsBlock != 0) {
					avgBlock /= sumWeightsBlock;
					b.setSentimentEntity(new SentimentEntity(b.getProcessedText().trim(), avgBlock));
					avgDoc += avgBlock * d.getBlockDocDistances()[i].getCohesion();
					sumWeightsDoc += d.getBlockDocDistances()[i].getCohesion();
				}
			}
		}

		if (sumWeightsDoc != 0) {
			d.setSentimentEntity(new SentimentEntity(d.getProcessedText().trim(), avgDoc / sumWeightsDoc));
		}
	}
}
