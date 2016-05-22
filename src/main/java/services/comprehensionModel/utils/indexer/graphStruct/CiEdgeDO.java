package services.comprehensionModel.utils.indexer.graphStruct;

import data.Word;

public class CiEdgeDO {
	public Word w1;
	public Word w2;
	public double score;
	public CiEdgeType edgeType;
	
	public boolean equals(CiEdgeDO otherEdge) {
		if( ((this.w1.equals(otherEdge.w1) && this.w2.equals(otherEdge.w2)) ||
			 (this.w1.equals(otherEdge.w2) && this.w2.equals(otherEdge.w1))) &&
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