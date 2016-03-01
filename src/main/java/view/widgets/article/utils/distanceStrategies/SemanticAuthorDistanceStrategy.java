package view.widgets.article.utils.distanceStrategies;

import cc.mallet.util.Maths;
import data.AbstractDocument;
import data.article.ResearchArticle;
import data.discourse.SemanticCohesion;
import services.commons.VectorAlgebra;

public class SemanticAuthorDistanceStrategy extends AAuthorDistanceStrategy {
	@Override
	public double computeDistanceBetween(ResearchArticle firstArticle, ResearchArticle secondArticle) {
		if(firstArticle.getURI().equals(secondArticle.getURI())) {
			return 1.0;
		}
		return computeDistance(firstArticle, secondArticle);
	}
	@Override
	public String getStrategyName() {
		return "Semantic Distance";
	}
	@Override
	public String getStrategyKey() {
		return "Semantic";
	}
	private double computeDistance(AbstractDocument d1, AbstractDocument d2) {
		double lsaSim = 0;
		double ldaSim = 0;
		if (d1.getLSA() != null && d2.getLSA() != null)
			lsaSim = VectorAlgebra.cosineSimilarity(d1.getLSAVector(),
					d2.getLSAVector());
		if (d1.getLDA() != null && d2.getLDA() != null)
			ldaSim = 1 - Maths.jensenShannonDivergence(
					d1.getLDAProbDistribution(),
					d2.getLDAProbDistribution());
		double sim = SemanticCohesion.getAggregatedSemanticMeasure(
				lsaSim, ldaSim);
		return sim;
	}
}
