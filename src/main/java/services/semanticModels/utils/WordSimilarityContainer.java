package services.semanticModels.utils;

import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

public class WordSimilarityContainer implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    private final Map<String, PriorityQueue<WordSimilarity>> wordSimilarityMap;

    public WordSimilarityContainer() {
        this.wordSimilarityMap = new TreeMap<>();
    }

    public Map<String, PriorityQueue<WordSimilarity>> getWordSimilarityMap() {
        return wordSimilarityMap;
    }

    public void indexDistance(String word1Lemma, String word2Lemma, double similarity) {
        this.indexDistanceByRef(word1Lemma, word2Lemma, similarity);
        this.indexDistanceByRef(word2Lemma, word1Lemma, similarity);
    }

    private void indexDistanceByRef(String referenceWordLemma, String otherWordLemma, double similarity) {
        if (this.wordSimilarityMap.containsKey(referenceWordLemma)) {
            PriorityQueue<WordSimilarity> wordSimilarityQueue = this.wordSimilarityMap.get(referenceWordLemma);
            wordSimilarityQueue.add(new WordSimilarity(otherWordLemma, similarity));
            this.wordSimilarityMap.put(referenceWordLemma, wordSimilarityQueue);
            return;
        }
        PriorityQueue<WordSimilarity> wordSimilarityQueue = new PriorityQueue<>(100);
        wordSimilarityQueue.add(new WordSimilarity(otherWordLemma, similarity));
        this.wordSimilarityMap.put(referenceWordLemma, wordSimilarityQueue);
    }
}
