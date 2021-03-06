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
package com.readerbench.coreservices.data.discourse;

import com.readerbench.coreservices.data.Word;
import com.readerbench.coreservices.data.lexicalchains.LexicalChain;
import com.readerbench.coreservices.semanticmodels.SimilarityType;
import org.apache.commons.lang3.StringUtils;
import com.readerbench.coreservices.commons.ValueComparator;
import com.readerbench.coreservices.commons.VectorAlgebra;
import com.readerbench.coreservices.semanticmodels.SemanticModel;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class SemanticChain implements Serializable, Comparable<SemanticChain> {

    private static final long serialVersionUID = -7902005522958585451L;

    private transient Map<SimilarityType, SemanticModel> semanticModels;
    private List<Word> words;
    private Map<String, Double> termOccurrences;
    private Map<SimilarityType, double[]> modelVectors;
    private double[] sentenceDistribution;
    private double[] blockDistribution;
    private double[] extendedSentenceDistribution;
    private double[] extendedBlockDistribution;
    private double[] blockMovingAverage;
    private double averageImportanceScore;

    
    public SemanticChain(LexicalChain chain, List<SemanticModel> models) {
        words = new LinkedList<>();
        this.setSemanticModels(models);
        chain.getLinks().stream().forEach((link) -> {
            words.add(link.getWord());
        });
    }

    public static double computeSimilarity(SemanticChain chain1, SemanticChain chain2) {
        // determines whether 2 chains can be merged
        if (chain1 == null || chain2 == null) {
            return -1;
        }

        // if words have same lemma
        for (Word w1 : chain1.getWords()) {
            for (Word w2 : chain2.getWords()) {
                if (w1.getLemma().equals(w2.getLemma())) {
                    return 1;
                }
            }
        }

        EnumMap<SimilarityType, Double> similarities = new EnumMap<>(SimilarityType.class);
        for (SimilarityType st : chain1.getSemanticModels().keySet()) {
            SemanticModel model = chain1.getSemanticModels().get(st);
            similarities.put(model.getSimilarityType(), model.getSimilarity(chain1.getModelVectors().get(st), chain2.getModelVectors().get(st)));
        }

        return SemanticCohesion.getAggregatedSemanticMeasure(similarities);
    }

    public static SemanticChain merge(SemanticChain chain1, SemanticChain chain2) {
        // copy words from chain 2, update vector
        chain2.getWords().stream().forEach((w2) -> {
            chain1.getWords().add(w2);
        });
        chain1.updateSemanticRepresentation();

        return chain1;
    }

    /**
     * @param chain1
     * @param chain2
     * @return true if chain1 is included in chain2
     */
    public static boolean includedIn(SemanticChain chain1, SemanticChain chain2) {
        for (Word w : chain1.getWords()) {
            if (!chain2.getWords().contains(w)) {
                return false;
            }
        }
        return true;
    }

    public void updateSemanticRepresentation() {
        if (modelVectors == null) {
            modelVectors = new EnumMap<>(SimilarityType.class);
        }

        for (Entry<SimilarityType, SemanticModel> e : semanticModels.entrySet()) {
            double[] vec = new double[e.getValue().getNoDimensions()];
            words.stream()
                    .map(word -> word.getModelRepresentation(e.getKey()))
                    .filter(Objects::nonNull)
                    .forEach(v -> {
                        for (int i = 0; i < e.getValue().getNoDimensions(); i++) {
                            vec[i] += v[i];
                        }
                    });
            modelVectors.put(e.getKey(), vec);
        }

        Map<String, Double> unsortedOccurences = new TreeMap<>();
        words.stream().forEach((word) -> {
            if (unsortedOccurences.containsKey(word.getLemma())) {
                unsortedOccurences.put(word.getLemma(), unsortedOccurences.get(word.getLemma()) + 1);
            } else {
                unsortedOccurences.put(word.getLemma(), 1.0);
            }
        });
        ValueComparator<String> kcvc = new ValueComparator<>(unsortedOccurences);
        termOccurrences = new TreeMap<>(kcvc);
        termOccurrences.putAll(unsortedOccurences);
    }

    public List<Word> getWords() {
        return words;
    }

    public void setWords(List<Word> words) {
        this.words = words;
    }

    public int getNoWords() {
        return words.size();
    }

    public int getNoSentences() {
        int no = 0;
        for (double d : sentenceDistribution) {
            if (d > 0) {
                no++;
            }
        }
        return no;
    }

    public int getNoBlocks() {
        int no = 0;
        for (double d : blockDistribution) {
            if (d > 0) {
                no++;
            }
        }
        return no;
    }

    public double getEntropySentence() {
        return VectorAlgebra.entropy(sentenceDistribution);
    }

    public double getEntropyBlock(boolean useMovingAverage) {
        if (useMovingAverage) {
            return VectorAlgebra.entropy(blockMovingAverage);
        }
        return VectorAlgebra.entropy(blockDistribution);
    }

    public double getAvgSentence(boolean useMovingAverage) {
        return VectorAlgebra.avg(sentenceDistribution);
    }

    public double getStdevSentence(boolean useMovingAverage) {
        return VectorAlgebra.stdev(sentenceDistribution);
    }

    public double getAvgBlock() {
        return VectorAlgebra.avg(blockDistribution);
    }

    public double getStdevBlock() {
        return VectorAlgebra.stdev(blockDistribution);
    }

    public double getAvgRecurrenceSentence() {
        return VectorAlgebra.avg(VectorAlgebra.getRecurrence(sentenceDistribution));
    }

    public double getAvgRecurrenceBlock() {
        return VectorAlgebra.avg(VectorAlgebra.getRecurrence(blockDistribution));
    }

    public double getStdevRecurrenceSentence() {
        return VectorAlgebra.stdev(VectorAlgebra.getRecurrence(sentenceDistribution));
    }

    public double getStdevRecurrenceBlock() {
        return VectorAlgebra.stdev(VectorAlgebra.getRecurrence(blockDistribution));
    }

    @Override
    public String toString() {
        String s = "(";
        int noMax = 3, noCrt = 0;
        for (String key : termOccurrences.keySet()) {
            if (noCrt == noMax) {
                break;
            }
            s += key + ",";
            noCrt++;
        }
        if (noCrt > 0) {
            s = s.substring(0, s.length() - 1);
        }
        s += ")";
        return s;
    }

    public String toStringAllWords() {
        Map<String, Integer> count = new HashMap<>();
        for (Word word : words) {
            String lemma = word.getLemma();
            if (!count.containsKey(lemma)) {
                count.put(lemma, 1);
            } else {
                count.put(lemma, count.get(lemma) + 1);
            }
        }
        List<String> entries = count.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue() - e1.getValue())
                .map(e -> e.getKey() + "(" + e.getValue() + ")")
                .collect(Collectors.toList());
        return "(" + StringUtils.join(entries, ", ") + ")";
    }

    public double[] getSentenceDistribution() {
        return sentenceDistribution;
    }

    public void setSentenceDistribution(double[] sentenceDistribution) {
        this.sentenceDistribution = sentenceDistribution;
    }

    public double[] getBlockDistribution() {
        return blockDistribution;
    }

    public void setBlockDistribution(double[] blockDistribution) {
        this.blockDistribution = blockDistribution;
    }

    public double[] getExtendedSentenceDistribution() {
        return extendedSentenceDistribution;
    }

    public void setExtendedSentenceDistribution(double[] extendedSentenceDistribution) {
        this.extendedSentenceDistribution = extendedSentenceDistribution;
    }

    public double[] getExtendedBlockDistribution() {
        return extendedBlockDistribution;
    }

    public void setExtendedBlockDistribution(double[] extendedBlockDistribution) {
        this.extendedBlockDistribution = extendedBlockDistribution;
    }

    public double[] getBlockMovingAverage() {
        return blockMovingAverage;
    }

    public void setBlockMovingAverage(double[] blockMovingAverage) {
        this.blockMovingAverage = blockMovingAverage;
    }

    public double getAverageImportanceScore() {
        return averageImportanceScore;
    }

    public void setAverageImportanceScore(double averageImportanceScore) {
        this.averageImportanceScore = averageImportanceScore;
    }

    public double getStdevSentiment() {
        return VectorAlgebra.stdev(getSentenceDistribution());
    }

    @Override
    public int compareTo(SemanticChain o) {
        return (int) (Math.signum(o.getNoWords() - this.getNoWords()));
    }

    public final void setSemanticModels(List<SemanticModel> models) {
        semanticModels = new EnumMap<>(SimilarityType.class);
        for (SemanticModel model : models) {
            semanticModels.put(model.getSimilarityType(), model);
        }
    }

    public Map<SimilarityType, SemanticModel> getSemanticModels() {
        return semanticModels;
    }

    public Map<SimilarityType, double[]> getModelVectors() {
        return modelVectors;
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

}
