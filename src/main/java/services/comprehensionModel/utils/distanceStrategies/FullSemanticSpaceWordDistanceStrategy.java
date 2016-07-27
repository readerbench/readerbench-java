package services.comprehensionModel.utils.distanceStrategies;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import data.Word;
import services.comprehensionModel.utils.CMUtils;
import services.comprehensionModel.utils.indexer.graphStruct.CMEdgeType;
import services.semanticModels.ISemanticModel;
import services.semanticModels.SpaceStatistics;
import services.semanticModels.utils.WordSimilarity;
import services.semanticModels.utils.WordSimilarityContainer;

public class FullSemanticSpaceWordDistanceStrategy implements IWordDistanceStrategy, java.io.Serializable {
	private static final long serialVersionUID = -5922757870061109713L;
	
	private WordSimilarityContainer wordDistanceContainer;
	private List<Word> uniqueWordList;
	
	private ISemanticModel semanticModel;
	private int noTopSimilarWords;
	private CMUtils cmUtils;
	
	public FullSemanticSpaceWordDistanceStrategy(ISemanticModel semanticModel, int noTopSimilarWords) {
		this.semanticModel = semanticModel;
		this.noTopSimilarWords = noTopSimilarWords;
		
		this.cmUtils = new CMUtils();
		
		this.indexDistances();
		this.indexUniqueWordList();
		System.out.println("Finished indexing the semantic space");
	}
	private void indexDistances() {
		SpaceStatistics spaceStatistics = new SpaceStatistics(semanticModel);
		this.wordDistanceContainer = spaceStatistics.getWordSimilarityContainer();
	}
	private void indexUniqueWordList() {
		Iterator<String> wordLemmaIterator = this.wordDistanceContainer.wordSimilarityMap.keySet().iterator();
		this.uniqueWordList = new ArrayList<Word>();
		while(wordLemmaIterator.hasNext()) {
			this.uniqueWordList.add(this.cmUtils.convertStringToWord(wordLemmaIterator.next(), this.semanticModel.getLanguage()));
		}
	}
	
	public double getDistance(Word w1, Word w2) {
		double similarity = this.getSimilarity(w1.getLemma(), w2.getLemma());
		return similarity > 0 ? similarity : this.getSimilarity(w2.getLemma(), w1.getLemma());
	}
	private double getSimilarity(String referenceLemma, String otherLemma) {
		PriorityQueue<WordSimilarity> similarityQueue = this.wordDistanceContainer.wordSimilarityMap.get(referenceLemma);
		if(similarityQueue == null) {
			return 0.0;
		}
		Iterator<WordSimilarity> similarityIterator = similarityQueue.iterator();
		for(int currentStep = 0; currentStep < this.noTopSimilarWords && similarityIterator.hasNext(); currentStep ++) {
			WordSimilarity sim = similarityIterator.next();
			if(sim.getWordLemma().equalsIgnoreCase(otherLemma)) {
				return sim.getSimilarity();
			}
		}
		return 0.0;
	}
	
	
	public CMEdgeType getCiEdgeType() {
		return CMEdgeType.Semantic;
	}
	
	public List<Word> getWordList() {
		return this.uniqueWordList;
	}
}