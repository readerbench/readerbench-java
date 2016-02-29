package view.widgets.article.utils;

import view.widgets.article.utils.distanceStrategies.IAuthorDistanceStrategy;
import data.article.ResearchArticle;

enum GraphNodeItemType {
	Author,
	Article
}

public class GraphNodeItem implements Comparable<GraphNodeItem> {
	private GraphNodeItemType nodeType;
	
	ResearchArticle article;
	SingleAuthorContainer author;
	
	public GraphNodeItem(ResearchArticle article) {
		this.nodeType = GraphNodeItemType.Article;
		this.article = article;
	}
	public GraphNodeItem(SingleAuthorContainer author) {
		this.nodeType = GraphNodeItemType.Author;
		this.author = author;
	}
	
	public String getURI() {
		switch (this.nodeType) {
		case Article:
			return this.article.getURI();
		case Author:
			return this.author.getAuthor().getAuthorUri();
		}
		return "";
	}
	public String getName() {
		switch (this.nodeType) {
		case Article:
			return this.article.getTitleText();
		case Author:
			return this.author.getAuthor().getAuthorName();
		}
		return "";
	}
	public boolean isArticle() {
		return this.nodeType == GraphNodeItemType.Article;
	}
	public boolean isAuthor() {
		return this.nodeType == GraphNodeItemType.Author;
	}
	
	@Override
	public int hashCode() {
		return this.getURI().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == null || obj == null)
			return false;
		GraphNodeItem otherNode = (GraphNodeItem)obj;
		return this.nodeType == otherNode.nodeType && this.getURI().equals(otherNode.getURI());
	}
	
	public double computeScore(GraphNodeItem otherItem, IAuthorDistanceStrategy distanceStrategy) {
		if(this.isArticle()) {
			if(otherItem.isArticle()) {
				return distanceStrategy.computeDistanceBetween(this.article, otherItem.article);
			}
			else {
				return distanceStrategy.computeDistanceBetween(otherItem.author, this.article);
			}
		}
		else {
			if(otherItem.isArticle()) {
				return distanceStrategy.computeDistanceBetween(this.author, otherItem.article);
			}
			else {
				return distanceStrategy.computeDistanceBetween(otherItem.author, this.author);
			}
		}
	}
	@Override
	public int compareTo(GraphNodeItem otherNode) {
		if(this.nodeType != otherNode.nodeType) {
			if(this.isArticle()) {
				return -1;
			}
			return 1;
		}
		if(this.nodeType == GraphNodeItemType.Article) {
			return this.article.compareTo(otherNode.article);
		}
		return this.author.compareTo(otherNode.author);
	}
}
