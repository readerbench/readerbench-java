package services.comprehensionModel.utils.pageRank;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import services.comprehensionModel.utils.indexer.graphStruct.CMNodeDO;

public class NodeRank implements Comparable<NodeRank> {
	public CMNodeDO node;
	public Double value;

	@Override
	public int compareTo(NodeRank otherRank) {
		if(this.value < otherRank.value) {
			return -1;
		}
		if(this.value > otherRank.value) {
			return 1;
		}
		return 0;
	}
	
	public static List<NodeRank> convertMapToNodeRankList(Map<CMNodeDO, Double> nodeActivationScoreMap) {
		List<NodeRank> rankList = new ArrayList<NodeRank>();
		Iterator<CMNodeDO> nodeIterator = nodeActivationScoreMap.keySet().iterator();
		while(nodeIterator.hasNext()) {
			NodeRank rank = new NodeRank();
			rank.node = nodeIterator.next();
			rank.value = nodeActivationScoreMap.get(rank.node);
			rankList.add(rank);
		}
		return rankList;
		
	}
}
