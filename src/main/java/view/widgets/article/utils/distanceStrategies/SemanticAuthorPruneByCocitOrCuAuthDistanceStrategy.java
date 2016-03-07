package view.widgets.article.utils.distanceStrategies;

import data.article.ResearchArticle;
import view.widgets.article.utils.ArticleContainer;

public class SemanticAuthorPruneByCocitOrCuAuthDistanceStrategy extends SemanticAuthorDistanceStrategy {
	private CoCitationsDistanceStrategy coCitationsStrategy;
	private CoAuthorshipDistanceStrategy coAuthorshipStrategy;
	public SemanticAuthorPruneByCocitOrCuAuthDistanceStrategy(ArticleContainer authorContainer) {
		super(authorContainer);
		this.coCitationsStrategy = new CoCitationsDistanceStrategy(authorContainer);
		this.coAuthorshipStrategy = new CoAuthorshipDistanceStrategy(authorContainer);
	}
	@Override
	public boolean pruneArticlePair(ResearchArticle firstArticle, ResearchArticle secondArticle) {
		double coCitationsScore = this.coCitationsStrategy.computeDistanceBetween(firstArticle, secondArticle);
		double coAuthorshipScore = this.coAuthorshipStrategy.computeDistanceBetween(firstArticle, secondArticle);
		return coCitationsScore == 0 && coAuthorshipScore == 0;
	}
	@Override
	public String getStrategyName() {
		return "Semantic Distance Prunned By Co Citations or Co Authorship";
	}
	@Override
	public String getStrategyKey() {
		return "SemanticPrunnedByCoCitOrCoAuth";
	}
}