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
import data.Word;
import data.discourse.Keyword;
import data.discourse.SemanticCohesion;
import data.sentiment.SentimentGrid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import services.commons.Formatting;
import services.discourse.keywordMining.KeywordModeling;
import webService.result.ResultEdge;
import webService.result.ResultNode;
import webService.result.ResultTopic;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class ConceptMap {

    private static final double MIN_SIZE = 5;
    private static final double MAX_SIZE_TOPIC = 20;

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

    public static ResultTopic getTopics(List<? extends AbstractDocument> queryDocs, double threshold, Set<Word> ignoredWords, int noTopics) {

        List<ResultNode> nodes = new ArrayList<>();
        List<ResultEdge> links = new ArrayList<>();

        List<Keyword> topics = new ArrayList();
        Map<Word, Double> topicScores = KeywordModeling.getCollectionTopics(queryDocs);
        for (Map.Entry<Word, Double> entry : topicScores.entrySet()) {
            if (ignoredWords != null && !ignoredWords.contains(entry.getKey())) {
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
                nodes.add(new ResultNode(i++, t.getWord().getLemma(), Formatting.formatNumber(t.getRelevance()), 1));
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
                        links.add(new ResultEdge("", nodeIndexes.get(t1.getWord()), nodeIndexes.get(t2.getWord()), Formatting.formatNumber(distance)));
                    }
                }
            }
        }

        return new ResultTopic(nodes, links);
    }
}
