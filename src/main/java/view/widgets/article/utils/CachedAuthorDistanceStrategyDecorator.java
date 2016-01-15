package view.widgets.article.utils;

import java.util.ArrayList;
import java.util.List;
import view.widgets.article.utils.distanceStrategies.IAuthorDistanceStrategy;

public class CachedAuthorDistanceStrategyDecorator implements IAuthorDistanceStrategy {
	private IAuthorDistanceStrategy distanceStrategy;
	private List<TwoAuthorsDistanceContainer> authorsDistanceContainer;

	public CachedAuthorDistanceStrategyDecorator(AuthorContainer authorContainer, IAuthorDistanceStrategy distanceStrategy) {
		this.authorsDistanceContainer = new ArrayList<TwoAuthorsDistanceContainer>();
		this.distanceStrategy = distanceStrategy;
		this.computeDistances(authorContainer);
	}
	private void computeDistances(AuthorContainer authorContainer) {
		for (int i = 0; i < authorContainer.getAuthorContainers().size() - 1; i++) {
			for (int j = i + 1; j < authorContainer.getAuthorContainers().size(); j++) {
				SingleAuthorContainer a1 = authorContainer.getAuthorContainers().get(i);
				SingleAuthorContainer a2 = authorContainer.getAuthorContainers().get(j);
				double sim = this.distanceStrategy.computeDistanceBetween(a1, a2);
				this.authorsDistanceContainer.add(new TwoAuthorsDistanceContainer(a1, a2, sim));
			}
		}
	}
	
	@Override
	public double computeDistanceBetween(SingleAuthorContainer firstAuthor, SingleAuthorContainer secondAuthor) {
		TwoAuthorsDistanceContainer distContainer = this.getAssociatedDistanceContainer(firstAuthor, secondAuthor);
		if(distContainer != null) {
			return distContainer.getSimilarity();
		}
		return this.distanceStrategy.computeDistanceBetween(firstAuthor, secondAuthor);
	}
	
	private TwoAuthorsDistanceContainer getAssociatedDistanceContainer(SingleAuthorContainer firstAuthor, SingleAuthorContainer secondAuthor) {
		TwoAuthorsDistanceContainer container = new TwoAuthorsDistanceContainer(firstAuthor, secondAuthor, 0.0);
		for(TwoAuthorsDistanceContainer internalContainer : this.authorsDistanceContainer) {
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
