package view.widgets.article.utils.distanceStrategies;

import java.util.List;

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
				aggregatedScore += this.computeDistanceBetween(firstAuthorArticle, secondAuthorArticle);
				noOfArticles += 1.0;
			}
		}
		if(noOfArticles > 0) {
			return aggregatedScore / noOfArticles;
		}
		return 0.0;
	}
	@Override
	public double computeDistanceBetween(ResearchArticle firstArticle, ResearchArticle secondArticle) {
		if(firstArticle.getURI().equals(secondArticle.getURI())) {
			return 1.0;
		}
		else {
			return computeDistance(firstArticle, secondArticle);
		}
	}
	@Override
	public double computeDistanceBetween(SingleAuthorContainer author, ResearchArticle article) {
		if(author.getAuthorArticles().size() == 0) {
			return 0.0;
		}
		List<ResearchArticle> authorArticleList = author.getAuthorArticles();
		double totalDistance = 0.0;
		for(ResearchArticle authorArticle : authorArticleList) {
			totalDistance += this.computeDistanceBetween(authorArticle, article);
		}
		return totalDistance / ((double)author.getAuthorArticles().size());
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
