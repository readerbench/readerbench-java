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
import data.AnalysisElement;
import data.Lang;
import data.NGram;
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
import java.util.stream.Collectors;
import services.complexity.wordComplexity.WordComplexity;
import services.discourse.keywordMining.KeywordModeling;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.SimilarityType;
import services.semanticModels.word2vec.Word2VecModel;
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
        Map<Keyword, Integer> nodeIndexes = new TreeMap<>();

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
            nodeIndexes.put(t, i);
            if (t.getElement() instanceof Word) {
                node.setLemma(t.getWord().getLemma());
                node.setPos(t.getWord().getPOS());
                //t.updateRelevance(queryDoc, t.getWord());
                node.setNoOcc(wordOcc.get(t.getWord()));
            } else {
                NGram nGram = (NGram) t.getElement();
                StringBuilder sb = new StringBuilder();
                for (Word w : nGram.getWords()) {
                    sb.append(w.getLemma()).append(" ");
                }
                node.setLemma(sb.toString());
                sb.setLength(0);
                for (Word w : nGram.getWords()) {
                    sb.append(w.getPOS()).append(" ");
                }
                node.setPos(sb.toString().trim());
                // TODO: update lemma relevance
                //t.updateRelevance(queryDoc, t.getElement());
                int noOcc = 0;
                Map<Word, Integer> nGramWordOccurences = nGram.getWordOccurences();
                for (Word w : nGram.getWords()) {
                    // nGram.getWordOccurences()
                    noOcc += nGramWordOccurences.get(w);   
                }
                node.setNoOcc(noOcc / nGram.getWords().size());
            }
            
            node.setTf(t.getTermFrequency());
            if (t.getElement() instanceof Word) {
                if (mapIdf != null && mapIdf.containsKey(t.getWord())) {
                    node.setIdf(mapIdf.get(t.getWord()));
                } else {
                    node.setIdf(-1);
                }
            } else {
                NGram nGram = (NGram) t.getElement();
                for (Word w : nGram.getWords()) {
                    if (mapIdf != null && mapIdf.containsKey(w)) {
                        node.setIdf(mapIdf.get(w));
                    } else {
                        node.setIdf(-1);
                    }
                }
            }

            // similarity scores between word and document using each semantic model
            if (lsa != null) {
                node.addSemanticSimilarity(SimilarityType.LSA.getAcronym(), lsa.getSimilarity(t.getElement(), queryDoc));
            }
            if (lda != null) {
                node.addSemanticSimilarity(SimilarityType.LDA.getAcronym(), lda.getSimilarity(t.getElement(), queryDoc));
            }
            if (word2Vec != null) {
                node.addSemanticSimilarity(SimilarityType.WORD2VEC.getAcronym(), word2Vec.getSimilarity(t.getElement(), queryDoc));
            }

            if (t.getElement() instanceof Word) {
                node.setAverageDistanceToHypernymTreeRoot(WordComplexity.getAverageDistanceToHypernymTreeRoot(t.getWord(), lang));
                node.setMaxDistanceToHypernymTreeRoot(WordComplexity.getMaxDistanceToHypernymTreeRoot(t.getWord(), lang));
                node.setPolysemyCount(WordComplexity.getPolysemyCount(t.getWord()));
            } else {
                NGram nGram = (NGram) t.getElement();
                double averageDistanceToHypernymTreeRoot = 0.0;
                double maxDistanceToHypernymTreeRoot = 0.0;
                int polysemyCount = 0;
                for (Word w : nGram.getWords()) {
                    averageDistanceToHypernymTreeRoot += WordComplexity.getAverageDistanceToHypernymTreeRoot(w, lang);
                    maxDistanceToHypernymTreeRoot += WordComplexity.getMaxDistanceToHypernymTreeRoot(w, lang);
                    polysemyCount += WordComplexity.getPolysemyCount(w);
                }
                node.setAverageDistanceToHypernymTreeRoot(averageDistanceToHypernymTreeRoot / nGram.getWords().size());
                node.setMaxDistanceToHypernymTreeRoot(maxDistanceToHypernymTreeRoot /  nGram.getWords().size());
                node.setPolysemyCount(polysemyCount / nGram.getWords().size());
            }
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
        if (maxNoWords != null) {
            Collections.sort(keywords);
            keywords = keywords.subList(0, Math.min(keywords.size(), maxNoWords));
        }
        Map<Keyword, Integer> nodeIndexes = new TreeMap<>();

        for (int i = 0; i < keywords.size(); i++) {
            Keyword keyword = keywords.get(i);
            double relevance = Math.round(keyword.getRelevance() * 100.0) / 100.0;
            String lemma;
            if (keyword.getElement() instanceof Word) {
                lemma = keyword.getWord().getLemma();
            }
            else {
                StringBuilder sb = new StringBuilder();
                NGram nGram = (NGram) keyword.getElement();
                for (Word w : nGram.getWords()) {
                    sb.append(w.getLemma()).append(" ");
                }
                lemma = sb.toString();
            }
            ResultNode node = new ResultNode(i, lemma, relevance, 1);
            nodeIndexes.put(keyword, i);
            nodes.add(node);
        }
        List<ResultEdge> links = buildLinks(keywords, threshold, nodeIndexes);
        appendDegreeValues(nodes, links, nodeIndexes);

        return new ResultTopic(nodes, links);
    }

    private static List<ResultEdge> buildLinks(List<Keyword> keywords, double threshold, Map<Keyword, Integer> nodeIndexes) {
        List<ResultEdge> links = new ArrayList<>();
        for (Keyword t1 : keywords) {
            for (Keyword t2 : keywords) {
                if (!t1.equals(t2)) {
                    double sim = SemanticCohesion.getAverageSemanticModelSimilarity(t1.getElement(), t2.getElement());
                    if (sim >= threshold) {
                        links.add(new ResultEdge("", nodeIndexes.get(t1), nodeIndexes.get(t2), sim));
                    }
                }
            }
        }
        Collections.sort(links);
        return links;
    }

    private static void appendDegreeValues(List<ResultNode> nodes, List<ResultEdge> links, Map<Keyword, Integer> nodeIndexes) {
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
