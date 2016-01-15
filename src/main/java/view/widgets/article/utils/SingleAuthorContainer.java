package view.widgets.article.utils;

import java.util.List;

import data.article.ResearchArticle;
import data.article.ArticleAuthor;

public class SingleAuthorContainer {
	ArticleAuthor author;
	List<ResearchArticle> authorArticles;
	
	public SingleAuthorContainer(ArticleAuthor author, List<ResearchArticle> authorArticles) {
		this.authorArticles = authorArticles;
	}
	
	@Override
	public String toString() {
		String containerString = "{" + this.author.toString() + ", [";
		for(ResearchArticle article : this.authorArticles) {
			containerString += " {" + article.getTitleText() + "}";
		}
		return containerString + "]}";
	}
	public void addResearchArticle(ResearchArticle article) {
		if(!this.hasArticle(article)) {
			this.authorArticles.add(article);
		}
	}
	private boolean hasArticle(ResearchArticle article) {
		for(ResearchArticle authorArticle : this.authorArticles) {
			if(authorArticle.getURI().equals(article.getURI())) {
				return true;
			}
		}
		return false;
	}
	public boolean isSameAuthor(ArticleAuthor author) {
		return this.author.isSameAuthor(author);
	}
	public ArticleAuthor getAuthor() {
		return this.author;
	}
	public List<ResearchArticle> getAuthorArticles() {
		return this.authorArticles;
	}
	public double getNumberOfCommonArticles(SingleAuthorContainer otherAuthorContainer) {
		ArticleAuthor otherAuthor  = otherAuthorContainer.author;
		double noOfCommonArticles = 0.0;
		for(ResearchArticle article : this.authorArticles) {
			for(ArticleAuthor articleAuthor : article.getArticleAuthorList()) {
				if(articleAuthor.isSameAuthor(otherAuthor)) {
					noOfCommonArticles++;
					continue;
				}
			}
		}
		return noOfCommonArticles;
	}
	
	public double getNumberOfCoCitations(SingleAuthorContainer otherAuthorContainer) {
		double noOfCoCitations = 0.0;
		for(ResearchArticle article : this.authorArticles) {
			for(String citationUri : article.getCitationURIList()) {
				for(ResearchArticle otherAuthorArticle : otherAuthorContainer.getAuthorArticles()) {
					for(String otherCitationUri : otherAuthorArticle.getCitationURIList()) {
						if(citationUri.equals(otherCitationUri)) {
							noOfCoCitations += 1.0;
						}
					}
				}
			}
		}
		return noOfCoCitations;
	}
}
