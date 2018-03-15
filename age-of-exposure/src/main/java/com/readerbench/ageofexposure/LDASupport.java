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
package com.readerbench.ageofexposure;

import cc.mallet.util.Maths;
import com.readerbench.data.Word;
import com.readerbench.readerbenchcore.semanticModels.LDA.LDA;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LDASupport {

    /**
     * Align two topics in two separate models and compute topic distance. (JSH
     * on word distributions)
     *
     * @param lda1 - first model
     * @param topic1Id - first topic
     * @param lda2 - second model
     * @param topic2Id - second topic
     * @return : distance between topics ([0 - 1])
     */
    public static double topicDistance(LDA lda1, int topic1Id, LDA lda2, int topic2Id) {

        /* Get the bigger Alphabet */
        int biggerSize = Math.max(
                lda1.getWordSet().size(),
                lda2.getWordSet().size());

        /* Create model local word id to reference(bigger) model id mapping */
        Map<Word, Integer> wordIndex = new HashMap<>();
        TreeSet<Word> words = new TreeSet<>(lda1.getWordSet());
        words.retainAll(lda2.getWordSet());
        int id = 0;
        for (Word w : words) {
            wordIndex.put(w, id++);
        }

        /* Fill up distributions ( index = reference id ) */
        double[] distribution1 = new double[words.size()];
        double[] distribution2 = new double[words.size()];

        final double sum1 = lda1.getSortedWords()[topic1Id].stream()
                .filter(p -> wordIndex.containsKey(p.first))
                .mapToDouble(p -> p.second)
                .sum();
        lda1.getSortedWords()[topic1Id].stream()
                .filter(p -> wordIndex.containsKey(p.first))
                .forEach(p -> {
                    distribution1[wordIndex.get(p.first)] = p.second / sum1;
                });
        final double sum2 = lda2.getSortedWords()[topic2Id].stream()
                .filter(p -> wordIndex.containsKey(p.first))
                .mapToDouble(p -> p.second)
                .sum();
        lda2.getSortedWords()[topic2Id].stream()
                .filter(p -> wordIndex.containsKey(p.first))
                .forEach(p -> {
                    distribution1[wordIndex.get(p.first)] = p.second / sum2;
                });

        /* Compute sine=1-cosSimilarity^2 as distribution distance */
        // double cosineSim = VectorAlgebra.cosineSimilarity(distribution1,
        // distribution2);
        // return 1D-cosineSim;
        // return Math.sqrt(1D-cosineSim*cosineSim);

        /* Compute JensenShannon Divergence distribution distance */
        return Maths.jensenShannonDivergence(distribution1, distribution2);
    }

    /**
     * Returns a list of concepts ( concept = list of strings
     * ).O(topic_num*numWords*Aphabet.lookup(word)).
     *
     * @param lda
     * @param numWords - words/concept
     * @return
     */
    public static List<List<String>> getConcepts(LDA lda, int numWords) {
        List<List<String>> result = new ArrayList<>();

        // Iterate topics and extract top words
        for (int topic = 0; topic < lda.getNoDimensions(); topic++) {
            result.add(lda.getSortedWords()[topic].subList(0, numWords).stream()
                    .map(p -> "(" + p.first.getText() + "," + p.second + ")")
                    .collect(Collectors.toList()));
        }

        return result;
    }

    /**
     * Computes a word weight in a topic word distribution. O(num_words)
     *
     * @param lda : the input LDA model
     * @param concept the reference word
     * @param topicId : the reference topic number
     * @return : the normalized word weight
     *
     */
    public static double getWordWeight(LDA lda, Word concept, int topicId) {
        double wordWeight = lda.getWordRepresentation(concept)[topicId];
        double weightSum = lda.getSortedWords()[topicId].stream()
                .mapToDouble(p -> p.second)
                .sum();
        return wordWeight / weightSum;
    }

    public static Map<Word, Double> getWordWeights(LDA lda, Integer topicId) {
        final double weightSum = lda.getSortedWords()[topicId].stream()
                .mapToDouble(p -> p.second)
                .sum();
        if (weightSum == 0) {
            return new TreeMap<>();
        }
        return lda.getSortedWords()[topicId].stream()
                .collect(Collectors.toMap(
                        p -> p.first,
                        p -> p.second / weightSum));
    }

    public static Map<Word, Double> getWordWeights(LDA lda, Double[] weights) {
        double weightSum = 0;
        for (int topicId = 0; topicId < lda.getNoDimensions(); topicId++) {
            final int index = topicId;
            weightSum += lda.getSortedWords()[topicId].stream()
                    .mapToDouble(p -> p.second * weights[index])
                    .sum();
        }
        if (weightSum == 0) {
            return new TreeMap<>();
        }
        final double sum = weightSum;
        return lda.getWordRepresentations().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> IntStream.range(0, lda.getNoDimensions())
                                .mapToDouble(topicId -> e.getValue()[topicId] * weights[topicId])
                                .sum() / sum
                ));

    }

}
