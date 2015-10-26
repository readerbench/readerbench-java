package services.discourse.selfExplanations;

import org.apache.log4j.Logger;

import services.semanticModels.LSA.LSA;
import DAO.Block;
import DAO.discourse.SemanticCohesion;
import DAO.document.Metacognition;

public class VerbalizationAssessment {
	static Logger logger = Logger.getLogger(LSA.class);

	public static void detRefBlockSimilarities(Metacognition metacognition) {
		logger.info("Building metacognition block similarities");

		// determine similarities with previous blocks from referred document
		metacognition.setBlockSimilarities(new SemanticCohesion[metacognition.getReferredDoc().getBlocks().size()]);

		int startIndex = 0;
		int endIndex = 0;
		for (Block v : metacognition.getBlocks()) {
			if (v.getRefBlock() != null) {
				endIndex = v.getRefBlock().getIndex();
				for (int refBlockId = startIndex; refBlockId <= endIndex; refBlockId++) {
					metacognition.getBlockRelatedness()[refBlockId] = new SemanticCohesion(v,
							metacognition.getReferredDoc().getBlocks().get(refBlockId));
				}
				startIndex = endIndex + 1;
			}
		}
	}
}
