package view.widgets.article.utils;

import java.util.HashMap;
import java.util.Map;

import data.article.ResearchArticle;
import view.widgets.article.utils.distanceStrategies.IAuthorDistanceStrategy;

public class CachedAuthorDistanceStrategyDecorator implements IAuthorDistanceStrategy {
	private IAuthorDistanceStrategy distanceStrategy;
	private Map<String, Double> singleAuthorContainerDistanceCache;
	private Map<String, Double> researchArticleDistanceCache;
	private Map<String, Double> authorToArticleDistanceCache;
	
	public CachedAuthorDistanceStrategyDecorator(ArticleContainer authorContainer, IAuthorDistanceStrategy distanceStrategy) {
		this.distanceStrategy = distanceStrategy;
		this.singleAuthorContainerDistanceCache = new HashMap<String, Double>();
		this.researchArticleDistanceCache = new HashMap<String, Double>();
		this.authorToArticleDistanceCache = new HashMap<String, Double>();
	}
	
	@Override
	public double computeDistanceBetween(SingleAuthorContainer firstAuthor, SingleAuthorContainer secondAuthor) {
		String key1 = firstAuthor.getAuthor().getAuthorUri() + secondAuthor.getAuthor().getAuthorUri();
		if(singleAuthorContainerDistanceCache.containsKey(key1)) {
			return singleAuthorContainerDistanceCache.get(key1);
		}
		
		String key2 = secondAuthor.getAuthor().getAuthorUri() + firstAuthor.getAuthor().getAuthorUri();
		if(singleAuthorContainerDistanceCache.containsKey(key2)) {
			return singleAuthorContainerDistanceCache.get(key2);
		}
		
		Double distance = this.distanceStrategy.computeDistanceBetween(firstAuthor, secondAuthor);
		singleAuthorContainerDistanceCache.put(key1, distance);
		return distance;
	}
	
	@Override 
	public double computeDistanceBetween(ResearchArticle firstArticle, ResearchArticle secondArticle) {
		String key1 = firstArticle.getURI() + secondArticle.getURI();
		if(this.researchArticleDistanceCache.containsKey(key1)) {
			return this.researchArticleDistanceCache.get(key1);
		}
		
		String key2 = secondArticle.getURI() + firstArticle.getURI();
		if(this.researchArticleDistanceCache.containsKey(key2)) {
			return this.researchArticleDistanceCache.get(key2);
		}
		
		Double distance = this.distanceStrategy.computeDistanceBetween(firstArticle, secondArticle);
		researchArticleDistanceCache.put(key1, distance);
		return distance;
	}
	
	public double computeDistanceBetween(SingleAuthorContainer author, ResearchArticle article) {
		String key = author.getAuthor().getAuthorUri() + article.getURI();
		if(this.authorToArticleDistanceCache.containsKey(key)) {
			return this.authorToArticleDistanceCache.get(key);
		}
		
		Double distance = this.distanceStrategy.computeDistanceBetween(author, article);
		authorToArticleDistanceCache.put(key, distance);
		return distance;
	}
	
	public boolean pruneArticlePair(ResearchArticle firstArticle, ResearchArticle secondArticle) {
		return this.distanceStrategy.pruneArticlePair(firstArticle, secondArticle);
	}
	public double getThreshold() {
		return this.distanceStrategy.getThreshold();
	}
	@Override
	public String getStrategyName() {
		// TODO Auto-generated method stub
		return this.distanceStrategy.getStrategyName();
	}
	public String getStrategyKey() {
		return this.distanceStrategy.getStrategyKey();
	}
}
