package services.comprehensionModel.utils.distanceStrategies;

import services.comprehensionModel.utils.indexer.graphStruct.CiEdgeType;
import data.AbstractDocument;
import data.Word;
import data.discourse.SemanticCohesion;

public class SemanticWordDistanceStrategy implements IWordDistanceStrategy, java.io.Serializable {
	private static final long serialVersionUID = 3474474197995126405L;
	private AbstractDocument document;
	
	public SemanticWordDistanceStrategy(AbstractDocument document) {
		this.document = document;
	}
	
	public double getDistance(Word w1, Word w2) {
		double lsaSim = 0;
		double ldaSim = 0;
		if (this.document.getLSA() != null)
			lsaSim = this.document.getLSA().getSimilarity(w1, w2);
		if (this.document.getLDA() != null)
			ldaSim = this.document.getLDA().getSimilarity(w1, w2);
		double sim = SemanticCohesion.getAggregatedSemanticMeasure(lsaSim, ldaSim);
		return sim;
	}
	public CiEdgeType getCiEdgeType() {
		return CiEdgeType.Semantic;
	}
}