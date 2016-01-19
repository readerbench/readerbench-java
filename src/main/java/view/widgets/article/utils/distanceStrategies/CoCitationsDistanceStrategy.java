package view.widgets.article.utils.distanceStrategies;

import java.util.List;

import view.widgets.article.utils.AuthorContainer;
import view.widgets.article.utils.SingleAuthorContainer;

public class CoCitationsDistanceStrategy implements IAuthorDistanceStrategy {
	private AuthorContainer authorContainer;
	private double maxCoCitationsCount;
	
	public CoCitationsDistanceStrategy(AuthorContainer authorContainer) {
		this.authorContainer = authorContainer;
		this.buildMaxCoCitationsCount();
	}
	private void buildMaxCoCitationsCount() {
		this.maxCoCitationsCount = 0.0;
		List<SingleAuthorContainer> containerList = authorContainer.getAuthorContainers();
		
		for(int i = 0; i < containerList.size(); i++) {
			for(int j = i+1; j < containerList.size(); j ++) {
				SingleAuthorContainer c1 = containerList.get(i);
				SingleAuthorContainer c2 = containerList.get(j);
				double noOfCoCitations = c1.getNumberOfCoCitations(c2);
				if (noOfCoCitations > this.maxCoCitationsCount) {
					this.maxCoCitationsCount = noOfCoCitations;
				}
			}
		}
	}
	
	@Override
	public double computeDistanceBetween(SingleAuthorContainer firstAuthor,
			SingleAuthorContainer secondAuthor) {
		if(this.maxCoCitationsCount == 0) {
			return 0.0;
		}
		double noOfCoCitations = firstAuthor.getNumberOfCoCitations(secondAuthor);
		return noOfCoCitations / this.maxCoCitationsCount;
	}
	@Override
	public String getStrategyName() {
		return "Co-Citations Distance";
	}
	public String getStrategyKey() {
		return "CoCitations";
	}
}
