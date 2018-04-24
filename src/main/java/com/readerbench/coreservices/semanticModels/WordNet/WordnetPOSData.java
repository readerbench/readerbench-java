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
package com.readerbench.coreservices.semanticModels.WordNet;

import com.readerbench.datasourceprovider.data.POS;
import com.readerbench.datasourceprovider.data.Word;
import com.readerbench.datasourceprovider.data.semanticmodels.SimilarityType;
import vu.wntools.wnsimilarity.WordnetSimilarityApi;
import vu.wntools.wnsimilarity.measures.SimilarityPair;
import vu.wntools.wordnet.WordnetData;
import vu.wntools.wordnet.WordnetLmfSaxParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
            general = initWordNet(fileName, null);
        }
        return general;
    }

    public WordnetData getByPOS(POS pos) {
        if (!dictionaries.containsKey(pos)) {
            dictionaries.put(pos, initWordNet(fileName, pos));
        }
        return dictionaries.get(pos);
    }

    public static WordnetData initWordNet(String fileName, POS pos) {
        WordnetLmfSaxParser parser = new WordnetLmfSaxParser();
        if (pos != null) {
            parser.setPos(pos.name());
        }
        parser.parseFile(fileName);
        for (Map.Entry<String, ArrayList<String>> e : parser.wordnetData.entryToSynsets.entrySet()) {
            for (String synset : e.getValue()) {
                if (!parser.wordnetData.synsetToEntries.containsKey(synset)) {
                    parser.wordnetData.synsetToEntries.put(synset, new ArrayList<>());
                }
                parser.wordnetData.synsetToEntries.get(synset).add(e.getKey());
            }
        }
        return parser.wordnetData;
    }

    public double semanticSimilarity(Word w1, Word w2, SimilarityType type) {
        String word1 = w1.getLemma();
        String word2 = w2.getLemma();
        return semanticSimilarity(word1, word2, OntologySupport.getPOS(w1.getPOS()), type);
    }

    public double semanticSimilarity(String word1, String word2, POS pos, SimilarityType type) {
        WordnetData wordnetData = getByPOS(pos);
        if (!wordnetData.entryToSynsets.containsKey(word1)
                || !wordnetData.entryToSynsets.containsKey(word2)) {
            wordnetData = getDictionary();
        }
        if (!wordnetData.entryToSynsets.containsKey(word1)
                || !wordnetData.entryToSynsets.containsKey(word2)) {
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
        return wnd.entryToSynsets.getOrDefault(lemma, new ArrayList<>()).stream()
                .flatMap(synset -> wnd.getSynonyms(synset).stream())
                .filter(syn -> !syn.equals(lemma))
                .collect(Collectors.toSet());
    }

}
