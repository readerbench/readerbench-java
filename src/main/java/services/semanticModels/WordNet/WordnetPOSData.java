/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.semanticModels.WordNet;

import data.Word;
import data.Lang;
import data.POS;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import static services.semanticModels.WordNet.OntologySupport.getPOS;
import vu.wntools.wnsimilarity.WordnetSimilarityApi;
import vu.wntools.wnsimilarity.measures.SimilarityPair;
import vu.wntools.wordnet.WordnetData;
import vu.wntools.wordnet.WordnetLmfSaxParser;

/**
 *
 * @author Stefan
 */
public class WordnetPOSData {

    private final Map<POS, WordnetData> dictionaries = new HashMap<>();
    private WordnetData general = null;
    private String fileName;
    
    public WordnetPOSData(String fileName) {
        this.fileName = fileName;
    }

    public WordnetData getDictionary() {
        if (general == null) {
            WordnetLmfSaxParser parser = new WordnetLmfSaxParser();
            parser.parseFile(fileName);
            general = parser.wordnetData;
        }
        return general;
    }
    
    public WordnetData getByPOS(POS pos) {
        if (!dictionaries.containsKey(pos)) {
            WordnetLmfSaxParser parser = new WordnetLmfSaxParser();
            parser.setPos(pos.name());
            parser.parseFile(fileName);
            dictionaries.put(pos, parser.wordnetData);
        }
        return dictionaries.get(pos);
    }

    public double semanticSimilarity(Word w1, Word w2, SimilarityType type) {
        String word1 = w1.getLemma();
        String word2 = w2.getLemma();
        return semanticSimilarity(word1, word2, OntologySupport.getPOS(w1.getPOS()), type);
    }
    
    public double semanticSimilarity(String word1, String word2, POS pos, SimilarityType type) {
        WordnetData wordnetData = getByPOS(pos);
        if (!wordnetData.entryToSynsets.containsKey(word1) || 
            !wordnetData.entryToSynsets.containsKey(word2)) {
            wordnetData = getDictionary();
        }
        if (!wordnetData.entryToSynsets.containsKey(word1) || 
            !wordnetData.entryToSynsets.containsKey(word2)) {
            return 0;
        }
        ArrayList<SimilarityPair> similarities = new ArrayList<>();
        switch (type) {
            case LEACOCK_CHODOROW:
                similarities = WordnetSimilarityApi.wordLeacockChodorowSimilarity(wordnetData, word1, word2);
                break;
            case WU_PALMER:
                similarities = WordnetSimilarityApi.wordWuPalmerSimilarity(wordnetData, word1, word2);
                break;
            case PATH_SIM:
                similarities = WordnetSimilarityApi.wordPathSimilarity(wordnetData, word1, word2);
                break;
        }
        return WordnetSimilarityApi.getTopScoringSimilarityPair(similarities).getScore();
    }
    
    public Set<String> getSynonyms(String lemma, POS pos) {
        final WordnetData wnd = getByPOS(pos);
        return wnd.entryToSynsets.get(lemma).stream()
                .flatMap(synset -> wnd.getSynonyms(synset).stream())
                .collect(Collectors.toSet());
    }

}
