package webService.cvCover;

import cc.mallet.util.Maths;
import data.AbstractDocument;
import data.discourse.SemanticCohesion;
import services.commons.VectorAlgebra;

public class CvCoverHelper {

	public static double computeSemanticSimilarity(
			AbstractDocument document1,
			AbstractDocument document2
		) {
		
		double
			lsaSim = 0,
			ldaSim = 0;
		if (document1.getLSA() != null && document2.getLSA() != null)
			lsaSim = VectorAlgebra.cosineSimilarity(document1.getLSAVector(), document2.getLSAVector());
		if (document1.getLDA() != null && document2.getLDA() != null)
			ldaSim = 1 - Maths.jensenShannonDivergence(document1.getLDAProbDistribution(), document2.getLDAProbDistribution());
		return SemanticCohesion.getAggregatedSemanticMeasure(lsaSim, ldaSim);
	}
	
}
