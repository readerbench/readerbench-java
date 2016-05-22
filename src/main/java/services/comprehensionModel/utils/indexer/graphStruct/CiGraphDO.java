package services.comprehensionModel.utils.indexer.graphStruct;

import java.util.ArrayList;
import java.util.List;
import data.Word;

public class CiGraphDO {
	public List<Word> wordList;
	public List<CiEdgeDO> edgeList;
	
	public boolean containsWord(Word otherWord) {
		for(Word word : this.wordList) {
			if(word.equals(otherWord)) {
				return true;
			}
		}
		return false;
	}
	private void addWordIfNotExists(Word otherWord) {
		if(!this.containsWord(otherWord)) {
			this.wordList.add(otherWord);
		}
	}
	
	private boolean containsEdge(CiEdgeDO otherEdge) {
		for(CiEdgeDO edge : edgeList) {
			if(edge.equals(otherEdge)) {
				return true;
			}
		}
		return false;
	}
	public List<CiEdgeDO> getEdgeList(Word word) {
		List<CiEdgeDO> outEdgeList = new ArrayList<CiEdgeDO>();
		for(CiEdgeDO edge : this.edgeList) {
			if(edge.w1.equals(word) || edge.w2.equals(word)) {
				outEdgeList.add(edge);
			}
		}
		return outEdgeList;
	}
	
	public void combineWithLinksFrom(CiGraphDO otherGraph) {
		List<Word> thisWordList = new ArrayList<Word>(this.wordList);
		for(Word word : thisWordList) {
			List<CiEdgeDO> otherGraphEdgeList = otherGraph.getEdgeList(word);
			for(CiEdgeDO otherGraphEdge : otherGraphEdgeList) {
				if(!this.containsEdge(otherGraphEdge)) {
					this.addWordIfNotExists(otherGraphEdge.w1);
					this.addWordIfNotExists(otherGraphEdge.w2);
					this.edgeList.add(otherGraphEdge);
				}
			}
		}
	}
}