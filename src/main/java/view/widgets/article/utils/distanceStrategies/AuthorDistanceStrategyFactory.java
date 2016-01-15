package view.widgets.article.utils.distanceStrategies;

import view.widgets.article.utils.AuthorContainer;

public class AuthorDistanceStrategyFactory {
	private AuthorContainer authorContainer;
	public AuthorDistanceStrategyFactory(AuthorContainer authorContainer) {
		this.authorContainer = authorContainer;
	}
	public IAuthorDistanceStrategy getDistanceStrategy(AuthorDistanceStrategyType strategyType) {
		switch (strategyType) {
		case SemanticDistance:
			return new SemanticAuthorDistanceStrategy();
		case CoAuthorshipDistance:
			return new CoAuthorshipDistanceStrategy(this.authorContainer);
		case CoCitationsDistance:
			return new CoCitationsDistanceStrategy(this.authorContainer);
		}
		return null;
	}
}