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
package services.discourse.keywordMining;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import data.AbstractDocument;
import data.AnalysisElement;
import data.Word;
import data.discourse.SemanticCohesion;
import data.discourse.Keyword;
import services.commons.VectorAlgebra;
import services.complexity.wordComplexity.WordComplexity;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.WordNet.OntologySupport;

public class KeywordModeling {

    static Logger logger = Logger.getLogger(KeywordModeling.class);

    public static final double LSA_WEIGHT = 1.0;
    public static final double LDA_WEIGHT = 1.0;
    public static final double WN_WEIGHT = 1.0;

    public static List<Keyword> filterTopics(AnalysisElement e, Set<String> ignoredWords) {
        logger.info("Filtering toppics");
        List<Keyword> filteredTopics = new ArrayList<>();
        for (Keyword t : e.getTopics()) {
            if (!ignoredWords.contains(t.getWord().getText())) {
                filteredTopics.add(t);
            }
        }
        return filteredTopics;
    }

    public static void determineTopics(AnalysisElement e) {
        logger.info("Determining keywords using Tf-IDf, LSA and LDA ...");
        // determine topics by using Tf-IDF and (LSA & LDA)
        for (Word w : e.getWordOccurences().keySet()) {
            Keyword newTopic = new Keyword(w, e);
            int index = e.getTopics().indexOf(newTopic);
            if (index >= 0) {
                // update frequency for identical lemmas
                Keyword refTopic = e.getTopics().get(index);
                refTopic.updateRelevance(e);
            } else {
                e.getTopics().add(newTopic);
            }
        }
        Collections.sort(e.getTopics());
    }

    public static List<Keyword> getSublist(List<Keyword> topics, int noTopics, boolean nounsOnly, boolean verbsOnly) {
        List<Keyword> results = new ArrayList<>();
        for (Keyword t : topics) {
            if (results.size() >= noTopics || t.getRelevance() < 0) {
                break;
            }
            if (nounsOnly && t.getWord().getPOS() != null && t.getWord().getPOS().startsWith("NN")) {
                results.add(t);
            }
            if (verbsOnly && t.getWord().getPOS() != null && t.getWord().getPOS().startsWith("VB")) {
                results.add(t);
            }
            if ((!nounsOnly && !verbsOnly) || t.getWord().getPOS() == null) {
                results.add(t);
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

    public static Map<Word, Double> getCollectionTopics(List<? extends AbstractDocument> loadedDocuments) {
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
        Map<Word, Double> newTopicScoreMap = new HashMap<>();

        for (Keyword t : topicL) {
            newTopicScoreMap.put(t.getWord(), t.getRelevance());
        }

        return newTopicScoreMap;
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
        logger.info("Determining inferred concepts ...");
        List<Keyword> inferredConcepts = new ArrayList<>();
        double[] topicsLSAVector = null;
        String topicString = "";
        double[] topicsLDAProbDistribution = null;

        // determine corresponding LSA vector for all selected topics
        if (e.getLSA() != null) {
            topicsLSAVector = new double[LSA.K];
            for (Keyword t : topics) {
                if (t.getRelevance() > 0) {
                    for (int i = 0; i < LSA.K; i++) {
                        topicsLSAVector[i] += t.getWord().getLSAVector()[i] * t.getRelevance();
                    }
                    topicString += t.getWord().getLemma() + " ";
                }
            }
        }
        topicString = topicString.trim();
        if (e.getLDA() != null) {
            topicsLDAProbDistribution = e.getLDA().getProbDistribution(topicString);
        }

        TreeMap<Word, Double> inferredConceptsCandidates = new TreeMap<>();

        // create possible matches by exploring 3 alternatives
        // 1 LSA
        logger.info("Determining similar concepts using LSA ...");
        if (e.getLSA() != null) {
            TreeMap<Word, Double> listLSA;

            for (Keyword t : topics) {
                listLSA = e.getLSA().getSimilarConcepts(t.getWord(), minThreshold);
                mergeMaps(inferredConceptsCandidates, listLSA, LSA_WEIGHT);
            }
        }

        // 2 LDA
        logger.info("Determining similar concepts using LDA ...");
        if (e.getLDA() != null) {
            TreeMap<Word, Double> listLDA;
            for (Keyword t : topics) {
                listLDA = e.getLDA().getSimilarConcepts(t.getWord(), minThreshold);
                mergeMaps(inferredConceptsCandidates, listLDA, LDA_WEIGHT);
            }
        }

        // 3 WN
        logger.info("Determining similar concepts using WN ...");
        TreeMap<Word, Double> listWN;
        for (Keyword t : topics) {
            listWN = OntologySupport.getSimilarConcepts(t.getWord());
            mergeMaps(inferredConceptsCandidates, listWN, WN_WEIGHT);
        }

        // rearrange previously identified concepts
        logger.info("Building final list of inferred concepts ...");
        for (Word w : inferredConceptsCandidates.keySet()) {
            if (!containsLemma(w, e.getWordOccurences().keySet())) {
                // possible candidate as inferred concept
                double lsaSim = 0;
                double ldaSim = 0;

                // sim to each topic
                double sumRelevance = 0;
                for (Keyword t : topics) {
                    if (t.getRelevance() > 0) {
                        if (e.getLSA() != null) {
                            lsaSim += VectorAlgebra.cosineSimilarity(e.getLSA().getWordVector(w),
                                    t.getWord().getLSAVector()) * t.getRelevance();
                        }
                        sumRelevance += t.getRelevance();
                        if (e.getLDA() != null) {
                            ldaSim = LDA.getSimilarity(w.getLDAProbDistribution(),
                                    t.getWord().getLDAProbDistribution());
                        }
                    }
                }
                if (sumRelevance != 0) {
                    lsaSim /= sumRelevance;
                    ldaSim /= sumRelevance;
                }

                // sim to topic vector
                if (e.getLSA() != null) {
                    lsaSim += VectorAlgebra.cosineSimilarity(e.getLSA().getWordVector(w), topicsLSAVector);
                }
                if (e.getLDA() != null) {
                    ldaSim += LDA.getSimilarity(w.getLDAProbDistribution(), topicsLDAProbDistribution);
                }

                // sim to analysis element
                if (e.getLSA() != null) {
                    lsaSim += VectorAlgebra.cosineSimilarity(e.getLSA().getWordVector(w), e.getLSAVector());
                }
                if (e.getLDA() != null) {
                    ldaSim += LDA.getSimilarity(w.getLDAProbDistribution(), e.getLDAProbDistribution());
                }

                // penalty for specificity
                double height = WordComplexity.getMaxDistanceToHypernymTreeRoot(w, e.getLanguage());
                if (height == -1) {
                    height = 10;
                }

                double relevance = inferredConceptsCandidates.get(w)
                        * (SemanticCohesion.getAggregatedSemanticMeasure(lsaSim, ldaSim)) / (1 + height);

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
        logger.info("Finished building list of inferred concepts");
    }
}
