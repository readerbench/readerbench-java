package services.comprehensionModel.utils.distanceStrategies;

import services.comprehensionModel.utils.distanceStrategies.utils.CMSyntacticGraph;
import services.comprehensionModel.utils.indexer.graphStruct.CMEdgeType;
import services.comprehensionModel.utils.indexer.graphStruct.CMEdgeDO;
import java.util.List;
import data.Word;

public class SyntacticWordDistanceStrategy implements IWordDistanceStrategy, java.io.Serializable {

    private static final long serialVersionUID = -8051746464532082314L;
    private final List<CMEdgeDO> edgeList;

    public SyntacticWordDistanceStrategy(CMSyntacticGraph syntacticGraph) {
        this.edgeList = syntacticGraph.getEdgeList();
    }

    @Override
    public double getDistance(Word w1, Word w2) {
        for (CMEdgeDO edge : this.edgeList) {
            Word dependentEdge = edge.getNode1().getWord();
            Word governorEdge = edge.getNode2().getWord();

            if ((dependentEdge.equals(w1) && governorEdge.equals(w2))
                    || (dependentEdge.equals(w2) && governorEdge.equals(w1))) {
                return 1.0;
            }
        }
        return 0.0;
    }

    @Override
    public CMEdgeType getCMEdgeType() {
        return CMEdgeType.Syntactic;
    }
}
