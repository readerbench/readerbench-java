package services.comprehensionModel.utils.distanceStrategies;

import services.comprehensionModel.utils.indexer.graphStruct.CiEdgeType;
import data.Word;

public interface IWordDistanceStrategy {
	public double getDistance(Word w1, Word w2);
	public CiEdgeType getCiEdgeType();
}