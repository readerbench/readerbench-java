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
        List<AbstractDocument> queryDocs = new ArrayList();
        queryDocs.add(queryDoc);
        return getKeywords(queryDocs, threshold, ignoredWords);
    }

    public static ResultTopic getKeywords(List<? extends AbstractDocument> queryDocs, double threshold, Set<Word> ignoredWords) {

        List<ResultNode> nodes = new ArrayList<>();
        List<ResultEdge> links = new ArrayList<>();

        List<Keyword> keywords = KeywordModeling.getCollectionTopics(queryDocs);
        for (Keyword k : keywords) {
            if (ignoredWords.contains(k.getWord())) {
                keywords.remove(k);
            }
        }

        // build nodes
        Map<Word, Integer> nodeIndexes = new TreeMap<>();

        int i = 0, j = 0;
        Lang lang = queryDocs.get(0).getLanguage();
        LSA lsa = (LSA) queryDocs.get(0).getSemanticModel(SimilarityType.LSA);
        LDA lda = (LDA) queryDocs.get(0).getSemanticModel(SimilarityType.LDA);
        Word2VecModel word2Vec = (Word2VecModel) queryDocs.get(0).getSemanticModel(SimilarityType.WORD2VEC);
        Map<Word, Double> mapIdf;
        if (lsa != null) {
            mapIdf = lsa.getMapIdf();
        } else {
            mapIdf = null;
        }
        Map<Word, Integer> wordOcc = queryDocs.get(0).getWordOccurences();
        for (Keyword t : keywords) {
            ResultNode node = new ResultNode(i, t.getWord().getText(), t.getRelevance(), 1);
            nodeIndexes.put(t.getWord(), i);
            node.setLemma(t.getWord().getLemma());
            node.setPos(t.getWord().getPOS());
            t.updateRelevance(queryDocs.get(0), t.getWord());
            node.setNoOcc(wordOcc.get(t.getWord()));
            node.setTf(t.getTermFrequency());
            if (mapIdf != null && mapIdf.containsKey(t.getWord())) {
                node.setIdf(mapIdf.get(t.getWord()));
            } else {
                node.setIdf(-1);
            }

            // similarity scores between word and document using each semantic model
            if (lsa != null) {
                node.addSemanticSimilarity(SimilarityType.LSA.getAcronym(), lsa.getSimilarity(t.getWord(), queryDocs.get(0)));
            }
            if (lda != null) {
                node.addSemanticSimilarity(SimilarityType.LDA.getAcronym(), lda.getSimilarity(t.getWord(), queryDocs.get(0)));
            }
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
        for (Keyword t1 : keywords) {
            for (Keyword t2 : keywords) {
                if (!t1.equals(t2)) {
                    double sim = SemanticCohesion.getAverageSemanticModelSimilarity(t1.getWord(), t2.getWord());
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
        for (ResultNode node : nodes) {
            node.setNoLinks(noLinks.get(node.getId()));
            node.setDegree(degree.get(node.getId()));
        }

        return new ResultTopic(nodes, links);
    }
}
