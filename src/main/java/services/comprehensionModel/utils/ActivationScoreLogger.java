package services.comprehensionModel.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import data.Word;
import services.comprehensionModel.utils.indexer.graphStruct.CiNodeDO;

public class ActivationScoreLogger {
	public static String OUTPUT_FILE_NAME = "out/comprehension_model_scores.csv";
	private List<Map<Word, Double>> activationHistory;
	private Set<Word> uniqueWordSet;
	
	public ActivationScoreLogger() {
		this.activationHistory = new ArrayList<Map<Word, Double>>();
		this.uniqueWordSet = new HashSet<Word>();
	}
	
	public void saveScores(Map<CiNodeDO, Double> activationMap) {
		Map<Word, Double> activationMapCopy = new HashMap<Word, Double>();
		Iterator<CiNodeDO> nodeIterator = activationMap.keySet().iterator();
		while(nodeIterator.hasNext()) {
			CiNodeDO currentNode = nodeIterator.next();
			
			activationMapCopy.put(currentNode.word, activationMap.get(currentNode));
			this.uniqueWordSet.add(currentNode.word);
		}
		this.activationHistory.add(activationMapCopy);
	}
	
	public void logSavedScores() {
		try {
			FileWriter fwrt = new FileWriter(ActivationScoreLogger.OUTPUT_FILE_NAME);
			BufferedWriter bfwrt = new BufferedWriter(fwrt);
			
			String header = "Word";
			for(int  i = 0; i < activationHistory.size(); i++) {
				header += ",Phrase " + (i + 1);
			}
			bfwrt.write(header);
			
			Iterator<Word> wordIterator = this.uniqueWordSet.iterator();
			
			while(wordIterator.hasNext()) {
				Word word = wordIterator.next();
				String line = word.getLemma();
				
				for(Map<Word, Double> activationMap : this.activationHistory) {
					if(!activationMap.containsKey(word)) {
						line += ",0"; 
					}
					else {
						line += "," + activationMap.get(word);
					}
				}
				
				bfwrt.newLine();
				bfwrt.write(line);
			}
			
			bfwrt.close();
			fwrt.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}