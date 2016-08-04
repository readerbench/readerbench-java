package services.comprehensionModel.utils.distanceStrategies;

import services.comprehensionModel.utils.distanceStrategies.utils.CMSyntacticGraph;
import services.comprehensionModel.utils.indexer.graphStruct.CMEdgeType;
import services.comprehensionModel.utils.indexer.graphStruct.CMEdgeDO;
import java.util.List;
import data.Word;

public class SyntacticWordDistanceStrategy implements IWordDistanceStrategy, java.io.Serializable {
	private static final long serialVersionUID = -8051746464532082314L;
	private List<CMEdgeDO> edgeList;
	
	public SyntacticWordDistanceStrategy(CMSyntacticGraph syntacticGraph) {
		this.edgeList = syntacticGraph.getEdgeList();
	}
	
	public double getDistance(Word w1, Word w2) {
		for (CMEdgeDO edge : this.edgeList) {
			Word dependentEdge = edge.node1.word;
			Word governorEdge = edge.node2.word;
			
			if( (dependentEdge.equals(w1) && governorEdge.equals(w2)) ||
					(dependentEdge.equals(w2) && governorEdge.equals(w1))) {
				return 1.0;
			}
		}
		return 0.0;
	}
	public CMEdgeType getCiEdgeType() {
		return CMEdgeType.Syntactic;
	}
}