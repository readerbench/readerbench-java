package services.comprehensionModel.utils.indexer.graphStruct;

import java.util.ArrayList;
import java.util.List;

public class CiGraphDO {
	public List<CiNodeDO> nodeList;
	public List<CiEdgeDO> edgeList;
	
	public CiGraphDO() {
		this.nodeList = new ArrayList<CiNodeDO>();
		this.edgeList = new ArrayList<CiEdgeDO>();
	}
	
	public static boolean nodeListContainsNode(List<CiNodeDO> nodeList, CiNodeDO otherNode) {
		for(CiNodeDO node : nodeList) {
			if(node.equals(otherNode)) {
				return true;
			}
		}
		return false;
	}
	public static boolean edgeListContainsEdge(List<CiEdgeDO> edgeList, CiEdgeDO otherEdge) {
		for(CiEdgeDO edge : edgeList) {
			if(edge.equals(otherEdge)) {
				return true;
			}
		}
		return false;
	}
	
	
	public boolean containsNode(CiNodeDO otherNode) {
		return nodeListContainsNode(this.nodeList, otherNode);
	}
	private void addNodeIfNotExists(CiNodeDO otherNode) {
		if(!this.containsNode(otherNode)) {
			this.nodeList.add(otherNode);
		}
	}
	
	private boolean containsEdge(CiEdgeDO otherEdge) {
		return edgeListContainsEdge(this.edgeList, otherEdge);
	}
	public List<CiEdgeDO> getEdgeList(CiNodeDO node) {
		List<CiEdgeDO> outEdgeList = new ArrayList<CiEdgeDO>();
		for(CiEdgeDO edge : this.edgeList) {
			if(edge.node1.equals(node) || edge.node2.equals(node)) {
				outEdgeList.add(edge);
			}
		}
		return outEdgeList;
	}
	
	public void combineWithLinksFrom(CiGraphDO otherGraph) {
		List<CiNodeDO> thisNodeList = new ArrayList<CiNodeDO>(this.nodeList);
		for(CiNodeDO node: thisNodeList) {
			List<CiEdgeDO> otherGraphEdgeList = otherGraph.getEdgeList(node);
			for(CiEdgeDO otherGraphEdge : otherGraphEdgeList) {
				if(!this.containsEdge(otherGraphEdge)) {
					this.addNodeIfNotExists(otherGraphEdge.node1);
					this.addNodeIfNotExists(otherGraphEdge.node2);
					this.edgeList.add(otherGraphEdge);
				}
			}
		}
	}
	public CiGraphDO getCombinedGraph(CiGraphDO otherGraph) {
		List<CiNodeDO> thisNodeList = new ArrayList<CiNodeDO>(this.nodeList);
		for(CiNodeDO otherGraphNode : otherGraph.nodeList) {
			if(!nodeListContainsNode(thisNodeList, otherGraphNode)) {
				thisNodeList.add(otherGraphNode);
			}
		}
		
		List<CiEdgeDO> thisEdgeList = new ArrayList<CiEdgeDO>(this.edgeList);
		for(CiEdgeDO otherGraphEdge : otherGraph.edgeList) {
			if(!edgeListContainsEdge(thisEdgeList, otherGraphEdge)) {
				thisEdgeList.add(otherGraphEdge);
			}
		}
		
		CiGraphDO outGraph = new CiGraphDO();
		outGraph.nodeList = thisNodeList;
		outGraph.edgeList = thisEdgeList;
				
		return outGraph;
	}
}