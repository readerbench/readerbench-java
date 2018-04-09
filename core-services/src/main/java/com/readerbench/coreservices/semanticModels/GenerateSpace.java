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
package com.readerbench.coreservices.semanticModels;

import com.readerbench.datasourceprovider.data.Word;
import com.readerbench.datasourceprovider.data.semanticmodels.ISemanticModel;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import com.readerbench.coreservices.commons.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GenerateSpace {

	private static final Logger LOGGER = LoggerFactory.getLogger(GenerateSpace.class);

	private ISemanticModel semSpace;

	public GenerateSpace(ISemanticModel semSpace) {
		this.semSpace = semSpace;
	}

	public void buildGraph(UndirectedGraph graph, GraphModel graphModel, String start, double threshold, int maxDepth) {
		Word word = Word.getWordFromConcept(start, semSpace.getLanguage());
		Map<Word, Integer> existingNodes = new TreeMap<>();
		Map<Word, Integer> depthNodes = new TreeMap<Word, Integer>();

		List<Word> currentProcessing = new LinkedList<Word>();
		List<Node> addedNodes = new LinkedList<Node>();

		currentProcessing.add(word);
		depthNodes.put(word, 0);
		// add starting point
		Node node = graphModel.factory().newNode(word.getLemma());
		node.setLabel(word.getLemma());
		node.setX((float) ((0.01 + Math.random()) * 1000) - 500);
		node.setY((float) ((0.01 + Math.random()) * 1000) - 500);
		graph.addNode(node);
		addedNodes.add(node);
		existingNodes.put(word, addedNodes.size() - 1);
		int noNodes = 1;
		double s0 = 0, s1 = 0, s2 = 0;

		if (semSpace.getWordSet().contains(word)) {
			while (!currentProcessing.isEmpty()) {
				// remove first element
				Word crt = currentProcessing.remove(0);
				if (depthNodes.get(crt) < maxDepth) {
					for (Word to : semSpace.getWordSet()) {
						float sim = (float) semSpace.getSimilarity(crt, to);
						if (!to.equals(crt) && sim >= threshold) {
							// the node does not already exist
							if (!existingNodes.containsKey(to) && !currentProcessing.contains(to)) {
								node = graphModel.factory().newNode(to.getLemma());
								node.setLabel(to.getLemma());
								node.setX((float) ((0.01 + Math.random()) * 1000) - 500);
								node.setY((float) ((0.01 + Math.random()) * 1000) - 500);
								graph.addNode(node);
								addedNodes.add(node);
								currentProcessing.add(to);
								existingNodes.put(to, addedNodes.size() - 1);
								depthNodes.put(to, depthNodes.get(crt) + 1);
								noNodes++;
							}
							// add edge
							Edge e = graphModel.factory().newEdge(addedNodes.get(existingNodes.get(crt)),
									addedNodes.get(existingNodes.get(to)), 0, 1.0d - sim, false);
							e.setLabel(Formatting.formatNumber(sim) + "");
							if (!graph.contains(e)) {
								graph.addEdge(e);
								s0++;
								s1 += sim;
								s2 += Math.pow(sim, 2);
							}
						}
					}
				} else {
					// add solely remaining edges to build the final graph
					for (Word to : semSpace.getWordSet()) {
						if (!crt.equals(to)) {
							float sim = (float) semSpace.getSimilarity(crt, to);
							if (sim >= threshold) {
								// the node exists within our generated graph
								if (existingNodes.containsKey(to)) {
									// add edge
									Edge e = graphModel.factory().newEdge(addedNodes.get(existingNodes.get(crt)),
											addedNodes.get(existingNodes.get(to)), 0, 1.0d - sim, false);
									e.setLabel(Formatting.formatNumber(sim) + "");

									if (!graph.contains(e)) {
										graph.addEdge(e);
										s0++;
										s1 += sim;
										s2 += Math.pow(sim, 2);
									}
								}
							}
						}
					}
				}
			}
		}
		double mean = 0, stdev = 0;
		if (s0 != 0) {
			mean = s1 / s0;
			stdev = Math.sqrt(s0 * s2 - Math.pow(s1, 2)) / s0;
		}
		LOGGER.info("No nodes:\t" + noNodes + "\tNo edges:\t" + ((int) s0) + "\tAverage similarity:\t"
				+ Formatting.formatNumber(mean) + "\tStdev similarity:\t" + Formatting.formatNumber(stdev));
	}
}
