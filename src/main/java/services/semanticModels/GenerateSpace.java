package services.semanticModels;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;

import DAO.Word;

public class GenerateSpace {
	static Logger logger = Logger.getLogger(GenerateSpace.class);
	private ISemanticModel semSpace;

	public GenerateSpace(ISemanticModel semSpace) {
		this.semSpace = semSpace;
	}

	public void buildGraph(UndirectedGraph graph, GraphModel graphModel, String start, double threshold, int maxDepth) {
		Word word = Word.getWordFromConcept(start, semSpace.getLanguage());
		Map<Word, Integer> existingNodes = new TreeMap<Word, Integer>();
		Map<Word, Integer> depthNodes = new TreeMap<Word, Integer>();

		List<Word> currentProcessing = new LinkedList<Word>();
		List<Node> addedNodes = new LinkedList<Node>();

		currentProcessing.add(word);
		depthNodes.put(word, 0);
		// add starting point
		Node node = graphModel.factory().newNode(word.getLemma());
		node.getNodeData().setLabel(word.getLemma());
		graph.addNode(node);
		addedNodes.add(node);
		existingNodes.put(word, addedNodes.size() - 1);

		if (semSpace.getWordSet().contains(word)) {
			while (!currentProcessing.isEmpty()) {
				// remove first element
				Word crt = currentProcessing.remove(0);
				if (depthNodes.get(crt) < maxDepth) {
					for (Word to : semSpace.getWordSet()) {
						float dist = (float) semSpace.getSimilarity(crt, to);
						if (!to.equals(crt) && dist >= threshold) {
							// the node does not already exist
							if (!existingNodes.containsKey(to) && !currentProcessing.contains(to)) {
								node = graphModel.factory().newNode(to.getLemma());
								node.getNodeData().setLabel(to.getLemma());
								graph.addNode(node);
								addedNodes.add(node);
								currentProcessing.add(to);
								existingNodes.put(to, addedNodes.size() - 1);
								depthNodes.put(to, depthNodes.get(crt) + 1);
							}
							// add edge
							Edge e = graphModel.factory().newEdge(addedNodes.get(existingNodes.get(crt)),
									addedNodes.get(existingNodes.get(to)));
							e.setWeight(1.0f - dist);
							e.getEdgeData().setLabel(dist + "");
							if (!graph.contains(e))
								graph.addEdge(e);
						}
					}
				} else {
					// add solely remaining edges to build the final graph
					for (Word to : semSpace.getWordSet()) {
						if (!crt.equals(to)) {
							float dist = (float) semSpace.getSimilarity(crt, to);
							if (dist >= threshold) {
								// the node exists within our generated graph
								if (existingNodes.containsKey(to)) {
									// add edge
									Edge e = graphModel.factory().newEdge(addedNodes.get(existingNodes.get(crt)),
											addedNodes.get(existingNodes.get(to)));
									e.setWeight(1.0f - dist);
									e.getEdgeData().setLabel(dist + "");
									if (!graph.contains(e))
										graph.addEdge(e);
								}
							}
						}
					}
				}
			}
		}
	}
}
