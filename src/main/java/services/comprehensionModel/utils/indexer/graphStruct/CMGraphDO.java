package services.comprehensionModel.utils.indexer.graphStruct;

import java.util.ArrayList;
import java.util.List;

public class CMGraphDO {
	public List<CMNodeDO> nodeList;
	public List<CMEdgeDO> edgeList;
	
	public CMGraphDO() {
		this.nodeList = new ArrayList<CMNodeDO>();
		this.edgeList = new ArrayList<CMEdgeDO>();
	}
	
	public static boolean nodeListContainsNode(List<CMNodeDO> nodeList, CMNodeDO otherNode) {
		for(CMNodeDO node : nodeList) {
			if(node.equals(otherNode)) {
				return true;
			}
		}
		return false;
	}
	public static boolean edgeListContainsEdge(List<CMEdgeDO> edgeList, CMEdgeDO otherEdge) {
		for(CMEdgeDO edge : edgeList) {
			if(edge.equals(otherEdge)) {
				return true;
			}
		}
		return false;
	}
	
	
	public boolean containsNode(CMNodeDO otherNode) {
		return nodeListContainsNode(this.nodeList, otherNode);
	}
	private void addNodeIfNotExists(CMNodeDO otherNode) {
		if(!this.containsNode(otherNode)) {
			this.nodeList.add(otherNode);
		}
	}
	
	private boolean containsEdge(CMEdgeDO otherEdge) {
		return edgeListContainsEdge(this.edgeList, otherEdge);
	}
	public List<CMEdgeDO> getEdgeList(CMNodeDO node) {
		List<CMEdgeDO> outEdgeList = new ArrayList<CMEdgeDO>();
		for(CMEdgeDO edge : this.edgeList) {
			if(edge.node1.equals(node) || edge.node2.equals(node)) {
				outEdgeList.add(edge);
			}
		}
		return outEdgeList;
	}
	
	public void combineWithLinksFrom(CMGraphDO otherGraph) {
		List<CMNodeDO> thisNodeList = new ArrayList<CMNodeDO>(this.nodeList);
		for(CMNodeDO node: thisNodeList) {
			List<CMEdgeDO> otherGraphEdgeList = otherGraph.getEdgeList(node);
			
			for(CMEdgeDO otherGraphEdge : otherGraphEdgeList) {
				// add direct nodes with links
				if(!this.containsEdge(otherGraphEdge)) {
					this.addNodeIfNotExists(otherGraphEdge.node1);
					this.addNodeIfNotExists(otherGraphEdge.node2);
					this.edgeList.add(otherGraphEdge);
				}
			}
		}
		// add missing links from the second graph
		thisNodeList = new ArrayList<CMNodeDO>(this.nodeList);
		for(CMNodeDO node: thisNodeList) {
			List<CMEdgeDO> otherGraphEdgeList = otherGraph.getEdgeList(node);
			for(CMEdgeDO otherGraphEdge : otherGraphEdgeList) {
				// add direct nodes with links
				if(!this.containsEdge(otherGraphEdge) && this.containsNode(otherGraphEdge.node1) 
						&& this.containsNode(otherGraphEdge.node2)) {
					this.edgeList.add(otherGraphEdge);
				}
			}
		}
	}
	public CMGraphDO getCombinedGraph(CMGraphDO otherGraph) {
		List<CMNodeDO> thisNodeList = new ArrayList<CMNodeDO>(this.nodeList);
		for(CMNodeDO otherGraphNode : otherGraph.nodeList) {
			if(!nodeListContainsNode(thisNodeList, otherGraphNode)) {
				thisNodeList.add(otherGraphNode);
			}
		}
		
		List<CMEdgeDO> thisEdgeList = new ArrayList<CMEdgeDO>(this.edgeList);
		for(CMEdgeDO otherGraphEdge : otherGraph.edgeList) {
			if(!edgeListContainsEdge(thisEdgeList, otherGraphEdge)) {
				thisEdgeList.add(otherGraphEdge);
			}
		}
		
		CMGraphDO outGraph = new CMGraphDO();
		outGraph.nodeList = thisNodeList;
		outGraph.edgeList = thisEdgeList;
				
		return outGraph;
	}
}