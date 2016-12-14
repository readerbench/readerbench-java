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

import data.AbstractDocument;
import data.Lang;
import data.Word;
import data.cscl.CSCLConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import services.comprehensionModel.utils.indexer.CMIndexer;
import services.comprehensionModel.utils.indexer.WordDistanceIndexer;
import services.comprehensionModel.utils.indexer.graphStruct.CMEdgeDO;
import services.comprehensionModel.utils.indexer.graphStruct.CMEdgeType;
import services.comprehensionModel.utils.indexer.graphStruct.CMGraphDO;
import services.comprehensionModel.utils.indexer.graphStruct.CMNodeDO;
import services.comprehensionModel.utils.indexer.graphStruct.CMNodeType;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LSA.LSA;

/**
 *
 * @author ionutparaschiv
 */
public class KeywordRelevanceCalculator {
    private final ISemanticModel semanticModel;
    private final double threshold;

    private CMGraphDO graph;
    
    public KeywordRelevanceCalculator(String text, ISemanticModel semanticModel, double threshold) {
        this.semanticModel = semanticModel;
        this.threshold = threshold;
        
        CMIndexer cmIndexer = new CMIndexer(text, semanticModel);
        this.graph = this.buildSemanticGraph(cmIndexer.getDocument());
        List<WordDistanceIndexer> syntacticIndexers = cmIndexer.getSyntacticIndexerList();
        for (WordDistanceIndexer indexer : syntacticIndexers) {
            this.graph.combineWithLinksFrom(indexer.getCMGraph(CMNodeType.TextBased));
        }
    }
    
    private CMGraphDO buildSemanticGraph(AbstractDocument document) {
        CMGraphDO semanticGraph = new CMGraphDO();
        List<Word> wordList = this.getWordList(document);
        wordList.forEach((word) -> {
            semanticGraph.addNodeIfNotExists(new CMNodeDO(word, CMNodeType.TextBased));
        });
        
        List<CMNodeDO> nodeList = semanticGraph.getNodeList();
        List<CMEdgeDO> edgeList = new ArrayList();
        for(int i = 0; i < nodeList.size(); i++) {
            for (int j = i + 1; j < nodeList.size(); j++) {
                double distance = this.semanticModel.getSimilarity(nodeList.get(i).getWord(), nodeList.get(j).getWord());
                if(distance >= this.threshold) {
                    CMEdgeDO edge = new CMEdgeDO(nodeList.get(i), nodeList.get(j), CMEdgeType.Semantic, distance);
                    edgeList.add(edge);
                }
            }
        }
        semanticGraph.setEdgeList(edgeList);
        return semanticGraph;
    }
    private List<Word> getWordList(AbstractDocument document) {
        Set<Word> wordSet = new TreeSet();
        document.getBlocks().forEach((block) -> {
            block.getSentences().forEach((sentence) -> {
                List<Word> wordList = sentence.getAllWords();
                wordList.forEach((word) -> {
                    wordSet.add(word);
                });
            });
        });
        return new ArrayList<>(wordSet);
    }
    
    public void logScores() {
        System.out.println(this.graph);
        
    }
    
    
    public static void main(String[] args) {
        String text = "A human can be a man or a woman. Cristian is a man.";
        ISemanticModel semanticModel = LSA.loadLSA(CSCLConstants.LSA_PATH, Lang.en);
        double threshold = 0.3;
        
        KeywordRelevanceCalculator calculator = new KeywordRelevanceCalculator(text, semanticModel, threshold);
        calculator.logScores();
    }
}