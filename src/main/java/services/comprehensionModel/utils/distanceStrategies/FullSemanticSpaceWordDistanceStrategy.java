package services.comprehensionModel.utils.distanceStrategies;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import data.Word;
import org.apache.log4j.Logger;
import services.comprehensionModel.utils.CMUtils;
import services.comprehensionModel.utils.indexer.graphStruct.CMEdgeType;
import services.semanticModels.ISemanticModel;
import services.semanticModels.SpaceStatistics;
import services.semanticModels.utils.WordSimilarity;
import services.semanticModels.utils.WordSimilarityContainer;

public class FullSemanticSpaceWordDistanceStrategy implements IWordDistanceStrategy, java.io.Serializable {

    private static final long serialVersionUID = -5922757870061109713L;
    static Logger logger = Logger.getLogger(FullSemanticSpaceWordDistanceStrategy.class);

    private WordSimilarityContainer wordDistanceContainer;
    private List<Word> uniqueWordList;
    private final ISemanticModel semanticModel;
    private final int noTopSimilarWords;
    private final double threshold;
    private final CMUtils cmUtils;

    public FullSemanticSpaceWordDistanceStrategy(ISemanticModel semanticModel, double threshold, int noTopSimilarWords) {
        this.semanticModel = semanticModel;
        this.cmUtils = new CMUtils();
        this.threshold = threshold;
        this.noTopSimilarWords = noTopSimilarWords;
        this.indexDistances();
        this.indexUniqueWordList();
        logger.info("Finished indexing the semantic space ...");
    }

    private void indexDistances() {
        SpaceStatistics spaceStatistics = new SpaceStatistics(semanticModel);
        this.wordDistanceContainer = spaceStatistics.getWordSimilarityContainer();
    }

    private void indexUniqueWordList() {
        Iterator<String> wordLemmaIterator = this.wordDistanceContainer.getWordSimilarityMap().keySet().iterator();
        this.uniqueWordList = new ArrayList<>();
        while (wordLemmaIterator.hasNext()) {
            this.uniqueWordList.add(this.cmUtils.convertStringToWord(wordLemmaIterator.next(), this.semanticModel.getLanguage()));
        }
    }

    @Override
    public double getDistance(Word w1, Word w2) {
        double sim = semanticModel.getSimilarity(w1, w2);
        return ((sim >= threshold) && (getIndex(w1.getLemma(), w2.getLemma()) != -1 || getIndex(w2.getLemma(), w1.getLemma()) != -1)) ? sim : 0;
    }

    private int getIndex(String referenceLemma, String lemma) {
        PriorityQueue<WordSimilarity> similarityQueue = this.wordDistanceContainer.getWordSimilarityMap().get(referenceLemma);
        if (similarityQueue == null) {
            return -1;
        }
        Iterator<WordSimilarity> similarityIterator = similarityQueue.iterator();
        for (int currentStep = 0; currentStep < this.noTopSimilarWords && similarityIterator.hasNext(); currentStep++) {
            WordSimilarity sim = similarityIterator.next();
            if (sim.getWordLemma().equalsIgnoreCase(lemma)) {
                return currentStep;
            }
        }
        return -1;
    }

    @Override
    public CMEdgeType getCiEdgeType() {
        return CMEdgeType.Semantic;
    }

    public List<Word> getWordList() {
        return this.uniqueWordList;
    }
}
