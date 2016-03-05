package view.widgets.article.utils.distanceStrategies;

import view.widgets.article.utils.ArticleContainer;

public class AuthorDistanceStrategyFactory {
	private ArticleContainer authorContainer;
	public AuthorDistanceStrategyFactory(ArticleContainer authorContainer) {
		this.authorContainer = authorContainer;
	}
	public IAuthorDistanceStrategy getDistanceStrategy(AuthorDistanceStrategyType strategyType) {
		switch (strategyType) {
		case SemanticDistance:
			return new SemanticAuthorDistanceStrategy(this.authorContainer);
		case CoAuthorshipDistance:
			return new CoAuthorshipDistanceStrategy(this.authorContainer);
		case CoCitationsDistance:
			return new CoCitationsDistanceStrategy(this.authorContainer);
		}
		return null;
	}
}