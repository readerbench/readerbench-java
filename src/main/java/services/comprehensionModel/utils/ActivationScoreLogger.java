package services.comprehensionModel.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import data.Word;
import services.comprehensionModel.utils.indexer.graphStruct.CiGraphDO;
import services.comprehensionModel.utils.indexer.graphStruct.CiNodeDO;
import services.comprehensionModel.utils.indexer.graphStruct.CiNodeType;

public class ActivationScoreLogger {
	public static String OUTPUT_FILE_NAME = "out/comprehension_model_scores.csv";
	private List<Map<Word, Double>> activationHistory;
	private List<CiNodeDO> uniqueWordList;
	
	public ActivationScoreLogger() {
		this.activationHistory = new ArrayList<Map<Word, Double>>();
		this.uniqueWordList = new ArrayList<CiNodeDO>();
	}
	
	public void saveScores(Map<CiNodeDO, Double> activationMap) {
		Map<Word, Double> activationMapCopy = new HashMap<Word, Double>();
		Iterator<CiNodeDO> nodeIterator = activationMap.keySet().iterator();
		while(nodeIterator.hasNext()) {
			CiNodeDO currentNode = nodeIterator.next();
			
			activationMapCopy.put(currentNode.word, activationMap.get(currentNode));
		}
		this.activationHistory.add(activationMapCopy);
	}
	public void saveNodes(CiGraphDO graph) {
		for(CiNodeDO node : graph.nodeList) {
			this.addNodeIfNotExists(node);
		}
	}
	private void addNodeIfNotExists(CiNodeDO nodeToAdd) {
		boolean exists = false;
		for(int i = 0; i < this.uniqueWordList.size(); i ++) {
			CiNodeDO currentNode = this.uniqueWordList.get(i);
			if(currentNode.equals(nodeToAdd)) {
				if(currentNode.nodeType != CiNodeType.Syntactic && nodeToAdd.nodeType == CiNodeType.Syntactic) {
					this.uniqueWordList.set(i, nodeToAdd);
				}
				exists = true;
				break;
			}
		}
		if(!exists) {
			this.uniqueWordList.add(nodeToAdd);
		}
	}
	
	public void logSavedScores() {
		try {
			FileWriter fwrt = new FileWriter(ActivationScoreLogger.OUTPUT_FILE_NAME);
			BufferedWriter bfwrt = new BufferedWriter(fwrt);
			
			String header = "Word,Type";
			for(int  i = 0; i < activationHistory.size(); i++) {
				header += ",Phrase " + (i + 1);
			}
			bfwrt.write(header);
			
			for(CiNodeDO node : this.uniqueWordList) {
				if(node.nodeType == CiNodeType.Syntactic) {
					String line = this.getLogLineForNode(node, "Syntactic");
					bfwrt.newLine();
					bfwrt.write(line);
				}
			}
			
			for(CiNodeDO node : this.uniqueWordList) {
				if(node.nodeType != CiNodeType.Syntactic) {
					String line = this.getLogLineForNode(node, "Semantic");
					bfwrt.newLine();
					bfwrt.write(line);
				}
			}
			
			bfwrt.close();
			fwrt.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private String getLogLineForNode(CiNodeDO node, String nodeType) {
		String line = node.word.getLemma() + "," + nodeType;
		
		for(Map<Word, Double> activationMap : this.activationHistory) {
			if(!activationMap.containsKey(node.word)) {
				line += ",0"; 
			}
			else {
				line += "," + activationMap.get(node.word);
			}
		}
		return line;
	}
	
}