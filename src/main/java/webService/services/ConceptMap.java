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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import data.AbstractDocument;
import data.Word;
import data.discourse.SemanticCohesion;
import data.discourse.Topic;
import data.sentiment.SentimentGrid;
import services.commons.Formatting;
import services.discourse.topicMining.TopicModeling;
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
	 * @return List of keywords and corresponding relevance scores for results
	 */
	public static ResultTopic getTopics(AbstractDocument queryDoc, double threshold, Set<String> ignoredWords) {

		List<ResultNode> nodes = new ArrayList<>();
		List<ResultEdge> links = new ArrayList<>();

		// List<Topic> topics = queryDoc.getTopics();
		List<Topic> topics = TopicModeling.getSublist(queryDoc.getTopics(), 50, false, false);
		if (ignoredWords != null) topics = TopicModeling.filterTopics(queryDoc, ignoredWords);

		// build connected graph
		Map<Word, Boolean> visibleConcepts = new TreeMap<>();
		// build nodes
		SentimentGrid<Double> edges = new SentimentGrid<>(topics.size(), topics.size());
		Map<Word, Integer> nodeIndexes = new TreeMap<>();

		for (Topic t : topics) {
			visibleConcepts.put(t.getWord(), false);
		}

		// determine similarities
		for (Word w1 : visibleConcepts.keySet()) {
			for (Word w2 : visibleConcepts.keySet()) {
				double lsaSim = 0;
				double ldaSim = 0;
				if (queryDoc.getLSA() != null)
					lsaSim = queryDoc.getLSA().getSimilarity(w1, w2);
				if (queryDoc.getLDA() != null)
					ldaSim = queryDoc.getLDA().getSimilarity(w1, w2);
				double sim = SemanticCohesion.getAggregatedSemanticMeasure(lsaSim, ldaSim);
				if (!w1.equals(w2) && sim >= threshold) {
					visibleConcepts.put(w1, true);
					visibleConcepts.put(w2, true);
				}
			}
		}

		// determine optimal sizes
		double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
		for (Topic t : topics) {
			if (visibleConcepts.get(t.getWord()) && t.getRelevance() >= 0) {
				min = Math.min(min, Math.log(1 + t.getRelevance()));
				max = Math.max(max, Math.log(1 + t.getRelevance()));
			}
		}

		int i = 0, j;
		for (Topic t : topics) {
			if (visibleConcepts.get(t.getWord())) {
				double nodeSize = 0;
				if (max != min && t.getRelevance() >= 0) {
					nodeSize = (MIN_SIZE
							+ (Math.log(1 + t.getRelevance()) - min) / (max - min) * (MAX_SIZE_TOPIC - MIN_SIZE));
				} else {
					nodeSize = MIN_SIZE;
				}
				nodeIndexes.put(t.getWord(), i);
				nodes.add(new ResultNode(i++, t.getWord().getLemma(), Formatting.formatNumber(t.getRelevance()), 1));
			}
		}

		// determine similarities
		i = 0;
		j = 0;
		for (Word w1 : visibleConcepts.keySet()) {
			edges.setIndex(w1.toString(), i++);
			for (Word w2 : visibleConcepts.keySet()) {
				edges.setIndex(w2.toString(), j++);
				if (!w1.equals(w2) && visibleConcepts.get(w1) && visibleConcepts.get(w2)) {
					double lsaSim = 0;
					double ldaSim = 0;
					if (queryDoc.getLSA() != null) {
						lsaSim = queryDoc.getLSA().getSimilarity(w1, w2);
					}
					if (queryDoc.getLDA() != null) {
						ldaSim = queryDoc.getLDA().getSimilarity(w1, w2);
					}
					double sim = SemanticCohesion.getAggregatedSemanticMeasure(lsaSim, ldaSim);
					if (sim >= threshold) {
						double distance;
						if (sim > .9)
							distance = 1;
						else
							distance = (1f - sim) * 10;
						links.add(new ResultEdge("", nodeIndexes.get(w1), nodeIndexes.get(w2), Formatting.formatNumber(distance)));
					}
				}
			}
		}

		return new ResultTopic(nodes, links);
	}
	
}
