package services.comprehensionModel.utils.distanceStrategies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import data.Lang;
import data.Word;
import runtime.semanticModels.SpaceStatistics;
import runtime.semanticModels.utils.WordDistance;
import services.comprehensionModel.utils.CMUtils;
import services.comprehensionModel.utils.indexer.QueryIndexer;
import services.comprehensionModel.utils.indexer.graphStruct.CiEdgeType;
import services.semanticModels.LDA.LDA;

public class FullSemanticSpaceWordDistanceStrategy implements IWordDistanceStrategy, java.io.Serializable {

	private static final long serialVersionUID = -5922757870061109713L;
	private List<WordDistance> wordDistanceList;
	private List<Word> uniqueWordList;
	private Map<String, Double> wordDistanceMap;
	
	private CMUtils cmUtils;
	private Lang lang;
	
	public FullSemanticSpaceWordDistanceStrategy(Lang lang) {
		this.cmUtils = new CMUtils();
		this.lang = lang;
		this.wordDistanceMap = new HashMap<String, Double>();
		
		this.indexDistances();
		this.indexUniqueWordList();
	}
	private void indexDistances() {
		SpaceStatistics spaceStatistics = new SpaceStatistics(LDA.loadLDA("resources/in/HDP/grade" + 2, QueryIndexer.lang));
		spaceStatistics.buildWordDistances();
		this.wordDistanceList =  spaceStatistics.getRelevantSimilarities();
	}
	private void indexUniqueWordList() {
		Set<String> uniqueWordLemmaSet = new HashSet<String>();
		for(WordDistance wordDistance: this.wordDistanceList) {
			uniqueWordLemmaSet.add(wordDistance.getWord1());
			uniqueWordLemmaSet.add(wordDistance.getWord2());
			this.indexWordDistance(wordDistance);
		}
		this.uniqueWordList = new ArrayList<Word>();
		Iterator<String> wordStringInterator = uniqueWordLemmaSet.iterator();
		while(wordStringInterator.hasNext()) {
			this.uniqueWordList.add(this.cmUtils.convertStringToWord(wordStringInterator.next(), this.lang));
		}
	}
	private void indexWordDistance(WordDistance wDistance) {
		this.wordDistanceMap.put(wDistance.getWord1() + "#" + wDistance.getWord2(), wDistance.getSimilarity());
		this.wordDistanceMap.put(wDistance.getWord2() + "#" + wDistance.getWord1(), wDistance.getSimilarity());
	}
	
	public double getDistance(Word w1, Word w2) {
		if(this.wordDistanceMap.containsKey(w1.getLemma() + "#" + w2.getLemma())) {
			return this.wordDistanceMap.get(w1.getLemma() + "#" + w2.getLemma());
		}
		return 0;
	}
	
	public CiEdgeType getCiEdgeType() {
		return CiEdgeType.Semantic;
	}
	
	public List<Word> getWordList() {
		return this.uniqueWordList;
	}
}