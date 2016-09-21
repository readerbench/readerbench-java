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
package data.discourse;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import data.Word;
import data.lexicalChains.LexicalChain;
import data.sentiment.SentimentEntity;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import services.commons.ValueComparator;
import services.commons.VectorAlgebra;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;

public class SemanticChain implements Serializable, Comparable<SemanticChain> {

    private static final long serialVersionUID = -7902005522958585451L;
    private static final double LSA_SIMILARITY_THRESHOLD = 1.1;
    private static final double LDA_SIMILARITY_THRESHOLD = 1.1;

    private transient LSA lsa;
    private transient LDA lda;
    private List<Word> words;
    private Map<String, Double> termOccurrences;
    private double[] lsaVector;
    private double[] ldaProbDistribution;
    private double[] sentenceDistribution;
    private double[] blockDistribution;
    private double[] blockMovingAverage;
    private double averageImportanceScore;

    private transient SentimentEntity chainSentiment;

    public SemanticChain(LexicalChain chain, LSA lsa, LDA lda) {
        words = new LinkedList<>();
        this.lsa = lsa;
        this.lda = lda;
        chain.getLinks().stream().forEach((link) -> {
            words.add(link.getWord());
        });
        this.chainSentiment = new SentimentEntity();
    }

    public static double similarity(SemanticChain chain1, SemanticChain chain2) {
        // determines whether 2 chains can be merged
        if (chain1 == null || chain2 == null) {
            return -1;
        }

        double dist = -1;
        // if words have same lemma
        for (Word w1 : chain1.getWords()) {
            for (Word w2 : chain2.getWords()) {
                if (w1.getLemma().equals(w2.getLemma())) {
                    return 1;
                }
            }
        }

        double distLSA = VectorAlgebra.cosineSimilarity(chain1.getLSAVector(), chain2.getLSAVector());
        if (distLSA >= LSA_SIMILARITY_THRESHOLD) {
            dist = Math.max(dist, distLSA);
        }

        double distLDA = LDA.getSimilarity(chain1.getLDAProbDistribution(), chain2.getLDAProbDistribution());
        if (distLDA >= LDA_SIMILARITY_THRESHOLD) {
            dist = Math.max(dist, distLDA);
        }

        return dist;
    }

    public static SemanticChain merge(SemanticChain chain1, SemanticChain chain2) {
        // copy words from chain 2, update vector
        chain2.getWords().stream().forEach((w2) -> {
            chain1.getWords().add(w2);
        });
        chain1.updateSemanticRepresentation();

        return chain1;
    }

    public void updateSemanticRepresentation() {
        if (lsa != null) {
            lsaVector = new double[LSA.K];
            words.stream().forEach((word) -> {
                for (int i = 0; i < LSA.K; i++) {
                    lsaVector[i] += word.getLSAVector()[i];
                }
            });
        }

        // determine LDA distribution
        if (lda != null) {
            String text = "";
            text = words.stream().map((word) -> word.getLemma() + " ").reduce(text, String::concat);
            this.ldaProbDistribution = lda.getProbDistribution(text.trim());
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

    public double[] getLSAVector() {
        return lsaVector;
    }

    public void setLSAVector(double[] lsaVector) {
        this.lsaVector = lsaVector;
    }

    public double[] getLDAProbDistribution() {
        return ldaProbDistribution;
    }

    public void setLDAProbDistribution(double[] ldaProbDistribution) {
        this.ldaProbDistribution = ldaProbDistribution;
    }

    public LSA getLSA() {
        return lsa;
    }

    public void setLSA(LSA lsa) {
        this.lsa = lsa;
    }

    public LDA getLDA() {
        return lda;
    }

    public void setLDA(LDA lda) {
        this.lda = lda;
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
            if (!count.containsKey(lemma)) count.put(lemma, 1);
            else count.put(lemma, count.get(lemma) + 1);
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

    public SentimentEntity getChainSentiment() {
        return chainSentiment;
    }

    public void setChainSentiment(SentimentEntity chainSentiment) {
        this.chainSentiment = chainSentiment;
    }

    public void setChainSentiment(int sentiment) {
        SentimentEntity sre = new SentimentEntity();
        String s = "";
        for (Word w : words) {
            s = s + w.getText() + " ";
        }
        // sre.addSentimentResultEntity(s, sentiment);
        this.chainSentiment = sre;
    }

    public double getStdevSentiment() {
        return VectorAlgebra.stdev(getSentenceDistribution());
    }

    @Override
    public int compareTo(SemanticChain o) {
        return (int) (Math.signum(o.getNoWords() - this.getNoWords()));
    }
}
