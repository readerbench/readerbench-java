package view.widgets.article.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import view.widgets.article.utils.distanceStrategies.IAuthorDistanceStrategy;

public class CachedAuthorDistanceStrategyDecorator implements IAuthorDistanceStrategy {
	private IAuthorDistanceStrategy distanceStrategy;
	private Map<String, AuthorPairDistanceContainer> authorsDistanceContainer;

	public CachedAuthorDistanceStrategyDecorator(AuthorContainer authorContainer, IAuthorDistanceStrategy distanceStrategy) {
		this.authorsDistanceContainer = new HashMap<String, AuthorPairDistanceContainer>();
		this.distanceStrategy = distanceStrategy;
		this.computeDistances(authorContainer);
	}
	private void computeDistances(AuthorContainer authorContainer) {
		for (int i = 0; i < authorContainer.getAuthorContainers().size() - 1; i++) {
			for (int j = i + 1; j < authorContainer.getAuthorContainers().size(); j++) {
				SingleAuthorContainer a1 = authorContainer.getAuthorContainers().get(i);
				SingleAuthorContainer a2 = authorContainer.getAuthorContainers().get(j);
				double sim = this.distanceStrategy.computeDistanceBetween(a1, a2);
				AuthorPairDistanceContainer pair = new AuthorPairDistanceContainer(a1, a2, sim);
				
				this.authorsDistanceContainer.put(a1.getAuthor().getAuthorUri() + a2.getAuthor().getAuthorUri(), pair);
				this.authorsDistanceContainer.put(a2.getAuthor().getAuthorUri() + a1.getAuthor().getAuthorUri(), pair);
			}
		}
	}
	
	@Override
	public double computeDistanceBetween(SingleAuthorContainer firstAuthor, SingleAuthorContainer secondAuthor) {
		AuthorPairDistanceContainer distContainer = this.getAssociatedDistanceContainer(firstAuthor, secondAuthor);
		if(distContainer != null) {
			return distContainer.getSimilarity();
		}
		return this.distanceStrategy.computeDistanceBetween(firstAuthor, secondAuthor);
	}
	
	private AuthorPairDistanceContainer getAssociatedDistanceContainer(SingleAuthorContainer firstAuthor, SingleAuthorContainer secondAuthor) {
		String key1 = firstAuthor.getAuthor().getAuthorUri() + secondAuthor.getAuthor().getAuthorUri();
		String key2 = secondAuthor.getAuthor().getAuthorUri() + firstAuthor.getAuthor().getAuthorUri();
		
		if(this.authorsDistanceContainer.containsKey(key1)) {
			return this.authorsDistanceContainer.get(key1);
		}
		else if(this.authorsDistanceContainer.containsKey(key2)) {
			return this.authorsDistanceContainer.get(key2);
		}
		return null;
	}

	@Override
	public String getStrategyName() {
		// TODO Auto-generated method stub
		return this.distanceStrategy.getStrategyName();
	}

}
