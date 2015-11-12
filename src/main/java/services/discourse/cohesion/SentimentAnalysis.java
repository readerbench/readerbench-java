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
		
		// initialize sentiment valence map for document
		SentimentEntity se = new SentimentEntity();
		Iterator itValenceMap = DAO.sentiment.SentimentValence.getValenceMap().entrySet().iterator();
		logger.info("Valence map has " + DAO.sentiment.SentimentValence.getValenceMap().size() + " sentiments.");
		while (itValenceMap.hasNext()) {
	        Map.Entry pair = (Map.Entry)itValenceMap.next();
	        DAO.sentiment.SentimentValence daoSe = (DAO.sentiment.SentimentValence)pair.getValue();
	        se.add(new DAO.sentiment.SentimentValence(
	        		daoSe.getId(),
	        		daoSe.getName(),
	        		daoSe.getIndexLabel(),
	        		daoSe.getRage()
	        		), 0.0);
		}
		d.setSentimentEntity(se);

		// initialize sentiment valence map for blocks
		for (int i = 0; i < d.getBlocks().size(); i++) {
			Block b = d.getBlocks().get(i);
			
			se = new SentimentEntity();
			itValenceMap = DAO.sentiment.SentimentValence.getValenceMap().entrySet().iterator();
			logger.info("Should add " + DAO.sentiment.SentimentValence.getValenceMap().size() + " sentiments to block " + b.getIndex());
			while (itValenceMap.hasNext()) {
		        Map.Entry pair = (Map.Entry)itValenceMap.next();
		        DAO.sentiment.SentimentValence daoSe = (DAO.sentiment.SentimentValence)pair.getValue();
		        se.add(new DAO.sentiment.SentimentValence(
		        		daoSe.getId(),
		        		daoSe.getName(),
		        		daoSe.getIndexLabel(),
		        		daoSe.getRage()
		        		), 0.0);
			}
			b.setSentimentEntity(se);
			logger.info("Block " + b.getIndex() + " has " + b.getSentimentEntity().getAll().size() + " initialized sentiments now");
		}
		
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
				        if (value != null) {
				        	avgBlock.put(sv, (avgBlock.get(sv) == null ? 0 : avgBlock.get(sv))
				        			+ b.getSentenceBlockDistances()[j].getCohesion()
									* value);
				        	sumWeightsBlock.put(sv, (sumWeightsBlock.get(sv) == null ? 0 : sumWeightsBlock.get(sv))
				        			+ b.getSentenceBlockDistances()[j].getCohesion());
				        }
				        it.remove(); // avoids a ConcurrentModificationException
				    }
				}
				
				// updating sentiment values for block
				it = b.getSentimentEntity().getAll().entrySet().iterator();
				logger.info("Updating sentiment valences for current block. Block has " + b.getSentimentEntity().getAll().size() + " sentiments.");
				while (it.hasNext()) {
			        Map.Entry pair = (Map.Entry)it.next();
			        SentimentValence sv = (SentimentValence)pair.getKey();
			        Double value = (Double)pair.getValue();
					if (sumWeightsBlock.get(sv) != null) {
						avgBlock.put(sv, avgBlock.get(sv) / sumWeightsBlock.get(sv));
						//b.setSentimentEntity(new SentimentEntity(b.getProcessedText().trim(), avgBlock));
						se = new SentimentEntity();
						se.add(sv, avgBlock.get(sv));
						avgDoc.put(sv, (avgDoc.get(sv) == null ? 0 : avgDoc.get(sv)) + avgBlock.get(sv) * d.getBlockDocDistances()[i].getCohesion());
						sumWeightsDoc.put(sv,  (sumWeightsDoc.get(sv) == null ? 0 : sumWeightsDoc.get(sv) + d.getBlockDocDistances()[i].getCohesion()));
						logger.info("Adding sentiment " + sv.getIndexLabel() + " to block " + b.getIndex());
						b.setSentimentEntity(se);
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
			if (sumWeightsDoc.get(sv) != null) {
				se = new SentimentEntity();
				se.add(sv, avgDoc.get(sv) / sumWeightsDoc.get(sv));
				logger.info("Adding sentiment " + sv.getIndexLabel() + " to document ");
				d.setSentimentEntity(se);
			}
		}
	}
}
