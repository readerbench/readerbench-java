package services.discourse.cohesion;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import data.AbstractDocument;
import data.Block;
import data.Sentence;
import data.sentiment.SentimentEntity;
import data.sentiment.SentimentValence;

/**
 * 
 * @author Mihai Dascalu
 */
public class SentimentAnalysis {
	static Logger logger = Logger.getLogger(CohesionGraph.class);

	public static void weightSemanticValences(AbstractDocument d) {
		logger.info("Weighting sentiment valences ...");

		// initialize sentiment valence map for document
		SentimentEntity se = new SentimentEntity();
		Iterator itValenceMap = data.sentiment.SentimentValence.getValenceMap().entrySet().iterator();
		// logger.info("Valence map has " +
		// DAO.sentiment.SentimentValence.getValenceMap().size() + "
		// sentiments.");
		while (itValenceMap.hasNext()) {
			Map.Entry pair = (Map.Entry) itValenceMap.next();
			data.sentiment.SentimentValence daoSe = (data.sentiment.SentimentValence) pair.getValue();
			se.add(daoSe, 0.0);
		}
		d.setSentimentEntity(se);

		// initialize sentiment valence map for blocks
		for (int i = 0; i < d.getBlocks().size(); i++) {
			Block b = d.getBlocks().get(i);
			if (b != null) {
				se = new SentimentEntity();
				itValenceMap = data.sentiment.SentimentValence.getValenceMap().entrySet().iterator();
				// logger.info("Should add " +
				// DAO.sentiment.SentimentValence.getValenceMap().size() + "
				// sentiments to block " + b.getIndex());
				while (itValenceMap.hasNext()) {
					Map.Entry pair = (Map.Entry) itValenceMap.next();
					data.sentiment.SentimentValence daoSe = (data.sentiment.SentimentValence) pair.getValue();
					se.add(daoSe, 0.0);
				}
				b.setSentimentEntity(se);
				// logger.info("Block " + b.getIndex() + " has " +
				// b.getSentimentEntity().getAll().size() + " initialized
				// sentiments now");
			}
		}

		Map<SentimentValence, Double> avgDoc = new HashMap<>();
		Map<SentimentValence, Double> sumWeightsDoc = new HashMap<>();
		// perform weighted sentiment per block and per document

		// logger.info("[Weighting] I have " + d.getBlocks().size() + "
		// blocks.");
		for (int i = 0; i < d.getBlocks().size(); i++) {
			Block b = d.getBlocks().get(i);
			if (b != null) {
				Map<SentimentValence, Double> avgBlock = new HashMap<>();
				Map<SentimentValence, Double> sumWeightsBlock = new HashMap<>();
				// Map<SentimentValence, Double> elemValences =
				// b.getSentimentEntity().getAll();
				// double avgBlock = 0, sumWeightsBlock = 0;
				// logger.info("[Weighting] Block " + b.getIndex() + " has " +
				// b.getSentences().size() + " sentences.");
				for (int j = 0; j < b.getSentences().size(); j++) {
					Sentence s = b.getSentences().get(j);
					// logger.info("[Weighting] There are " +
					// s.getSentimentEntity().getAll().size() + " sentiments set
					// for this sentence.");
					for (Map.Entry<SentimentValence, Double> pair : s.getSentimentEntity().getAll().entrySet()) {
						SentimentValence sv = pair.getKey();
						Double value = pair.getValue();
						// logger.info(" Sentence s (sentiment " + sv.getName()
						// + " = " + value + ")");
						if (value != null) {
							avgBlock.put(sv, (avgBlock.get(sv) == null ? 0 : avgBlock.get(sv))
									+ b.getSentenceBlockDistances()[j].getCohesion() * value);
							sumWeightsBlock.put(sv, (sumWeightsBlock.get(sv) == null ? 0 : sumWeightsBlock.get(sv))
									+ b.getSentenceBlockDistances()[j].getCohesion());
						}
					}
				}

				// updating sentiment values for block
				// logger.info("Updating sentiment valences for current block.
				// Block has " + b.getSentimentEntity().getAll().size() + "
				// sentiments.");
				for (Map.Entry<SentimentValence, Double> pair : b.getSentimentEntity().getAll().entrySet()) {
					SentimentValence sv = pair.getKey();
					Double value = pair.getValue();
					if (sumWeightsBlock.get(sv) != null) {
						avgBlock.put(sv, avgBlock.get(sv) / sumWeightsBlock.get(sv));
						// b.setSentimentEntity(new
						// SentimentEntity(b.getProcessedText().trim(),
						// avgBlock));
						avgDoc.put(sv, (avgDoc.get(sv) == null ? 0 : avgDoc.get(sv))
								+ avgBlock.get(sv) * d.getBlockDocDistances()[i].getCohesion());
						sumWeightsDoc.put(sv, (sumWeightsDoc.get(sv) == null ? 0 : sumWeightsDoc.get(sv))
								+ d.getBlockDocDistances()[i].getCohesion());
						// logger.info("Adding sentiment " + sv.getIndexLabel()
						// + " to block " + b.getIndex());
						b.getSentimentEntity().add(sv, avgBlock.get(sv));
					}
				}
			}
		}

		for (Map.Entry<SentimentValence, Double> pair : d.getSentimentEntity().getAll().entrySet()) {
			SentimentValence sv = pair.getKey();
			Double value = pair.getValue();
			if (sumWeightsDoc.get(sv) != null) {
				d.getSentimentEntity().add(sv, avgDoc.get(sv) / sumWeightsDoc.get(sv));
				// logger.info("Adding sentiment " + sv.getIndexLabel() + " to
				// document ");
			}
		}
	}
}
