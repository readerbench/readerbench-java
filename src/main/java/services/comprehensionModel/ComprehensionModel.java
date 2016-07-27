package services.comprehensionModel;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import data.Lang;
import data.Sentence;
import services.comprehensionModel.utils.ActivationScoreLogger;
import services.comprehensionModel.utils.indexer.QueryIndexer;
import services.comprehensionModel.utils.indexer.WordDistanceIndexer;
import services.comprehensionModel.utils.indexer.graphStruct.CMGraphDO;
import services.comprehensionModel.utils.indexer.graphStruct.CMNodeDO;
import services.comprehensionModel.utils.indexer.graphStruct.CMNodeType;
import services.comprehensionModel.utils.pageRank.NodeRank;
import services.comprehensionModel.utils.pageRank.PageRank;
import services.semanticModels.LDA.LDA;

public class ComprehensionModel {
	private double minActivationThreshold;
	private int maxNoActiveWords;
	private int maxNoActiveWordsIncrement;
	private ActivationScoreLogger activationScoreLogger;

	private QueryIndexer queryIndexer;
	public CMGraphDO currentGraph;

	public ComprehensionModel(String text, int hdpGrade, int noTopSimilarWords, double minActivationThreshold,
			int maxNoActiveWords, int maxNoActiveWordsIncrement) {

		this.queryIndexer = new QueryIndexer(text, LDA.loadLDA("resources/in/HDP/grade" + hdpGrade, Lang.eng),
				noTopSimilarWords);
		this.currentGraph = new CMGraphDO();
		this.minActivationThreshold = minActivationThreshold;
		this.maxNoActiveWords = maxNoActiveWords;
		this.maxNoActiveWordsIncrement = maxNoActiveWordsIncrement;
		this.activationScoreLogger = new ActivationScoreLogger();
	}

	public WordDistanceIndexer getSemanticIndexer() {
		return this.queryIndexer.getSemanticIndexer();
	}

	public int getTotalNoOfPhrases() {
		return this.queryIndexer.getSyntacticIndexerList().size();
	}

	public Sentence getSentenceAtIndex(int index) {
		return this.queryIndexer.document.getSentencesInDocument().get(index);
	}

	public WordDistanceIndexer getSyntacticIndexerAtIndex(int index) {
		return this.queryIndexer.getSyntacticIndexerList().get(index);
	}

	public Map<CMNodeDO, Double> getNodeActivationScoreMap() {
		return this.queryIndexer.getNodeActivationScoreMap();
	}

	public void updateActivationScoreMapAtIndex(int index) {
		WordDistanceIndexer indexer = this.getSyntacticIndexerAtIndex(index);
		for (int i = 0; i < indexer.wordList.size(); i++) {
			CMNodeDO node = new CMNodeDO();
			node.nodeType = CMNodeType.Syntactic;
			node.word = indexer.wordList.get(i);
			double score = this.getNodeActivationScoreMap().get(node);
			score++;
			this.getNodeActivationScoreMap().put(node, score);
		}
		for (CMNodeDO otherNode : this.currentGraph.nodeList) {
			if (!this.getNodeActivationScoreMap().containsKey(otherNode)) {
				this.getNodeActivationScoreMap().put(otherNode, 0.0);
			}
		}
	}

	public void markAllNodesAsInactive() {
		for (CMNodeDO node : this.currentGraph.nodeList) {
			node.nodeType = CMNodeType.Inactive;
		}
	}

	public void applyPageRank(int sentenceIndex) {
		PageRank pageRank = new PageRank();
		Map<CMNodeDO, Double> updatedNodeActivationScoreMap = pageRank.runPageRank(this.getNodeActivationScoreMap(),
				this.currentGraph);

		int maxWords = this.maxNoActiveWords + (sentenceIndex * this.maxNoActiveWordsIncrement);

		List<NodeRank> nodeRankList = NodeRank.convertMapToNodeRankList(updatedNodeActivationScoreMap);
		Collections.sort(nodeRankList, Collections.reverseOrder());
		
		System.out.println(updatedNodeActivationScoreMap);
		this.activateFirstWords(updatedNodeActivationScoreMap, nodeRankList, maxWords);
		System.out.println(updatedNodeActivationScoreMap);
		
		Iterator<CMNodeDO> nodeIterator = updatedNodeActivationScoreMap.keySet().iterator();
		while (nodeIterator.hasNext()) {
			CMNodeDO node = nodeIterator.next();
			this.getNodeActivationScoreMap().put(node, updatedNodeActivationScoreMap.get(node));
		}

		this.activationScoreLogger.saveScores(updatedNodeActivationScoreMap);
	}

	private void activateFirstWords(Map<CMNodeDO, Double> updatedNodeActivationScoreMap, List<NodeRank> nodeRankList,
			int maxWords) {
		int noActivatedWord = 0;
		Set<CMNodeDO> activeNodeSet = new TreeSet<CMNodeDO>();
		for (NodeRank nodeRank : nodeRankList) {
			if (nodeRank.value < this.minActivationThreshold) {
				break;
			}
			for (CMNodeDO currentNode : this.currentGraph.nodeList) {
				if (currentNode.equals(nodeRank.node)) {
					if (currentNode.nodeType == CMNodeType.Inactive) {
						currentNode.nodeType = CMNodeType.Active;
					}
					activeNodeSet.add(currentNode);
					noActivatedWord++;
					break;
				}
			}
			if (noActivatedWord >= maxWords) {
				break;
			}
		}
		updatedNodeActivationScoreMap.keySet().forEach((node) -> {
			if (activeNodeSet.contains(node)) {
				double oldValue = updatedNodeActivationScoreMap.get(node);
				updatedNodeActivationScoreMap.put(node, oldValue + 1.0);
			} else {
				updatedNodeActivationScoreMap.put(node, 0.0);
			}
		});
	}

	public void logSavedScores(CMGraphDO syntacticGraph) {
		this.activationScoreLogger.saveNodes(syntacticGraph);
		this.activationScoreLogger.saveNodes(this.currentGraph);
		this.activationScoreLogger.logSavedScores();
	}
}