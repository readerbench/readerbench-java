package services.comprehensionModel.utils.distanceStrategies.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import data.Word;
import services.comprehensionModel.utils.CMUtils;
import services.comprehensionModel.utils.indexer.graphStruct.CMEdgeDO;
import services.comprehensionModel.utils.indexer.graphStruct.CMEdgeType;
import services.comprehensionModel.utils.indexer.graphStruct.CMNodeDO;
import services.comprehensionModel.utils.indexer.graphStruct.CMNodeType;

public class CMSyntacticGraph {
	private CMUtils cmUtils;
	private List<CMEdgeDO> edgeList;
	private Set<Word> wordSet;
	
	public CMSyntacticGraph() {
		this.cmUtils = new CMUtils();
		this.edgeList = new ArrayList<>();
		this.wordSet = new TreeSet<Word>();
	}
	
	public void indexEdge(Word word1, Word word2) {
		CMNodeDO node1 = new CMNodeDO();
		node1.word = word1;
		node1.nodeType = CMNodeType.Syntactic;
		
		CMNodeDO node2 = new CMNodeDO();
		node2.word = word2;
		node2.nodeType = CMNodeType.Syntactic;
		
		CMEdgeDO edge = new CMEdgeDO();
		edge.node1 = node1;
		edge.node2 = node2;
		edge.edgeType = CMEdgeType.Syntactic;
		edge.score = 1.0;
		this.edgeList.add(edge);
		
		this.wordSet.add(word1);
		this.wordSet.add(word2);
	}
	
	
	public List<Word> getWordList() {
		return this.cmUtils.convertIteratorToList(this.wordSet.iterator());
	}
	
	public List<CMEdgeDO> getEdgeList() {
		return this.edgeList;
	}	
}
