package services.discourse.cohesion;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import DAO.AbstractDocument;
import DAO.Block;
import DAO.Sentence;
import DAO.Word;
import DAO.sentiment.SentimentEntity;
import DAO.sentiment.SentimentValence;

/**
 * 
 * @author Mihai Dascalu
 */
public class SentimentAnalysis {
	static Logger logger = Logger.getLogger(CohesionGraph.class);

	public static void weightSemanticValences(AbstractDocument d) {
		logger.info("Weighting sentiment valences ...");		

		Iterator<Map.Entry<SentimentValence, Double>> it;
		
		Map<SentimentValence, Double> avgDoc = new HashMap<>();
		Map<SentimentValence, Double> sumWeightsDoc = new HashMap<>();
		// perform weighted sentiment per block and per document
		logger.info("[Weighting] I have " + d.getBlocks().size() + " blocks.");
		for (int i = 0; i < d.getBlocks().size(); i++) {
			Block b = d.getBlocks().get(i);
			if (b != null) {
				Map<SentimentValence, Double> avgBlock = new HashMap<>();
				Map<SentimentValence, Double> sumWeightsBlock = new HashMap<>();
				//Map<SentimentValence, Double> elemValences = b.getSentimentEntity().getAll();
				//double avgBlock = 0, sumWeightsBlock = 0;
				logger.info("[Weighting] Block " + b.getIndex() + " has " + b.getSentences().size() + " sentences."); 
				for (int j = 0; j < b.getSentences().size(); j++) {
					Sentence s = b.getSentences().get(j);
					it = s.getSentimentEntity().getAll().entrySet().iterator();
					logger.info("[Weighting] There are " + s.getSentimentEntity().getAll().size() + " sentiments set for this sentence.");
				    while (it.hasNext()) {
				        Map.Entry<SentimentValence, Double> pair = it.next();
				        SentimentValence sv = (SentimentValence)pair.getKey();
				        Double value = (Double)pair.getValue();
				        logger.info(" Sentence s (sentiment " + sv.getName() + " = " + value + ")");
				        if (value != -1) { // -1?
				        	avgBlock.put(sv, (avgBlock.get(sv) == null ? 0 : avgBlock.get(sv))
				        			+ b.getSentenceBlockDistances()[j].getCohesion()
									* value);
				        	sumWeightsBlock.put(sv, (sumWeightsBlock.get(sv) == null ? 0 : sumWeightsBlock.get(sv))
				        			+ b.getSentenceBlockDistances()[j].getCohesion());
				        }
				        it.remove(); // avoids a ConcurrentModificationException
				    }
				}
				it = b.getSentimentEntity().getAll().entrySet().iterator();
				while (it.hasNext()) {
			        Map.Entry pair = (Map.Entry)it.next();
			        SentimentValence sv = (SentimentValence)pair.getKey();
			        Double value = (Double)pair.getValue();
					if (sumWeightsBlock.get(sv) != 0) {
						avgBlock.put(sv, avgBlock.get(sv) / sumWeightsBlock.get(sv));
						//b.setSentimentEntity(new SentimentEntity(b.getProcessedText().trim(), avgBlock));
						SentimentEntity se = new SentimentEntity();
						se.add(sv, avgBlock.get(sv));
						avgDoc.put(sv, (avgDoc.get(sv) == null ? 0 : avgDoc.get(sv)) + avgBlock.get(sv) * d.getBlockDocDistances()[i].getCohesion());
						sumWeightsDoc.put(sv,  (sumWeightsDoc.get(sv) == null ? 0 : sumWeightsDoc.get(sv) + d.getBlockDocDistances()[i].getCohesion()));
						b.setSentimentEntity(new SentimentEntity());
						b.getSentimentEntity().add(sv, avgBlock.get(sv));
					}
					it.remove();
				}
			}
		}

		it = d.getSentimentEntity().getAll().entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
	        SentimentValence sv = (SentimentValence)pair.getKey();
	        Double value = (Double)pair.getValue();
			if (sumWeightsDoc.get(sv) != 0) {
				//d.setSentimentEntity(new SentimentEntity(d.getProcessedText().trim(), avgDoc / sumWeightsDoc));
				// TODO: do we really need to add parameters to SentimentEntity?
				SentimentEntity se = new SentimentEntity();
				se.add(sv, avgDoc.get(sv) / sumWeightsDoc.get(sv));
				d.setSentimentEntity(new SentimentEntity());
			}
		}
	}
}
