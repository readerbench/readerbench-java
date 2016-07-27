package services.comprehensionModel.utils.indexer.graphStruct;

public class CMEdgeDO {
	public CMNodeDO node1;
	public CMNodeDO node2;
	public double score;
	public CMEdgeType edgeType;
	
	public boolean equals(CMEdgeDO otherEdge) {
		if( ((this.node1.equals(otherEdge.node1) && this.node2.equals(otherEdge.node2)) ||
			 (this.node1.equals(otherEdge.node2) && this.node2.equals(otherEdge.node1))) &&
			this.edgeType == otherEdge.edgeType
				) {
			return true;
		}
		return false;
	}
	
	public String getEdgeTypeString() {
		if(this.edgeType == CMEdgeType.Semantic)
			return "Semantic";
		return "Syntactic";
	}
	
	public CMNodeDO getOppositeNode(CMNodeDO node) {
		if(this.node1.equals(node)) {
			return this.node2;
		}
		if(this.node2.equals(node)) {
			return this.node1;
		}
		return null;
	}
}