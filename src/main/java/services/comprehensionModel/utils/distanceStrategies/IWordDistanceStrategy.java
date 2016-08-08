package services.comprehensionModel.utils.distanceStrategies;

import services.comprehensionModel.utils.indexer.graphStruct.CMEdgeType;
import data.Word;

public interface IWordDistanceStrategy {
	public double getDistance(Word w1, Word w2);
	public CMEdgeType getCMEdgeType();
}