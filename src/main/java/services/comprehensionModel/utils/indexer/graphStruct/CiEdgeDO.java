package services.comprehensionModel.utils.indexer.graphStruct;

public class CiEdgeDO {
	public CiNodeDO node1;
	public CiNodeDO node2;
	public double score;
	public CiEdgeType edgeType;
	
	public boolean equals(CiEdgeDO otherEdge) {
		if( ((this.node1.equals(otherEdge.node1) && this.node2.equals(otherEdge.node2)) ||
			 (this.node1.equals(otherEdge.node2) && this.node2.equals(otherEdge.node1))) &&
			this.edgeType == otherEdge.edgeType
				) {
			return true;
		}
		return false;
	}
	
	public String getEdgeTypeString() {
		if(this.edgeType == CiEdgeType.Semantic)
			return "Semantic";
		return "Syntactic";
	}
}