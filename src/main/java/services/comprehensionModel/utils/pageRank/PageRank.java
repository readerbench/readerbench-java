package services.comprehensionModel.utils.pageRank;

import java.util.TreeMap;
import java.util.List;
import java.util.Map;

import services.comprehensionModel.utils.indexer.graphStruct.CMEdgeDO;
import services.comprehensionModel.utils.indexer.graphStruct.CMGraphDO;
import services.comprehensionModel.utils.indexer.graphStruct.CMNodeDO;

public class PageRank {
	public static int MAX_ITER = 100000;
	public static double EPS = 0.001;
	public static double PROB = 0.85;
	
	public PageRank() {
	}
	
	public Map<CMNodeDO, Double> runPageRank(Map<CMNodeDO, Double> pageRankValues, CMGraphDO graph) {
		int algIteration = 0;
		Map<CMNodeDO, Double> currentPageRankValues = new TreeMap<CMNodeDO, Double>(pageRankValues);
		while(algIteration < PageRank.MAX_ITER) {
			double r = this.calculateR(currentPageRankValues, graph);
			
			Map<CMNodeDO, Double> tempPageRankValues = new TreeMap<CMNodeDO, Double>();
			boolean done = true;
			for(CMNodeDO node : graph.nodeList) {
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
	
	private double calculateR(Map<CMNodeDO, Double> pageRankValues, CMGraphDO graph) {
		double r = 0;
		double N = (double)graph.nodeList.size();
		for(CMNodeDO node : graph.nodeList) {
			List<CMEdgeDO> nodeEdgeList = graph.getEdgeList(node);
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
	
	private double computeTempPageRankValue(Map<CMNodeDO, Double> pageRankValues, CMGraphDO graph, CMNodeDO node, double r) {
		double res = r;
		List<CMEdgeDO> nodeEdgeList = graph.getEdgeList(node);
		for(CMEdgeDO edge: nodeEdgeList) {
			CMNodeDO neighbor = edge.getOppositeNode(node);
			List<CMEdgeDO> neighborEdgeList = graph.getEdgeList(neighbor);
			double normalize = (double)neighborEdgeList.size();
			res += PageRank.PROB * (this.getPageRankValue(pageRankValues, neighbor, graph) / normalize);
		}
		return res;
	}
	
	private double getPageRankValue(Map<CMNodeDO, Double> pageRankValues, CMNodeDO node, CMGraphDO graph) {
		if(pageRankValues.containsKey(node)) {
			return pageRankValues.get(node);
		}
		return 1/((double)graph.nodeList.size());
	}
}