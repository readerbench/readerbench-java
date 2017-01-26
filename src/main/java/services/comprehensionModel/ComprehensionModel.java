/* 
 * Copyright 2016 ReaderBench.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package services.comprehensionModel;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import data.Sentence;
import services.comprehensionModel.utils.ActivationScoreLogger;
import services.comprehensionModel.utils.indexer.CMIndexer;
import services.comprehensionModel.utils.indexer.WordDistanceIndexer;
import services.comprehensionModel.utils.indexer.graphStruct.CMGraphDO;
import services.comprehensionModel.utils.indexer.graphStruct.CMNodeDO;
import services.comprehensionModel.utils.indexer.graphStruct.CMNodeType;
import services.comprehensionModel.utils.pageRank.NodeRank;
import services.comprehensionModel.utils.pageRank.PageRank;
import services.semanticModels.ISemanticModel;
import services.semanticModels.utils.WordSimilarityContainer;

public class ComprehensionModel {

    private final double minActivationThreshold;
    private final int maxNoActiveWords;
    private final int maxNoActiveWordsIncrement;
    private final int noTopSimilarWords;
    private final ActivationScoreLogger activationScoreLogger;

    private final CMIndexer cmIndexer;
    private CMGraphDO currentGraph;

    public ComprehensionModel(String text, ISemanticModel semModel, double semanticThreshold, int noTopSimilarWords, double minActivationThreshold,
            int maxNoActiveWords, int maxNoActiveWordsIncrement) {
        this.cmIndexer = new CMIndexer(text, semModel, semanticThreshold, noTopSimilarWords);
        this.currentGraph = new CMGraphDO();
        this.minActivationThreshold = minActivationThreshold;
        this.maxNoActiveWords = maxNoActiveWords;
        this.maxNoActiveWordsIncrement = maxNoActiveWordsIncrement;
        this.activationScoreLogger = new ActivationScoreLogger();
        this.noTopSimilarWords = noTopSimilarWords;
    }

    public WordSimilarityContainer getWordSimilarityContainer() {
        return this.cmIndexer.getWordSimilarityContainer();
    }

    public CMGraphDO getCurrentGraph() {
        return currentGraph;
    }

    public void setCurrentGraph(CMGraphDO currentGraph) {
        this.currentGraph = currentGraph;
    }
    
    public int getTotalNoOfPhrases() {
        return this.cmIndexer.getSyntacticIndexerList().size();
    }

    public Sentence getSentenceAtIndex(int index) {
        return this.cmIndexer.document.getSentencesInDocument().get(index);
    }

    public WordDistanceIndexer getSyntacticIndexerAtIndex(int index) {
        return this.cmIndexer.getSyntacticIndexerList().get(index);
    }

    public Map<CMNodeDO, Double> getNodeActivationScoreMap() {
        return this.cmIndexer.getNodeActivationScoreMap();
    }

    public void updateActivationScoreMapAtIndex(int index) {
        WordDistanceIndexer indexer = this.getSyntacticIndexerAtIndex(index);
        for (int i = 0; i < indexer.getWordList().size(); i++) {
            CMNodeDO node = new CMNodeDO(indexer.getWordList().get(i), CMNodeType.TextBased);
            double score = this.getNodeActivationScoreMap().get(node);
            score++;
            this.getNodeActivationScoreMap().put(node, score);
        }
        this.currentGraph.getNodeList().stream().filter((otherNode) -> (!this.getNodeActivationScoreMap().containsKey(otherNode))).forEach((otherNode) -> {
            this.getNodeActivationScoreMap().put(otherNode, 0.0);
        });
    }

    public void markAllNodesAsInactive() {
        this.currentGraph.getNodeList().stream().forEach((node) -> {
            node.deactivate();
        });
    }

    public void applyPageRank(int sentenceIndex) {
        PageRank pageRank = new PageRank();
        Map<CMNodeDO, Double> updatedNodeActivationScoreMap = pageRank.runPageRank(this.getNodeActivationScoreMap(),
                this.currentGraph);

        int maxWords = this.maxNoActiveWords + (sentenceIndex * this.maxNoActiveWordsIncrement);

        List<NodeRank> nodeRankList = NodeRank.convertMapToNodeRankList(updatedNodeActivationScoreMap);
        Collections.sort(nodeRankList, Collections.reverseOrder());

        this.activateFirstWords(//updatedNodeActivationScoreMap, 
                nodeRankList, maxWords);

        Iterator<CMNodeDO> nodeIterator = updatedNodeActivationScoreMap.keySet().iterator();
        while (nodeIterator.hasNext()) {
            CMNodeDO node = nodeIterator.next();
            this.getNodeActivationScoreMap().put(node, updatedNodeActivationScoreMap.get(node));
        }

        this.activationScoreLogger.saveScores(updatedNodeActivationScoreMap);
    }

    private void activateFirstWords(//Map<CMNodeDO, Double> updatedNodeActivationScoreMap, 
            List<NodeRank> nodeRankList, int maxWords) {
        int noActivatedWord = 0;
        //Set<CMNodeDO> activeNodeSet = new TreeSet<>();
        for (NodeRank nodeRank : nodeRankList) {
            if (nodeRank.getValue() < this.minActivationThreshold) {
                break;
            }
            for (CMNodeDO currentNode : this.currentGraph.getNodeList()) {
                if (currentNode.equals(nodeRank.getNode())) {
                    currentNode.activate();
//                    activeNodeSet.add(currentNode);
                    noActivatedWord++;
                    break;
                }
            }
            if (noActivatedWord >= maxWords) {
                break;
            }
        }
//		updatedNodeActivationScoreMap.keySet().forEach((node) -> {
//			if (!activeNodeSet.contains(node)) {
//				double oldValue = updatedNodeActivationScoreMap.get(node);
//				updatedNodeActivationScoreMap.put(node, oldValue + 1.0);
//			} else {
//				updatedNodeActivationScoreMap.put(node, 0.0);
//			}
//		});
    }

    public int getNoTopSimilarWords() {
        return this.noTopSimilarWords;
    }
    
    public void logSavedScores(CMGraphDO syntacticGraph, int sentenceIndex) {
        this.activationScoreLogger.saveNodes(syntacticGraph);
        this.activationScoreLogger.saveNodes(this.currentGraph);
        this.activationScoreLogger.logSavedScores();
    }
}
