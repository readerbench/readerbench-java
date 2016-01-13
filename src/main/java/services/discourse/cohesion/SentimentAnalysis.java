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
import edu.cmu.lti.jawjaw.pobj.Lang;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;

/**
 *
 * @author Mihai Dascalu
 */
public class SentimentAnalysis {
	
	static Logger logger = Logger.getLogger(CohesionGraph.class);
	
	public static void weightSemanticValences(Sentence s) {
		if (s.getWords().isEmpty()) return;
		for (SentimentValence daoSe : SentimentValence.getAllValences()) {
			double value = s.getWords().stream().mapToDouble(w -> {
				SentimentEntity e = w.getSentiment();
				if (e == null) {
					return 0.;
				}
				Double v = e.get(daoSe);
				return (v == null ? 0. : v);
			}).sum() / s.getWords().size();
			s.getSentimentEntity().add(daoSe, value);
		}
	}
	
	public static void weightSemanticValences(Block b) {
		SentimentEntity se = new SentimentEntity();
		se.init();
		b.setSentimentEntity(se);
		Map<SentimentValence, Double> avgBlock = new HashMap<>();
		Map<SentimentValence, Double> sumWeightsBlock = new HashMap<>();
		// Map<SentimentValence, Double> elemValences =
		// b.getSentimentEntity().getAll();
		// double avgBlock = 0, sumWeightsBlock = 0;
		// logger.info("[Weighting] Block " + b.getIndex() + " has " +
		// b.getSentences().size() + " sentences.");
		for (int i = 0; i < b.getSentences().size(); i++) {
			Sentence s = b.getSentences().get(i);
			weightSemanticValences(s);
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
							+ b.getSentenceBlockDistances()[i].getCohesion() * value);
					sumWeightsBlock.put(sv, (sumWeightsBlock.get(sv) == null ? 0 : sumWeightsBlock.get(sv))
							+ b.getSentenceBlockDistances()[i].getCohesion());
				}
			}
		}
		avgBlock.entrySet().stream().forEach(e -> {
			b.getSentimentEntity().add(e.getKey(), e.getValue() / sumWeightsBlock.get(e.getKey()));
		});
		
	}
	
	public static void weightSemanticValences(AbstractDocument d) {
		logger.info("Weighting sentiment valences ...");

		// initialize sentiment valence map for document
		SentimentEntity se = new SentimentEntity();
		se.init();
		// logger.info("Valence map has " +
		// DAO.sentiment.SentimentValence.getValenceMap().size() + "
		// sentiments.");
		d.setSentimentEntity(se);
		
		Map<SentimentValence, Double> avgDoc = new HashMap<>();
		Map<SentimentValence, Double> sumWeightsDoc = new HashMap<>();
		// perform weighted sentiment per block and per document

		// logger.info("[Weighting] I have " + d.getBlocks().size() + "
		// blocks.");
		for (int i = 0; i < d.getBlocks().size(); i++) {
			Block b = d.getBlocks().get(i);
			if (b != null) {
				weightSemanticValences(b);
				
				for (Map.Entry<SentimentValence, Double> pair : b.getSentimentEntity().getAll().entrySet()) {
					SentimentValence sv = pair.getKey();
					Double value = pair.getValue();
					avgDoc.put(sv, (avgDoc.get(sv) == null ? 0 : avgDoc.get(sv))
							+ value * d.getBlockDocDistances()[i].getCohesion());
					sumWeightsDoc.put(sv, (sumWeightsDoc.get(sv) == null ? 0 : sumWeightsDoc.get(sv))
							+ d.getBlockDocDistances()[i].getCohesion());
					// logger.info("Adding sentiment " + sv.getIndexLabel()
					// + " to block " + b.getIndex());
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
