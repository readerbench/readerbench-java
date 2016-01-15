package view.widgets.article.utils.distanceStrategies;

import cc.mallet.util.Maths;
import data.AbstractDocument;
import data.article.ResearchArticle;
import data.discourse.SemanticCohesion;
import services.commons.VectorAlgebra;
import view.widgets.article.utils.SingleAuthorContainer;

public class SemanticAuthorDistanceStrategy implements IAuthorDistanceStrategy {
	@Override
	public double computeDistanceBetween(SingleAuthorContainer firstAuthor,
			SingleAuthorContainer secondAuthor) {
		if(firstAuthor.isSameAuthor(secondAuthor.getAuthor())) {
			return 1.0;
		}
		double aggregatedScore = 0.0, noOfArticles = 0.0;
		
		for(ResearchArticle firstAuthorArticle : firstAuthor.getAuthorArticles()) {
			for(ResearchArticle secondAuthorArticle : secondAuthor.getAuthorArticles()) {
				if(firstAuthorArticle.getURI().equals(secondAuthorArticle.getURI())) {
					aggregatedScore += 1.0;
				}
				else {
					aggregatedScore += computeDistance(firstAuthorArticle, secondAuthorArticle);
				}
				noOfArticles += 1.0;
			}
		}
		if(noOfArticles > 0) {
			return aggregatedScore / noOfArticles;
		}
		return 0.0;
	}
	@Override
	public String getStrategyName() {
		return "Semantic Distance";
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
