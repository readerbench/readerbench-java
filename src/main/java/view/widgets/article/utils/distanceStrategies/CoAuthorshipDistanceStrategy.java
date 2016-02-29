package view.widgets.article.utils.distanceStrategies;

import java.util.List;

import data.article.ArticleAuthor;
import data.article.ResearchArticle;
import view.widgets.article.utils.ArticleContainer;
import view.widgets.article.utils.SingleAuthorContainer;

public class CoAuthorshipDistanceStrategy implements IAuthorDistanceStrategy {
	private ArticleContainer authorContainer;
	private double maxNoOfCommonArticlesBetweenAuthors;
	private double maxNoOfCommonAuthorsBetweenArticles;
	
	public CoAuthorshipDistanceStrategy(ArticleContainer authorContainer) {
		this.authorContainer = authorContainer;
		this.buildMaxNoOfCommonArticlesBetweenAuthors();
		this.buildMaxNoOfCommonAuthorsBetweenArticles();
	}
	private void buildMaxNoOfCommonArticlesBetweenAuthors() {
		this.maxNoOfCommonArticlesBetweenAuthors = 0.0;
		List<SingleAuthorContainer> containerList = authorContainer.getAuthorContainers();
		
		for(int i = 0; i < containerList.size(); i++) {
			for(int j = i+1; j < containerList.size(); j ++) {
				SingleAuthorContainer c1 = containerList.get(i);
				SingleAuthorContainer c2 = containerList.get(j);
				double noOfCommonArticles = c1.getNumberOfCommonArticles(c2);
				if (noOfCommonArticles > this.maxNoOfCommonArticlesBetweenAuthors) {
					this.maxNoOfCommonArticlesBetweenAuthors = noOfCommonArticles;
				}
			}
		}
	}
	private void buildMaxNoOfCommonAuthorsBetweenArticles() {
		this.maxNoOfCommonAuthorsBetweenArticles = 0;
		List<ResearchArticle> articleList = authorContainer.getArticles();
		for(int i = 0; i < articleList.size(); i++) {
			for(int j = i+1; j < articleList.size(); j ++) {
				ResearchArticle a1 = articleList.get(i);
				ResearchArticle a2 = articleList.get(j);
				double noCommonAuthors = this.getNoOfCommonAuthorsBetween(a1, a2);
				if (noCommonAuthors > this.maxNoOfCommonAuthorsBetweenArticles) {
					this.maxNoOfCommonAuthorsBetweenArticles = noCommonAuthors;
				}
			}
		}
	}
	private double getNoOfCommonAuthorsBetween(ResearchArticle a1, ResearchArticle a2) {
		double noCommonAuthors = 0;
		List<ArticleAuthor> a1Authors = a1.getArticleAuthorList();
		for(ArticleAuthor a1Author : a1Authors) {
			if(this.articleHasAuthor(a2, a1Author)) {
				noCommonAuthors ++;
			}
		}
		return noCommonAuthors;
	}
	private boolean articleHasAuthor(ResearchArticle article, ArticleAuthor author) {
		for(ArticleAuthor articleAuthor : article.getArticleAuthorList() ) {
			if(articleAuthor.isSameAuthor(author)) {
				return true;
			}
		}
		return false;
	}
	
	
	@Override
	public double computeDistanceBetween(SingleAuthorContainer firstAuthor,
			SingleAuthorContainer secondAuthor) {
		if(this.maxNoOfCommonArticlesBetweenAuthors == 0) {
			return 0.0;
		}
		double noOfCommonArticles = firstAuthor.getNumberOfCommonArticles(secondAuthor);
		return noOfCommonArticles / this.maxNoOfCommonArticlesBetweenAuthors;
	}
	@Override
	public double computeDistanceBetween(ResearchArticle firstArticle, ResearchArticle secondArticle) {
		if(this.maxNoOfCommonAuthorsBetweenArticles == 0) {
			return 0.0;
		}
		if(firstArticle.getURI().equals(secondArticle.getURI())) {
			return 1.0;
		}
		double noCommonAuthors = this.getNoOfCommonAuthorsBetween(firstArticle, secondArticle);
		return noCommonAuthors / this.maxNoOfCommonAuthorsBetweenArticles; 
	}
	@Override
	public double computeDistanceBetween(SingleAuthorContainer author, ResearchArticle article) {
		if(this.articleHasAuthor(article, author.getAuthor())) {
			return 1.0;
		}
		return 0.0;
	}
	@Override
	public String getStrategyName() {
		return "Co-Authorship Distance";
	}
	@Override
	public String getStrategyKey() {
		return "CoAuthorship";
	}
}
