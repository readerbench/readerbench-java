package view.widgets.article.utils.distanceStrategies;

import view.widgets.article.utils.SingleAuthorContainer;

public interface IAuthorDistanceStrategy {
	public double computeDistanceBetween(SingleAuthorContainer firstAuthor, SingleAuthorContainer secondAuthor);
}