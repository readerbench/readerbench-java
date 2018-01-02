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
package com.readerbench.services.discourse.keywordMining;

import com.readerbench.data.AbstractDocument;
import com.readerbench.data.AnalysisElement;
import com.readerbench.data.NGram;
import com.readerbench.data.Word;
import com.readerbench.data.discourse.Keyword;
import com.readerbench.data.discourse.SemanticCohesion;
import com.readerbench.services.complexity.wordComplexity.WordComplexity;
import com.readerbench.services.semanticModels.ISemanticModel;
import com.readerbench.services.semanticModels.SimilarityType;
import com.readerbench.services.semanticModels.WordNet.OntologySupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class KeywordModeling {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeywordModeling.class);

    public static final double LSA_WEIGHT = 1.0;
    public static final double LDA_WEIGHT = 1.0;
    public static final double W2V_WEIGHT = 1.0;
    public static final double WN_WEIGHT = 1.0;

    public static List<Keyword> filterTopics(AnalysisElement e, Set<String> ignoredWords) {
        LOGGER.info("Filtering toppics ...");
        List<Keyword> filteredTopics = new ArrayList<>();
        for (Keyword t : e.getTopics()) {
            if (!ignoredWords.contains(t.getWord().getLemma())) {
                filteredTopics.add(t);
            }
        }
        return filteredTopics;
    }

    public static void determineKeywords(AnalysisElement e, boolean useBigrams) {
        LOGGER.info("Determining keywords using Tf-IDf, LSA and LDA ...");
        // determine topics by using Tf-IDF and (LSA & LDA)
        for (Word w : e.getWordOccurences().keySet()) {
            Keyword newTopic = new Keyword(w, e);
            int index = e.getTopics().indexOf(newTopic);
            if (index >= 0) {
                // update frequency for identical lemmas
                Keyword refTopic = e.getTopics().get(index);
                refTopic.updateRelevance(e, w);
            } else {
                e.getTopics().add(newTopic);
            }
        }
        if (useBigrams) {
            try {
                Map<NGram, Long> ngrams = e.getBiGrams().stream()
                        .collect(Collectors.groupingBy(
                                Function.identity(),
                                Collectors.counting()));

                for (Map.Entry<NGram, Long> entry : ngrams.entrySet()) {
                    Keyword newTopic = new Keyword(entry.getKey(), e, entry.getValue().intValue());
                    int index = e.getTopics().indexOf(newTopic);
                    if (index >= 0) {
                        // update frequency for identical lemmas
                        Keyword refTopic = e.getTopics().get(index);
                        refTopic.updateRelevance(e, entry.getKey(), entry.getValue().intValue());
                    } else {
                        e.getTopics().add(newTopic);
                    }
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        Collections.sort(e.getTopics());
    }

    public static List<Keyword> getSublist(List<Keyword> topics, int noTopics, boolean nounsOnly, boolean verbsOnly) {
        if (noTopics == 0 && !nounsOnly && !verbsOnly) {
            return topics;
        }
        List<Keyword> results = new ArrayList<>();
        for (Keyword t : topics) {
            if ((noTopics > 0 && results.size() >= noTopics) || t.getRelevance() < 0) {
                break;
            }
            if (t.getElement() instanceof Word) {
                if (nounsOnly && t.getWord().getPOS() != null && t.getWord().getPOS().startsWith("NN")) {
                    results.add(t);
                }
                if (verbsOnly && t.getWord().getPOS() != null && t.getWord().getPOS().startsWith("VB")) {
                    results.add(t);
                }
                if ((!nounsOnly && !verbsOnly) || t.getWord().getPOS() == null) {
                    results.add(t);
                }
            } else if (t.getElement() instanceof NGram) {
                NGram nGram = (NGram) t.getElement();
                boolean keep = true;
                for (Word w : nGram.getWords()) {
                    if (nounsOnly && w.getPOS() != null && !w.getPOS().startsWith("NN")) {
                        keep = false;
                        break;
                    }
                    if (verbsOnly && w.getPOS() != null && !w.getPOS().startsWith("VB")) {
                        keep = false;
                        break;
                    }
                    if ((!nounsOnly && !verbsOnly) || w.getPOS() == null) {
                        keep = true;
                    }
                }
                if (keep) {
                    results.add(t);
                }
            }
        }
        return results;
    }

    private static void mergeMaps(Map<Word, Double> m1, Map<Word, Double> m2, double factor) {
        // merge all occurrences of m2 into m1
        if (m2 == null) {
            return;
        }
        for (Word w2 : m2.keySet()) {
            if (m1.containsKey(w2)) {
                m1.put(w2, m1.get(w2) + m2.get(w2) * factor);
            } else {
                m1.put(w2, m2.get(w2) * factor);
            }
        }
    }

    public static List<Keyword> getCollectionTopics(List<? extends AbstractDocument> loadedDocuments) {
        Map<String, Double> topicScoreMap = new TreeMap<>();
        Map<String, Word> lemmaToWord = new TreeMap<>();

        for (AbstractDocument d : loadedDocuments) {
            List<Keyword> docTopics = d.getTopics();
            Collections.sort(docTopics, (Keyword t1, Keyword t2) -> -Double.compare(t1.getRelevance(), t2.getRelevance()));
            for (int i = 0; i < docTopics.size(); i++) {
                String lemma = docTopics.get(i).getWord().getLemma();
                if (!topicScoreMap.containsKey(lemma)) {
                    topicScoreMap.put(lemma, docTopics.get(i).getRelevance());
                    lemmaToWord.put(lemma, docTopics.get(i).getWord());
                } else {
                    double topicRel = topicScoreMap.get(lemma)
                            + docTopics.get(i).getRelevance();
                    topicScoreMap.put(lemma, topicRel);
                }
            }
        }

        List<Keyword> topicL = new ArrayList<>();
        Iterator<Map.Entry<String, Double>> mapIter = topicScoreMap.entrySet().iterator();
        while (mapIter.hasNext()) {
            Map.Entry<String, Double> entry = mapIter.next();
            topicL.add(new Keyword(lemmaToWord.get(entry.getKey()), entry.getValue()));
        }
        Collections.sort(topicL);
        return topicL;
    }

    private static boolean containsLemma(Word word, Set<Word> words) {
        for (Word w : words) {
            if (w.getLemma().equals(word.getLemma())) {
                return true;
            }
        }
        return false;
    }

    public static void determineInferredConcepts(AnalysisElement e, List<Keyword> topics, double minThreshold) {
        LOGGER.info("Determining inferred concepts ...");
        List<Keyword> inferredConcepts = new ArrayList<>();

        Map<SimilarityType, double[]> modelVectors = new EnumMap<>(SimilarityType.class);

        for (ISemanticModel model : e.getSemanticModels()) {
            double[] vec = new double[model.getNoDimensions()];
            topics.stream().forEach((topic) -> {
                for (int i = 0; i < model.getNoDimensions(); i++) {
                    if (topic.getWord().getModelRepresentation(model.getType()) != null) {
                        vec[i] += topic.getWord().getModelRepresentation(model.getType())[i];
                    }
                }
            });
            modelVectors.put(model.getType(), vec);
        }

        TreeMap<Word, Double> inferredConceptsCandidates = new TreeMap<>();

        // create possible matches by exploring 3 alternatives
        // 1 LSA
        if (e.getSemanticModel(SimilarityType.LSA) != null) {
            LOGGER.info("Determining similar concepts using LSA ...");
            TreeMap<Word, Double> listLSA;
            for (Keyword t : topics) {
                listLSA = e.getSemanticModel(SimilarityType.LSA).getSimilarConcepts(t.getElement(), minThreshold);
                mergeMaps(inferredConceptsCandidates, listLSA, LSA_WEIGHT);
            }
        }

        // 2 LDA
        if (e.getSemanticModel(SimilarityType.LDA) != null) {
            LOGGER.info("Determining similar concepts using LDA ...");
            TreeMap<Word, Double> listLDA;
            for (Keyword t : topics) {
                listLDA = e.getSemanticModel(SimilarityType.LDA).getSimilarConcepts(t.getElement(), minThreshold);
                mergeMaps(inferredConceptsCandidates, listLDA, LDA_WEIGHT);
            }
        }

        // 3 Word2Vec
        if (e.getSemanticModel(SimilarityType.WORD2VEC) != null) {
            LOGGER.info("Determining similar concepts using word2vec ...");
            TreeMap<Word, Double> listW2V;
            for (Keyword t : topics) {
                listW2V = e.getSemanticModel(SimilarityType.WORD2VEC).getSimilarConcepts(t.getElement(), minThreshold);
                mergeMaps(inferredConceptsCandidates, listW2V, W2V_WEIGHT);
            }
        }

        // 4 WN
        LOGGER.info("Determining similar concepts using WN ...");
        TreeMap<Word, Double> listWN;
        for (Keyword t : topics) {
            listWN = OntologySupport.getSimilarConcepts(t.getWord());
            mergeMaps(inferredConceptsCandidates, listWN, WN_WEIGHT);
        }

        // rearrange previously identified concepts
        LOGGER.info("Building final list of inferred concepts ...");
        for (Word w : inferredConceptsCandidates.keySet()) {
            if (!containsLemma(w, e.getWordOccurences().keySet())) {
                w.setSemanticModels(e.getSemanticModels());
                // penalty for specificity
                double height = WordComplexity.getMaxDistanceToHypernymTreeRoot(w, e.getLanguage());
                if (height == -1) {
                    height = 10;
                }

                double relevance = inferredConceptsCandidates.get(w) * (SemanticCohesion.getAverageSemanticModelSimilarity(w, e)) / (1 + height);

                Keyword t = new Keyword(w, relevance);

                if (inferredConcepts.contains(t)) {
                    Keyword updatedTopic = inferredConcepts.get(inferredConcepts.indexOf(t));
                    updatedTopic.setRelevance(updatedTopic.getRelevance() + relevance);
                } else {
                    inferredConcepts.add(t);
                }
            }
        }

        Collections.sort(inferredConcepts);
        e.setInferredConcepts(inferredConcepts);
        LOGGER.info("Finished building list of inferred concepts ...");
    }
}
