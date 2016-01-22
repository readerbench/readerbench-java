package view.widgets.article.utils.distanceStrategies;

import java.util.List;

import view.widgets.article.utils.AuthorContainer;
import view.widgets.article.utils.SingleAuthorContainer;

public class CoAuthorshipDistanceStrategy implements IAuthorDistanceStrategy {
	private AuthorContainer authorContainer;
	private double maxCoAuthorshipCount;
	
	public CoAuthorshipDistanceStrategy(AuthorContainer authorContainer) {
		this.authorContainer = authorContainer;
		this.buildMaxCoAuthorshipCount();
	}
	private void buildMaxCoAuthorshipCount() {
		this.maxCoAuthorshipCount = 0.0;
		List<SingleAuthorContainer> containerList = authorContainer.getAuthorContainers();
		
		for(int i = 0; i < containerList.size(); i++) {
			for(int j = i+1; j < containerList.size(); j ++) {
				SingleAuthorContainer c1 = containerList.get(i);
				SingleAuthorContainer c2 = containerList.get(j);
				double noOfCommonArticles = c1.getNumberOfCommonArticles(c2);
				if (noOfCommonArticles > this.maxCoAuthorshipCount) {
					this.maxCoAuthorshipCount = noOfCommonArticles;
				}
			}
		}
	}
	
	@Override
	public double computeDistanceBetween(SingleAuthorContainer firstAuthor,
			SingleAuthorContainer secondAuthor) {
		if(this.maxCoAuthorshipCount == 0) {
			return 0.0;
		}
		double noOfCommonArticles = firstAuthor.getNumberOfCommonArticles(secondAuthor);
		return noOfCommonArticles / this.maxCoAuthorshipCount;
	}
	@Override
	public String getStrategyName() {
		return "Co-Authorship Distance";
	}
	public String getStrategyKey() {
		return "CoAuthorship";
	}
}
