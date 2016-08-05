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

public class ConceptMap {
	
	private static final double MIN_SIZE = 5;
	private static final double MAX_SIZE_TOPIC = 20;

	/**
	 * Get document topics
	 *
	 * @param query
	 * @return List of keywords and corresponding relevance scores for results
	 */
	public static ResultTopic getTopics(AbstractDocument queryDoc, double threshold, Set<String> ignoredWords) {

		List<ResultNode> nodes = new ArrayList<ResultNode>();
		List<ResultEdge> links = new ArrayList<ResultEdge>();

		// List<Topic> topics = queryDoc.getTopics();
		List<Topic> topics = TopicModeling.getSublist(queryDoc.getTopics(), 50, false, false);
		if (ignoredWords != null) topics = TopicModeling.filterTopics(queryDoc, ignoredWords);

		// build connected graph
		Map<Word, Boolean> visibleConcepts = new TreeMap<Word, Boolean>();
		// build nodes
		Map<Word, Double> nodeSizes = new TreeMap<Word, Double>();
		Map<Word, Integer> nodeGroups = new TreeMap<Word, Integer>();
		SentimentGrid<Double> edges = new SentimentGrid<>(topics.size(), topics.size());
		Map<Word, Integer> nodeIndexes = new TreeMap<Word, Integer>();

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
						double distance = Double.MAX_VALUE;
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
