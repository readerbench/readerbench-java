/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webService.services.cimodel;

import data.Lang;
import data.Sentence;
import data.Word;
import java.util.List;
import java.util.Map;
import services.comprehensionModel.ComprehensionModel;
import services.comprehensionModel.utils.ActivationScoreLogger;
import services.comprehensionModel.utils.WordActivation;
import services.comprehensionModel.utils.indexer.WordDistanceIndexer;
import services.comprehensionModel.utils.indexer.graphStruct.CMEdgeType;
import services.comprehensionModel.utils.indexer.graphStruct.CMGraphDO;
import services.comprehensionModel.utils.indexer.graphStruct.CMNodeDO;
import services.comprehensionModel.utils.indexer.graphStruct.CMNodeType;
import services.semanticModels.LSA.LSA;
import view.widgets.article.utils.distanceStrategies.AuthorDistanceStrategyType;
import webService.services.cimodel.result.CMResult;
import webService.services.cimodel.result.CMSentence;
import webService.services.cimodel.result.CMWordActivationResult;
import webService.services.cimodel.result.CMWordResult;
import webService.services.lak.result.TwoModeGraph;
import webService.services.lak.result.TwoModeGraphEdge;
import webService.services.lak.result.TwoModeGraphNode;
import webService.services.lak.result.TwoModeGraphNodeType;

/**
 *
 * @author ionutparaschiv
 */
public class ComprehensionModelService {
    private final double minActivationThreshold;
    private final int maxSemanticExpand;
    
    public ComprehensionModelService(double minActivationThreshold, int maxSemanticExpand) {
        this.minActivationThreshold = minActivationThreshold;
        this.maxSemanticExpand = maxSemanticExpand;
    }
    
    public CMResult run(String sentenceTest) {
        CMResult result = new CMResult();
        ComprehensionModel cm = new ComprehensionModel(sentenceTest, LSA.loadLSA("resources/config/EN/LSA/TASA", Lang.en), this.minActivationThreshold, this.maxSemanticExpand);
        
        for (int sentenceIndex = 0; sentenceIndex < cm.getTotalNoOfPhrases(); sentenceIndex ++) {
            Sentence sentence = cm.getSentenceAtIndex(sentenceIndex);
            
            WordDistanceIndexer syntacticIndexer = cm.getSyntacticIndexerAtIndex(sentenceIndex);
            CMGraphDO currentSyntacticGraph = syntacticIndexer.getCMGraph(CMNodeType.TextBased);
            CMGraphDO currentGraph = cm.getCurrentGraph();
            currentGraph.combineWithSyntacticLinksFrom(currentSyntacticGraph, cm.getSemanticModel(), cm.getMaxDictionaryExpansion());
            cm.setCurrentGraph(currentGraph);
            cm.applyPageRank(sentenceIndex);
            
            CMSentence cmSentence = new CMSentence();
            cmSentence.setText(sentence.getText());
            cmSentence.setIndex(sentenceIndex);
            
            TwoModeGraph graph = new TwoModeGraph();
            
            currentGraph.getNodeList().stream().forEach((currentNode) -> {
                String text = currentNode.getWord().getLemma();
                TwoModeGraphNode node = new TwoModeGraphNode(TwoModeGraphNodeType.Inferred, text, text);
                if(currentNode.getNodeType() == CMNodeType.TextBased) {
                    node = new TwoModeGraphNode(TwoModeGraphNodeType.TextBased, text, text);
                }
                node.setActive(currentNode.isActive());
                graph.nodeList.add(node);
            });
            
            currentGraph.getEdgeList().stream().map((edge) -> {
                TwoModeGraphEdge outEdge = new TwoModeGraphEdge(AuthorDistanceStrategyType.SemanticDistance, edge.getScore(), 
                        edge.getNode1().getWord().getLemma(), edge.getNode2().getWord().getLemma());
                if(edge.getEdgeType() == CMEdgeType.Syntactic) {
                    outEdge = new TwoModeGraphEdge(AuthorDistanceStrategyType.SyntacticDistance, edge.getScore(),
                            edge.getNode1().getWord().getLemma(), edge.getNode2().getWord().getLemma());
                }
                return outEdge;
            }).forEachOrdered((outEdge) -> {
                graph.edgeList.add(outEdge);
            });
            
            cm.logSavedScores(syntacticIndexer.getCMGraph(CMNodeType.TextBased), sentenceIndex, false);
            
            cmSentence.setGraph(graph);
            result.sentenceList.add(cmSentence);
        }
        
        ActivationScoreLogger scoreLogger = cm.getActivationScoreLogger();
        List<Map<Word, WordActivation>> activationHistory = scoreLogger.getActivationHistory();
        List<CMNodeDO> uniqueWordList = scoreLogger.getUniqueWordList();
        for (CMNodeDO node : uniqueWordList) {
            if (node.getNodeType() == CMNodeType.TextBased) {
                result.wordList.add(this.getCMWordResult(node, activationHistory));
            }
        }

        for (CMNodeDO node : uniqueWordList) {
            if (node.getNodeType() != CMNodeType.TextBased) {
                result.wordList.add(this.getCMWordResult(node, activationHistory));
            }
        }
        
        return result;
    }
    
    private CMWordResult getCMWordResult(CMNodeDO node, List<Map<Word, WordActivation>> activationHistory) {
        CMWordResult result = new CMWordResult();
        
        result.value = node.getWord().getLemma();
        result.type = node.getNodeType();
        
        for (Map<Word, WordActivation> activationMap : activationHistory) {
            if (!activationMap.containsKey(node.getWord())) {
                CMWordActivationResult actResult = new CMWordActivationResult();
                actResult.setIsActive(false);
                actResult.setScore(0.0);
                result.activationList.add(actResult);
            } else {
                WordActivation wordActivation = activationMap.get(node.getWord());
                
                CMWordActivationResult actResult = new CMWordActivationResult();
                actResult.setIsActive(wordActivation.isActive());
                actResult.setScore(wordActivation.getActivationValue());
                result.activationList.add(actResult);
            }
        }
        
        return result;
    }
}
