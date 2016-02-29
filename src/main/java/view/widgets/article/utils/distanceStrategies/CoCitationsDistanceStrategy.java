package view.widgets.article.utils.distanceStrategies;

import java.util.List;

import data.article.ResearchArticle;
import view.widgets.article.utils.ArticleContainer;
import view.widgets.article.utils.SingleAuthorContainer;

public class CoCitationsDistanceStrategy implements IAuthorDistanceStrategy {
	private ArticleContainer authorContainer;
	private double maxCoCitationsBetweenAuthors;
	private double maxCoCitationsBetweenArticles;
	
	public CoCitationsDistanceStrategy(ArticleContainer authorContainer) {
		this.authorContainer = authorContainer;
		this.buildMaxCoCitationsBetweenAuthors();
		this.buildMaxCoCitationsBetweenArticles();
	}
	private void buildMaxCoCitationsBetweenAuthors() {
		this.maxCoCitationsBetweenAuthors = 0.0;
		List<SingleAuthorContainer> containerList = authorContainer.getAuthorContainers();
		
		for(int i = 0; i < containerList.size(); i++) {
			for(int j = i+1; j < containerList.size(); j ++) {
				SingleAuthorContainer c1 = containerList.get(i);
				SingleAuthorContainer c2 = containerList.get(j);
				double noOfCoCitations = c1.getNumberOfCoCitations(c2);
				if (noOfCoCitations > this.maxCoCitationsBetweenAuthors) {
					this.maxCoCitationsBetweenAuthors = noOfCoCitations;
				}
			}
		}
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
	public double computeDistanceBetween(SingleAuthorContainer firstAuthor,
			SingleAuthorContainer secondAuthor) {
		if(this.maxCoCitationsBetweenAuthors == 0) {
			return 0.0;
		}
		double noOfCoCitations = firstAuthor.getNumberOfCoCitations(secondAuthor);
		return noOfCoCitations / this.maxCoCitationsBetweenAuthors;
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
		return noOfCoCitations / this.maxCoCitationsBetweenAuthors;
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
		return "Co-Citations Distance";
	}
	@Override
	public String getStrategyKey() {
		return "CoCitations";
	}
}
