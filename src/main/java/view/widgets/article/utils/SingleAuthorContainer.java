package view.widgets.article.utils;

import java.util.List;

import data.article.ResearchArticle;
import data.article.ArticleAuthor;

public class SingleAuthorContainer implements Comparable<SingleAuthorContainer> {
	ArticleAuthor author;
	List<ResearchArticle> authorArticles;
	
	public SingleAuthorContainer(ArticleAuthor author, List<ResearchArticle> authorArticles) {
		this.authorArticles = authorArticles;
		this.author = author;
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
			for(ResearchArticle otherAuthorArticle : otherAuthorContainer.getAuthorArticles()) {
				noOfCoCitations += SingleAuthorContainer.getNoOfCoCitationsBetweenArticles(article, otherAuthorArticle);
			}
		}
		return noOfCoCitations;
	}
	public static double getNoOfCoCitationsBetweenArticles(ResearchArticle a1, ResearchArticle a2) {
		double noOfCoCitations = 0.0;
		for(String citationUri : a1.getCitationURIList()) {
			for(String otherCitationUri : a2.getCitationURIList()) {
				if(citationUri.equals(otherCitationUri)) {
					noOfCoCitations += 1.0;
				}
			}
		}
		return noOfCoCitations;
	}

	@Override
	public int compareTo(SingleAuthorContainer o) {
		return this.author.getAuthorUri().compareTo(((SingleAuthorContainer)o).getAuthor().getAuthorUri());
	}
	@Override
	public int hashCode() {
		return this.author.getAuthorUri().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == null || obj == null)
			return false;
		SingleAuthorContainer d = (SingleAuthorContainer) obj;
		return this.getAuthor().getAuthorUri().equals(d.getAuthor().getAuthorUri());
	}
}
