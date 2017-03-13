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
import edu.stanford.nlp.math.ArrayMath;
import java.util.ArrayList;
import services.commons.VectorAlgebra;
import services.comprehensionModel.utils.ActivationScoreLogger;
import services.comprehensionModel.utils.indexer.CMIndexer;
import services.comprehensionModel.utils.indexer.WordDistanceIndexer;
import services.comprehensionModel.utils.indexer.graphStruct.CMEdgeDO;
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
        updatedNodeActivationScoreMap = this.normalizeActivationScoreMapWithMax(updatedNodeActivationScoreMap);
        Iterator<CMNodeDO> nodeIterator = updatedNodeActivationScoreMap.keySet().iterator();
        while (nodeIterator.hasNext()) {
            CMNodeDO node = nodeIterator.next();
            this.getNodeActivationScoreMap().put(node, updatedNodeActivationScoreMap.get(node));
        }
        this.activateWordsOverThreshold(updatedNodeActivationScoreMap);
        this.activationScoreLogger.saveScores(updatedNodeActivationScoreMap);
    }

    private void activateWordsOverThreshold(Map<CMNodeDO, Double> activationMap) {
        Iterator<CMNodeDO> nodeIterator = activationMap.keySet().iterator();
        while (nodeIterator.hasNext()) {
            CMNodeDO node = nodeIterator.next();
            Double value = activationMap.get(node);
            if (value < this.minActivationThreshold) {
                node.deactivate();
                List<CMEdgeDO> edgeList = this.currentGraph.getEdgeList(node);
                for (CMEdgeDO edge : edgeList) {
                    edge.deactivate();
                }
            } else {
                node.activate();
            }
        }
    }

    private Map<CMNodeDO, Double> normalizeActivationScoreMap(Map<CMNodeDO, Double> activationScoreMap) {
        List<CMNodeDO> nodes = new ArrayList<>();
        List<Double> nodeActivationScores = new ArrayList<>();

        Iterator<Map.Entry<CMNodeDO, Double>> activationScoreIt = activationScoreMap.entrySet().iterator();
        while (activationScoreIt.hasNext()) {
            Map.Entry<CMNodeDO, Double> entry = activationScoreIt.next();
            nodes.add(entry.getKey());
            nodeActivationScores.add(entry.getValue());
        }

        double[] nodeActivationScoresArray = new double[nodeActivationScores.size()];
        for (int i = 0; i < nodeActivationScores.size(); i++) {
            nodeActivationScoresArray[i] = nodeActivationScores.get(i);
        }
        nodeActivationScoresArray = ArrayMath.softmax(nodeActivationScoresArray);

        for (int i = 0; i < nodeActivationScores.size(); i++) {
            activationScoreMap.put(nodes.get(i), nodeActivationScoresArray[i]);
        }
        return this.normalizeActivationScoreMapWithMax(activationScoreMap);
    }

    private Map<CMNodeDO, Double> normalizeActivationScoreMapWithMax(Map<CMNodeDO, Double> activationScoreMap) {
        double maxActivationScore = this.getMaxActivationScore(activationScoreMap);
        Iterator<Map.Entry<CMNodeDO, Double>> activationScoreIt = activationScoreMap.entrySet().iterator();
        while (activationScoreIt.hasNext()) {
            Map.Entry<CMNodeDO, Double> entry = activationScoreIt.next();
            entry.setValue(entry.getValue() / maxActivationScore);
        }
        return activationScoreMap;
    }

    private double getMaxActivationScore(Map<CMNodeDO, Double> activationScoreMap) {
        double maxActivationScore = 0.0;
        for (double score : activationScoreMap.values()) {
            maxActivationScore = Math.max(maxActivationScore, score);
        }
        return maxActivationScore;
    }

//    private void pruneInferredConcepts(Map<CMNodeDO, Double> activationMap) {
//        double[] scores = new double[activationMap.values().size()];
//        int i = 0;
//        for (double value : activationMap.values()) {
//            scores [i ++] = value;
//        }
//        double mean = VectorAlgebra.avg(scores);
//        double stdev = VectorAlgebra.stdev(scores);
//        double filter = mean - stdev;
//        
//        // all the words < activationScore will be removed !
//        // the visible attr should not exist on the node
//        // if the Inferred node is  < activationScore => will be removed completely
//        List<CMNodeDO> nodeList =  new ArrayList(this.currentGraph.getNodeList());
//        nodeList.stream().forEach(node -> {
//            if(node.getNodeType() != CMNodeType.TextBased) {
//                double activationScore = activationMap.get(node);
//                if (activationScore < filter) {
//                    activationMap.put(node, 0.0);
//                    this.currentGraph.removeNode(node);
//                }
//            }
//        });
//    }
    public int getNoTopSimilarWords() {
        return this.noTopSimilarWords;
    }

    public ISemanticModel getSemanticModel() {
        return this.cmIndexer.getSemanticModel();
    }

    public void logSavedScores(CMGraphDO syntacticGraph, int sentenceIndex) {
        this.activationScoreLogger.saveNodes(syntacticGraph);
        this.activationScoreLogger.saveNodes(this.currentGraph);
        this.activationScoreLogger.logSavedScores();
    }
}
