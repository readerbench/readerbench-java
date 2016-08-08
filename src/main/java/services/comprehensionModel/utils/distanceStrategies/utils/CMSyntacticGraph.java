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

    private final CMUtils cmUtils;
    private final List<CMEdgeDO> edgeList;
    private final Set<Word> wordSet;

    public CMSyntacticGraph() {
        this.cmUtils = new CMUtils();
        this.edgeList = new ArrayList<>();
        this.wordSet = new TreeSet<>();
    }

    public void indexEdge(Word word1, Word word2) {
        CMNodeDO node1 = new CMNodeDO(word1, CMNodeType.TextBased);
        CMNodeDO node2 = new CMNodeDO(word2, CMNodeType.TextBased);
        CMEdgeDO edge = new CMEdgeDO(node1, node2, CMEdgeType.Syntactic, 1.0d);
        this.wordSet.add(word1);
        this.wordSet.add(word2);
        this.edgeList.add(edge);
    }

    public List<Word> getWordList() {
        return this.cmUtils.convertIteratorToList(this.wordSet.iterator());
    }

    public List<CMEdgeDO> getEdgeList() {
        return this.edgeList;
    }
}
