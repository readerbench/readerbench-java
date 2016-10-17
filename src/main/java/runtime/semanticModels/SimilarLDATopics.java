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
package runtime.semanticModels;

import cc.mallet.types.IDSorter;
import data.Lang;
import data.Word;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import services.commons.Formatting;
import services.semanticModels.LDA.LDA;

/**
 *
 * @author Mihai Dascalu
 */
public class SimilarLDATopics {

    static final Logger LOGGER = Logger.getLogger(SimilarLDATopics.class);

    private final LDA lda;
    private final List<Word> simWords;

    public SimilarLDATopics(String pathToLDA, Lang lang, String words) {
        this.lda = LDA.loadLDA(pathToLDA, lang);
        List<String> listOfWords = Arrays.asList(words.split(", "));
        simWords = new ArrayList<>();
        listOfWords.stream().forEach((w) -> {
            simWords.add(Word.getWordFromConcept(w, lang));
        });
    }

    public double simTopic(int topic, int noWordsPerTopic) {
        double result = 0;
        double sumWeight = 0;
        // Get an array of sorted sets of word ID/count pairs
        ArrayList<TreeSet<IDSorter>> topicSortedWords = lda.getModel().getSortedWords();
        Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();

        int rank = 0;
        while (iterator.hasNext() && rank < noWordsPerTopic) {
            IDSorter idCountPair = iterator.next();
            double sim = 0;
            Word w = Word.getWordFromConcept(lda.getModel().getAlphabet().lookupObject(idCountPair.getID()).toString(), lda.getLanguage());
            for (Word simW : simWords) {
                sim += lda.getSimilarity(w, simW);
            }
            result += sim / simWords.size() * idCountPair.getWeight();
            sumWeight += idCountPair.getWeight();
            rank++;
        }
        return result / sumWeight;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    public void reorder(int top, int noWordsPerTopic) {
        Map<Integer, Double> sims = new HashMap<>();

        for (int i = 0; i < lda.getNoDimensions(); i++) {
            double sim = simTopic(i, noWordsPerTopic);
            LOGGER.info("Processed " + i + " >> " + sim);
            sims.put(i, sim);
        }

        sims = sortByValue(sims);

        int rank = 0;

        for (Map.Entry<Integer, Double> entry : sims.entrySet()) {
            System.out.println(entry.getKey() + " >> " + Formatting.formatNumber(entry.getValue()));
            System.out.println(lda.printTopic(entry.getKey(), noWordsPerTopic) + "\n");
            rank++;
            if (rank == top) {
                break;
            }
        }
    }

    public static void main(String[] args) {
//        String concepts = "cooking, sport, physical education, art, culture";
        String concepts = "leisure recreational activity, handicraft model making, board, card, role playing game, home maintenance improvement, nature appreciation, pet ownership care, sport exercise, travel exploration, art, collecting, cooking domestic skill, computer game, dancing, music, reading, theatre, writing, citizenship activity, community awareness, community involvement, interpersonal social skill, business social skill";
        SimilarLDATopics simTopics = new SimilarLDATopics("resources/config/EN/LDA/COCA_newspaper", Lang.en, concepts);
        simTopics.reorder(10, 100);
    }
}
