package services.comprehensionModel.utils.indexer.graphStruct;

public class CMEdgeDO {

    private final CMNodeDO node1;
    private final CMNodeDO node2;
    private final CMEdgeType edgeType;
    private final double score;

    public CMEdgeDO(CMNodeDO node1, CMNodeDO node2, CMEdgeType edgeType, double score) {
        this.node1 = node1;
        this.node2 = node2;
        this.edgeType = edgeType;
        this.score = score;
    }

    public CMNodeDO getNode1() {
        return node1;
    }

    public CMNodeDO getNode2() {
        return node2;
    }

    public CMEdgeType getEdgeType() {
        return edgeType;
    }

    public double getScore() {
        return score;
    }

    public boolean equals(CMEdgeDO otherEdge) {
        return ((this.node1.equals(otherEdge.node1) && this.node2.equals(otherEdge.node2))
                || (this.node1.equals(otherEdge.node2) && this.node2.equals(otherEdge.node1)))
                && this.edgeType == otherEdge.edgeType;
    }

    public String getEdgeTypeString() {
        return edgeType.toString();
    }

    public CMNodeDO getOppositeNode(CMNodeDO node) {
        if (this.node1.equals(node)) {
            return this.node2;
        }
        if (this.node2.equals(node)) {
            return this.node1;
        }
        return null;
    }
}
