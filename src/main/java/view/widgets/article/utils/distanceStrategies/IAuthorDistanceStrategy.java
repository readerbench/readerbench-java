package view.widgets.article.utils.distanceStrategies;

import data.article.ResearchArticle;
import view.widgets.article.utils.SingleAuthorContainer;

public interface IAuthorDistanceStrategy {
	public double computeDistanceBetween(SingleAuthorContainer firstAuthor, SingleAuthorContainer secondAuthor);
	public double computeDistanceBetween(ResearchArticle firstArticle, ResearchArticle secondArticle);
	public double computeDistanceBetween(SingleAuthorContainer author, ResearchArticle article);
	public String getStrategyName();
	public String getStrategyKey();
}