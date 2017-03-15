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

import data.Sentence;
import services.comprehensionModel.utils.ActivationScoreLogger;
import services.comprehensionModel.utils.indexer.CMIndexer;
import services.comprehensionModel.utils.indexer.WordDistanceIndexer;
import services.comprehensionModel.utils.indexer.graphStruct.CMGraphDO;
import services.comprehensionModel.utils.pageRank.PageRank;
import services.semanticModels.ISemanticModel;

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

    public void applyPageRank(int sentenceIndex) {
        PageRank pageRank = new PageRank();
        pageRank.runPageRank(this.currentGraph);
        this.normalizeActivationScoreMapWithMax();
        this.activateWordsOverThreshold();
        this.activationScoreLogger.saveScores(this.currentGraph.getActivationMap());
    }

    private void activateWordsOverThreshold() {
        this.currentGraph.getNodeList().stream().forEach(node -> {
            if (node.getActivationScore() < this.minActivationThreshold) {
                node.deactivate();
                this.currentGraph.getEdgeList(node).stream().forEach(edge -> {
                    edge.deactivate();
                });
            } else {
                node.activate();
            }
        });
    }

    private void normalizeActivationScoreMapWithMax() {
        double maxValue = this.currentGraph.getNodeList()
                .stream()
                .filter(node -> node.isActive())
                .map(node -> {
                    return node.getActivationScore();
                })
                .max(Double::compare).get();

        if (maxValue == 0.0) {
            return;
        }

        this.currentGraph.getNodeList()
                .stream()
                .filter(node -> node.isActive())
                .forEach(node -> {
                    double normalizedActivationScore = node.getActivationScore() / maxValue;
                    node.setActivationScore(normalizedActivationScore);
                });
    }

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
