package services.comprehensionModel.utils.pageRank;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import services.comprehensionModel.utils.indexer.graphStruct.CiEdgeDO;
import services.comprehensionModel.utils.indexer.graphStruct.CiGraphDO;
import services.comprehensionModel.utils.indexer.graphStruct.CiNodeDO;

public class PageRank {
	public static int MAX_ITER = 100000;
	public static double EPS = 0.001;
	public static double PROB = 0.85;
	
	public PageRank() {
	}
	
	public Map<CiNodeDO, Double> runPageRank(Map<CiNodeDO, Double> pageRankValues, CiGraphDO graph) {
		int algIteration = 0;
		Map<CiNodeDO, Double> currentPageRankValues = new HashMap<CiNodeDO, Double>(pageRankValues);
		while(algIteration < PageRank.MAX_ITER) {
			double r = this.calculateR(currentPageRankValues, graph);
			
			Map<CiNodeDO, Double> tempPageRankValues = new HashMap<CiNodeDO, Double>();
			boolean done = true;
			for(CiNodeDO node : graph.nodeList) {
				double tempPRValue = this.computeTempPageRankValue(currentPageRankValues, graph, node, r);
				double prevPRValue = this.getPageRankValue(currentPageRankValues, node, graph);
				
				tempPageRankValues.put(node, tempPRValue);
				
				if ((tempPRValue - prevPRValue) / prevPRValue >= PageRank.EPS) {
                    done = false;
                }
			}
			currentPageRankValues = tempPageRankValues;
			if(done) {
				break;
			}
			algIteration ++;
		}
		return currentPageRankValues;
	}
	
	private double calculateR(Map<CiNodeDO, Double> pageRankValues, CiGraphDO graph) {
		double r = 0;
		double N = (double)graph.nodeList.size();
		for(CiNodeDO node : graph.nodeList) {
			List<CiEdgeDO> nodeEdgeList = graph.getEdgeList(node);
			double nodeDegree = (double)nodeEdgeList.size();
			double nodePageRankVal = this.getPageRankValue(pageRankValues, node, graph);
			if(nodeDegree > 0) {
				r += (1.0 - PageRank.PROB) * (nodePageRankVal / N);
			}
			else {
				r += (nodePageRankVal / N);
			}
		}
		return r;
	}
	
	private double computeTempPageRankValue(Map<CiNodeDO, Double> pageRankValues, CiGraphDO graph, CiNodeDO node, double r) {
		double res = r;
		List<CiEdgeDO> nodeEdgeList = graph.getEdgeList(node);
		for(CiEdgeDO edge: nodeEdgeList) {
			CiNodeDO neighbor = edge.getOppositeNode(node);
			List<CiEdgeDO> neighborEdgeList = graph.getEdgeList(neighbor);
			double normalize = (double)neighborEdgeList.size();
			res += PageRank.PROB * (this.getPageRankValue(pageRankValues, neighbor, graph) / normalize);
		}
		return res;
	}
	
	private double getPageRankValue(Map<CiNodeDO, Double> pageRankValues, CiNodeDO node, CiGraphDO graph) {
		if(pageRankValues.containsKey(node)) {
			return pageRankValues.get(node);
		}
		return 1/((double)graph.nodeList.size());
	}
}