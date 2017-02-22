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
package webService.services;

import data.AbstractDocument;
import data.Lang;
import data.Word;
import data.discourse.Keyword;
import data.discourse.SemanticCohesion;
import data.sentiment.SentimentGrid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import services.complexity.wordComplexity.WordComplexity;
import services.discourse.keywordMining.KeywordModeling;
import services.semanticModels.LSA.LSA;
import services.semanticModels.LDA.LDA;
import services.semanticModels.word2vec.Word2VecModel;
import services.semanticModels.SimilarityType;
import webService.result.ResultEdge;
import webService.result.ResultNode;
import webService.result.ResultNodeAdvanced;
import webService.result.ResultTopic;
import webService.result.ResultTopicAdvanced;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class ConceptMap {

    /**
     * Get document topics
     *
     * @param queryDoc
     * @param threshold
     * @param ignoredWords
     * @param noTopics
     * @return List of keywords and corresponding relevance scores for results
     */
    public static ResultTopic getTopics(AbstractDocument queryDoc, double threshold, Set<Word> ignoredWords, int noTopics) {
        List<AbstractDocument> queryDocs = new ArrayList();
        queryDocs.add(queryDoc);
        return getTopics(queryDocs, threshold, ignoredWords, noTopics);
    }

    /**
     *
     * @param queryDocs
     * @param threshold
     * @param ignoredWords
     * @param noTopics
     * @return
     */
    public static ResultTopic getTopics(List<? extends AbstractDocument> queryDocs, double threshold, Set<Word> ignoredWords, int noTopics) {

        List<ResultNode> nodes = new ArrayList<>();
        List<ResultEdge> links = new ArrayList<>();

        List<Keyword> topics = new ArrayList();
        Map<Word, Double> topicScores = KeywordModeling.getCollectionTopics(queryDocs);
        for (Map.Entry<Word, Double> entry : topicScores.entrySet()) {
            if (ignoredWords != null) {
                if (!ignoredWords.contains(entry.getKey())) {
                    topics.add(new Keyword(entry.getKey(), entry.getValue()));
                }
            } else {
                topics.add(new Keyword(entry.getKey(), entry.getValue()));
            }
        }
        Collections.sort(topics);

        topics = KeywordModeling.getSublist(topics, noTopics, true, true);

        // build nodes
        SentimentGrid<Double> edges = new SentimentGrid<>(topics.size(), topics.size());
        Map<Word, Integer> nodeIndexes = new TreeMap<>();

        // determine optimal sizes
        double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
        for (Keyword t : topics) {
            if (t.getRelevance() >= 0) {
                min = Math.min(min, Math.log(1 + t.getRelevance()));
                max = Math.max(max, Math.log(1 + t.getRelevance()));
            }
        }

        int i = 0, j = 0;
        for (Keyword t : topics) {
            nodeIndexes.put(t.getWord(), i);
            nodes.add(new ResultNode(i++, t.getWord().getLemma(), t.getRelevance(), 1));
        }

        // determine similarities
        i = 0;
        for (Keyword t1 : topics) {
            edges.setIndex(t1.getWord().getLemma(), i++);
            for (Keyword t2 : topics) {
                edges.setIndex(t2.getWord().getLemma(), j++);
                if (!t1.equals(t2)) {
                    double sim = SemanticCohesion.getAverageSemanticModelSimilarity(t1.getWord(), t2.getWord());
                    if (sim >= threshold) {
                        double distance;
                        if (sim > .9) {
                            distance = 1;
                        } else {
                            distance = (1f - sim) * 10;
                        }
                        links.add(new ResultEdge("", nodeIndexes.get(t1.getWord()), nodeIndexes.get(t2.getWord()), distance));
                    }
                }
            }
        }

        return new ResultTopic(nodes, links);
    }

    public static ResultTopicAdvanced getTopicsAdvanced(AbstractDocument queryDoc, double threshold, Set<Word> ignoredWords) {
        List<AbstractDocument> queryDocs = new ArrayList();
        queryDocs.add(queryDoc);
        return getTopicsAdvanced(queryDocs, threshold, ignoredWords);
    }

    public static ResultTopicAdvanced getTopicsAdvanced(List<? extends AbstractDocument> queryDocs, double threshold, Set<Word> ignoredWords) {

        List<ResultNodeAdvanced> nodes = new ArrayList<>();
        List<ResultEdge> links = new ArrayList<>();

        List<Keyword> topics = new ArrayList();
        Map<Word, Double> topicScores = KeywordModeling.getCollectionTopics(queryDocs);
        for (Map.Entry<Word, Double> entry : topicScores.entrySet()) {
            if (ignoredWords != null) {
                if (!ignoredWords.contains(entry.getKey())) {
                    topics.add(new Keyword(entry.getKey(), entry.getValue()));
                }
            } else {
                topics.add(new Keyword(entry.getKey(), entry.getValue()));
            }
        }
        Collections.sort(topics);

        // build nodes
        Map<Word, Integer> nodeIndexes = new TreeMap<>();

        int i = 0, j = 0;
        Lang lang = queryDocs.get(0).getLanguage();
        LSA lsa = (LSA) queryDocs.get(0).getSemanticModel(SimilarityType.LSA);
        LDA lda = (LDA) queryDocs.get(0).getSemanticModel(SimilarityType.LDA);
        Word2VecModel word2Vec = (Word2VecModel) queryDocs.get(0).getSemanticModel(SimilarityType.WORD2VEC);
        Map<Word, Double> mapIdf = lsa.getMapIdf();
        Map<Word, Integer> wordOcc = queryDocs.get(0).getWordOccurences();
        for (Keyword t : topics) {
            ResultNodeAdvanced node = new ResultNodeAdvanced(i, t.getWord().getText(), t.getRelevance(), 1);
            nodeIndexes.put(t.getWord(), i);
            node.setLemma(t.getWord().getLemma());
            node.setPos(t.getWord().getPOS());
            t.updateRelevance(queryDocs.get(0), t.getWord());
            node.setNoOcc(wordOcc.get(t.getWord()));
            node.setTf(t.getTermFrequency());
            if (mapIdf.containsKey(t.getWord())) {
                node.setIdf(mapIdf.get(t.getWord()));
            } else {
                node.setIdf(-1);
            }

            // similarity scores between word and document using each semantic model
            node.addSemanticSimilarity(SimilarityType.LSA.getAcronym(), lsa.getSimilarity(t.getWord(), queryDocs.get(0)));
            node.addSemanticSimilarity(SimilarityType.LDA.getAcronym(), lda.getSimilarity(t.getWord(), queryDocs.get(0)));
            if (word2Vec != null) {
                node.addSemanticSimilarity(SimilarityType.WORD2VEC.getAcronym(), word2Vec.getSimilarity(t.getWord(), queryDocs.get(0)));
            }

            node.setAverageDistanceToHypernymTreeRoot(WordComplexity.getAverageDistanceToHypernymTreeRoot(t.getWord(), lang));
            node.setMaxDistanceToHypernymTreeRoot(WordComplexity.getMaxDistanceToHypernymTreeRoot(t.getWord(), lang));
            node.setPolysemyCount(WordComplexity.getPolysemyCount(t.getWord()));
            nodes.add(node);
            i++;
        }
        
        // determine similarities
        i = 0;
        for (Keyword t1 : topics) {
            for (Keyword t2 : topics) {
                if (!t1.equals(t2)) {
                    double sim = SemanticCohesion.getAverageSemanticModelSimilarity(t1.getWord(), t2.getWord());
                    System.out.println (sim);
                    if (sim >= threshold) {
                        links.add(new ResultEdge("", nodeIndexes.get(t1.getWord()), nodeIndexes.get(t2.getWord()), sim));
                    }
                }
            }
        }
        Collections.sort(links);

        // determine number of links and degree of nodes
        Map<Integer, Integer> noLinks = new HashMap<>();
        Map<Integer, Double> degree = new HashMap<>();
        for (Integer index : nodeIndexes.values()) {
            noLinks.put(index, 0);
            degree.put(index, 0d);
        }
        for (ResultEdge link : links) {
            noLinks.put(link.getSource(), noLinks.get(link.getSource()) + 1);
            degree.put(link.getSource(), degree.get(link.getSource()) + link.getScore());
        }
        for (ResultNodeAdvanced node : nodes) {
            node.setNoLinks(noLinks.get(node.getId()));
            node.setDegree(degree.get(node.getId()));
        }

        return new ResultTopicAdvanced(nodes, links);
    }
}
