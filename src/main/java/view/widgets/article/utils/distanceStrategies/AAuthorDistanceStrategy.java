package view.widgets.article.utils.distanceStrategies;

import java.util.List;

import view.widgets.article.utils.SingleAuthorContainer;
import data.article.ResearchArticle;

public abstract class AAuthorDistanceStrategy implements IAuthorDistanceStrategy {
	public double computeDistanceBetween(SingleAuthorContainer firstAuthor, SingleAuthorContainer secondAuthor) {
		double totalScore = 0.0, totalArticles = 0.0;
		for(ResearchArticle firstAuthorArticle : firstAuthor.getAuthorArticles()) {
			for(ResearchArticle secondAuthorArticle : secondAuthor.getAuthorArticles()) {
				double score = this.computeDistanceBetween(firstAuthorArticle, secondAuthorArticle);
				boolean pruneFlag = this.pruneArticlePair(firstAuthorArticle, secondAuthorArticle);
				if(score > this.getThreshold() && !pruneFlag) {
					totalScore += score;
					totalArticles ++;
				}
			}
		}
		if(totalArticles == 0) {
			return 0.0;
		}
		return (totalScore / totalArticles);
	}
	public double computeDistanceBetween(SingleAuthorContainer author, ResearchArticle article) {
		if(author.getAuthorArticles().size() == 0) {
			return 0.0;
		}
		List<ResearchArticle> authorArticleList = author.getAuthorArticles();
		double totalScore = 0.0, totalArticles = 0.0;
		for(ResearchArticle authorArticle : authorArticleList) {
			double score = this.computeDistanceBetween(authorArticle, article);
			boolean pruneFlag = this.pruneArticlePair(authorArticle, article);
			if(score > this.getThreshold() && !pruneFlag) {
				totalScore += score;
				totalArticles ++;
			}
		}
		if(totalArticles == 0) {
			return 0.0;
		}
		return (totalScore / totalArticles);
	}
	public abstract double computeDistanceBetween(ResearchArticle firstArticle, ResearchArticle secondArticle);
	public abstract boolean pruneArticlePair(ResearchArticle firstArticle, ResearchArticle secondArticle);
	public abstract double getThreshold();
	public abstract String getStrategyName();
	public abstract String getStrategyKey();
}
