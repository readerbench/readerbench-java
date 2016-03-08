package view.widgets.article.utils.distanceStrategies;

import java.util.List;

import data.article.ResearchArticle;
import view.widgets.article.utils.ArticleContainer;
import view.widgets.article.utils.SingleAuthorContainer;

public class CoCitationsDistanceStrategy extends AAuthorDistanceStrategy {
	private ArticleContainer authorContainer;
	private double maxCoCitationsBetweenArticles;
	
	public CoCitationsDistanceStrategy(ArticleContainer authorContainer) {
		this.authorContainer = authorContainer;
		this.buildMaxCoCitationsBetweenArticles();
	}
	private void buildMaxCoCitationsBetweenArticles() {
		this.maxCoCitationsBetweenArticles = 0.0;
		List<ResearchArticle> articleList = authorContainer.getArticles();
		for(int i = 0; i < articleList.size(); i++) {
			for(int j = i+1; j < articleList.size(); j ++) {
				ResearchArticle a1 = articleList.get(i);
				ResearchArticle a2 = articleList.get(j);
				
				double noOfCoCitations = SingleAuthorContainer.getNoOfCoCitationsBetweenArticles(a1, a2);
				if (noOfCoCitations > this.maxCoCitationsBetweenArticles) {
					this.maxCoCitationsBetweenArticles = noOfCoCitations;
				}
			}
		}
	}
	@Override
	public double computeDistanceBetween(ResearchArticle firstArticle, ResearchArticle secondArticle) {
		if(this.maxCoCitationsBetweenArticles == 0) {
			return 0.0;
		}
		if(firstArticle.getURI().equals(secondArticle.getURI())) {
			return 1.0;
		}
		double noOfCoCitations = SingleAuthorContainer.getNoOfCoCitationsBetweenArticles(firstArticle, secondArticle);
		return noOfCoCitations / this.maxCoCitationsBetweenArticles;
	}
	public boolean pruneArticlePair(ResearchArticle firstArticle, ResearchArticle secondArticle) {
		return false;
	}
	public double getThreshold() {
		return 0.000000000001;
	}
	@Override
	public String getStrategyName() {
		return "Co-Citations Distance";
	}
	@Override
	public String getStrategyKey() {
		return "CoCitations";
	}
}
