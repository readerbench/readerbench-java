package services.comprehensionModel.utils.distanceStrategies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import data.Lang;
import data.Word;
import services.semanticModels.SpaceStatistics;
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
	
	private int hdpGrade;
	private int noTopSimilarWords;
	private CMUtils cmUtils;
	private Lang lang;
	
	public FullSemanticSpaceWordDistanceStrategy(Lang lang, int hdpGrade, int noTopSimilarWords) {
		this.hdpGrade = hdpGrade;
		this.noTopSimilarWords = noTopSimilarWords;
		
		this.cmUtils = new CMUtils();
		this.lang = lang;
		this.wordDistanceMap = new HashMap<String, Double>();
		
		this.indexDistances();
		this.indexUniqueWordList();
		System.out.println("Finished indexing the semantic space");
	}
	private void indexDistances() {
		SpaceStatistics spaceStatistics = new SpaceStatistics(LDA.loadLDA("resources/in/HDP/grade" + this.hdpGrade, QueryIndexer.lang));
		this.wordDistanceList =  spaceStatistics.getRelevantSimilarities();
	}
	private void indexUniqueWordList() {
		List<String> wordList = this.getUniqueWordLemmaFromFullSet();
		
		int[] noSimilarWordArray = new int[wordList.size()];
		Arrays.fill(noSimilarWordArray, 0);
		HashMap<String, Integer> noSimilarWordArrayIndexMax = new HashMap<String, Integer>();
		for(int i = 0; i < wordList.size(); i ++) {
			noSimilarWordArrayIndexMax.put(wordList.get(i), i);
		}
		
		Set<String> filteredUniqueWordLemmaSet = new HashSet<String>();
		for(WordDistance wordDistance : wordDistanceList) {
			int word1Index = noSimilarWordArrayIndexMax.get(wordDistance.getWord1());
			noSimilarWordArray[word1Index]++;
			
			int word2Index = noSimilarWordArrayIndexMax.get(wordDistance.getWord2());
			noSimilarWordArray[word2Index]++;
			
			if(noSimilarWordArray[word1Index] <= this.noTopSimilarWords || noSimilarWordArray[word2Index] <= this.noTopSimilarWords) {
				filteredUniqueWordLemmaSet.add(wordDistance.getWord1());
				filteredUniqueWordLemmaSet.add(wordDistance.getWord2());
				this.indexWordDistance(wordDistance);
			}
		}
		
		this.uniqueWordList = new ArrayList<Word>();
		Iterator<String> wordStringInterator = filteredUniqueWordLemmaSet.iterator();
		while(wordStringInterator.hasNext()) {
			this.uniqueWordList.add(this.cmUtils.convertStringToWord(wordStringInterator.next(), this.lang));
		}
	}
	private List<String> getUniqueWordLemmaFromFullSet() {
		Set<String> uniqueWordLemmaSet = new HashSet<String>();
		for(WordDistance wordDistance: this.wordDistanceList) {
			uniqueWordLemmaSet.add(wordDistance.getWord1());
			uniqueWordLemmaSet.add(wordDistance.getWord2());
		}
		return this.cmUtils.convertStringIteratorToList(uniqueWordLemmaSet.iterator());
	}
	private List<WordDistance> getTopSimilarWordDistancesTo(String currentWord) {
		List<WordDistance> currentWordDistances = new ArrayList<WordDistance>();
		int currentNoTopSimilarWords = 0;
		for(WordDistance wordDistance: this.wordDistanceList) {
			if(wordDistance.getWord1().equalsIgnoreCase(currentWord) || wordDistance.getWord2().equalsIgnoreCase(currentWord)) {
				currentWordDistances.add(wordDistance);
				currentNoTopSimilarWords ++;
				if(currentNoTopSimilarWords >= this.noTopSimilarWords) {
					break;
				}
			}
		}
		return currentWordDistances;
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