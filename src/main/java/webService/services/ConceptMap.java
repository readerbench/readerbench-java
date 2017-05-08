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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import services.complexity.wordComplexity.WordComplexity;
import services.discourse.keywordMining.KeywordModeling;
import services.semanticModels.LSA.LSA;
import services.semanticModels.LDA.LDA;
import services.semanticModels.word2vec.Word2VecModel;
import services.semanticModels.SimilarityType;
import webService.result.ResultEdge;
import webService.result.ResultNode;
import webService.result.ResultTopic;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class ConceptMap {

    public static ResultTopic getKeywords(AbstractDocument queryDoc, double threshold, Set<Word> ignoredWords) {

        List<ResultNode> nodes = new ArrayList<>();

        List<Keyword> keywords = queryDoc.getTopics();
        if (ignoredWords != null && !ignoredWords.isEmpty()) {
            keywords = keywords.stream()
                    .filter(k -> !ignoredWords.contains(k.getWord()))
                    .collect(Collectors.toList());
        }

        // build nodes
        Map<Word, Integer> nodeIndexes = new TreeMap<>();

        int i = 0, j = 0;
        Lang lang = queryDoc.getLanguage();
        LSA lsa = (LSA) queryDoc.getSemanticModel(SimilarityType.LSA);
        LDA lda = (LDA) queryDoc.getSemanticModel(SimilarityType.LDA);
        Word2VecModel word2Vec = (Word2VecModel) queryDoc.getSemanticModel(SimilarityType.WORD2VEC);
        Map<Word, Double> mapIdf;
        if (lsa != null) {
            mapIdf = lsa.getMapIdf();
        } else {
            mapIdf = null;
        }
        Map<Word, Integer> wordOcc = queryDoc.getWordOccurences();
        for (Keyword t : keywords) {
            ResultNode node = new ResultNode(i, t.getWord().getText(), t.getRelevance(), 1);
            nodeIndexes.put(t.getWord(), i);
            node.setLemma(t.getWord().getLemma());
            node.setPos(t.getWord().getPOS());
            t.updateRelevance(queryDoc, t.getWord());
            node.setNoOcc(wordOcc.get(t.getWord()));
            node.setTf(t.getTermFrequency());
            if (mapIdf != null && mapIdf.containsKey(t.getWord())) {
                node.setIdf(mapIdf.get(t.getWord()));
            } else {
                node.setIdf(-1);
            }

            // similarity scores between word and document using each semantic model
            if (lsa != null) {
                node.addSemanticSimilarity(SimilarityType.LSA.getAcronym(), lsa.getSimilarity(t.getWord(), queryDoc));
            }
            if (lda != null) {
                node.addSemanticSimilarity(SimilarityType.LDA.getAcronym(), lda.getSimilarity(t.getWord(), queryDoc));
            }
            if (word2Vec != null) {
                node.addSemanticSimilarity(SimilarityType.WORD2VEC.getAcronym(), word2Vec.getSimilarity(t.getWord(), queryDoc));
            }

            node.setAverageDistanceToHypernymTreeRoot(WordComplexity.getAverageDistanceToHypernymTreeRoot(t.getWord(), lang));
            node.setMaxDistanceToHypernymTreeRoot(WordComplexity.getMaxDistanceToHypernymTreeRoot(t.getWord(), lang));
            node.setPolysemyCount(WordComplexity.getPolysemyCount(t.getWord()));
            nodes.add(node);
            i++;
        }

        // determine similarities
        List<ResultEdge> links = buildLinks(keywords, threshold, nodeIndexes);
        appendDegreeValues(nodes, links, nodeIndexes);

        return new ResultTopic(nodes, links);
    }

    public static ResultTopic getKeywords(List<? extends AbstractDocument> documents, double threshold, Integer maxNoWords) {
        List<ResultNode> nodes = new ArrayList<>();

        List<Keyword> keywords = KeywordModeling.getCollectionTopics(documents);
        if(maxNoWords != null) {
            Collections.sort(keywords);
            keywords = keywords.subList(0, Math.min(keywords.size(), maxNoWords));
        }
        Map<Word, Integer> nodeIndexes = new TreeMap<>();

        for (int i = 0; i < keywords.size(); i++) {
            Keyword keyword = keywords.get(i);
            double relevance = Math.round(keyword.getRelevance() * 100.0) / 100.0;
            ResultNode node = new ResultNode(i, keyword.getWord().getLemma(), relevance, 1);
            nodeIndexes.put(keyword.getWord(), i);
            nodes.add(node);
        }
        List<ResultEdge> links = buildLinks(keywords, threshold, nodeIndexes);
        appendDegreeValues(nodes, links, nodeIndexes);
      
        return new ResultTopic(nodes, links);
    }

    private static List<ResultEdge> buildLinks(List<Keyword> keywords, double threshold, Map<Word, Integer> nodeIndexes) {
        List<ResultEdge> links = new ArrayList<>();
        for (Keyword t1 : keywords) {
            for (Keyword t2 : keywords) {
                if (!t1.equals(t2)) {
                    double sim = SemanticCohesion.getAverageSemanticModelSimilarity(t1.getWord(), t2.getWord());
                    sim = Math.round(sim * 100.0) / 100.0;
                    if (sim >= threshold) {
                        links.add(new ResultEdge("", nodeIndexes.get(t1.getWord()), nodeIndexes.get(t2.getWord()), sim));
                    }
                }
            }
        }
        Collections.sort(links);
        return links;
    }

    private static void appendDegreeValues(List<ResultNode> nodes, List<ResultEdge> links, Map<Word, Integer> nodeIndexes) {
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
        for (ResultNode node : nodes) {
            node.setNoLinks(noLinks.get(node.getId()));
            node.setDegree(degree.get(node.getId()));
        }
    }
}
