package view.widgets.article.utils;

import java.util.ArrayList;
import java.util.List;
import view.widgets.article.utils.distanceStrategies.IAuthorDistanceStrategy;

public class CachedAuthorDistanceStrategyDecorator implements IAuthorDistanceStrategy {
	private IAuthorDistanceStrategy distanceStrategy;
	private List<AuthorPairDistanceContainer> authorsDistanceContainer;

	public CachedAuthorDistanceStrategyDecorator(AuthorContainer authorContainer, IAuthorDistanceStrategy distanceStrategy) {
		this.authorsDistanceContainer = new ArrayList<AuthorPairDistanceContainer>();
		this.distanceStrategy = distanceStrategy;
		this.computeDistances(authorContainer);
	}
	private void computeDistances(AuthorContainer authorContainer) {
		for (int i = 0; i < authorContainer.getAuthorContainers().size() - 1; i++) {
			for (int j = i + 1; j < authorContainer.getAuthorContainers().size(); j++) {
				SingleAuthorContainer a1 = authorContainer.getAuthorContainers().get(i);
				SingleAuthorContainer a2 = authorContainer.getAuthorContainers().get(j);
				double sim = this.distanceStrategy.computeDistanceBetween(a1, a2);
				this.authorsDistanceContainer.add(new AuthorPairDistanceContainer(a1, a2, sim));
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
		AuthorPairDistanceContainer container = new AuthorPairDistanceContainer(firstAuthor, secondAuthor, 0.0);
		for(AuthorPairDistanceContainer internalContainer : this.authorsDistanceContainer) {
			if(internalContainer.equals(container)) {
				return internalContainer;
			}
		}
		return null;
	}

	@Override
	public String getStrategyName() {
		// TODO Auto-generated method stub
		return this.distanceStrategy.getStrategyName();
	}

}
