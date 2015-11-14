package services.complexity.flow;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import DAO.AbstractDocument;
import services.complexity.ComplexityIndices;

public class DocumentFlow {
	private List<Integer> orderedParagraphs;
	private double[][] graph;

	public static enum Criteria {
		MAX_VALUE, ABOVE_MEAN_PLUS_STDEV;

		@Override
		public String toString() {
			switch (this) {
			case MAX_VALUE:
				return "Maximum value";
			case ABOVE_MEAN_PLUS_STDEV:
				return "Above mean+stdev";
			default:
				throw new IllegalArgumentException();
			}
		}
	};

	public DocumentFlow(AbstractDocument doc, int semanticDistanceIndex, Criteria crit) {
		this.graph = new double[doc.getBlocks().size()][doc.getBlocks().size()];

		switch (crit) {
		case MAX_VALUE:
			for (int i = 0; i < doc.getBlocks().size() - 1; i++) {
				// determine max value index
				double maxVal = 0;
				int maxIndex = -1;
				for (int j = i + 1; j < doc.getBlocks().size(); j++) {
					double coh = doc.getBlockDistances()[i][j].getSemanticDistances()[semanticDistanceIndex];
					if (coh > 0) {
						if (coh > maxVal) {
							maxVal = coh;
							maxIndex = j;
						}
					}
				}
				if (maxIndex != -1) graph[i][maxIndex] = maxVal;
			}
			break;

		case ABOVE_MEAN_PLUS_STDEV:
			double s0 = 0, s1 = 0, s2 = 0, mean = 0, stdev = 0;
			// determine mean+stdev
			for (int i = 0; i < doc.getBlocks().size() - 1; i++) {
				for (int j = i + 1; j < doc.getBlocks().size(); j++) {
					double coh = doc.getBlockDistances()[i][j].getSemanticDistances()[semanticDistanceIndex];
					if (coh > 0) {
						s0++;
						s1 += coh;
						s2 += Math.pow(coh, 2);
					}
				}
			}
			if (s0 != 0) {
				mean = s1 / s0;
				stdev = Math.sqrt(s0 * s2 - Math.pow(s1, 2)) / s0;
			}

			for (int i = 0; i < doc.getBlocks().size() - 1; i++) {
				for (int j = i + 1; j < doc.getBlocks().size(); j++) {
					double coh = doc.getBlockDistances()[i][j].getSemanticDistances()[semanticDistanceIndex];
					if (coh >= mean + stdev) {
						graph[i][j] = coh;
					}
				}
			}
			break;
		}

		performTopologicSort();
	}

	public void performTopologicSort() {
		// copy initial graph
		double[][] tmpGraph = new double[graph.length][graph.length];
		for (int i = 0; i < graph.length - 1; i++)
			for (int j = i + 1; j < graph.length; j++)
				tmpGraph[i][j] = graph[i][j];

		orderedParagraphs = new ArrayList<Integer>();
		while (true) {
			Set<Integer> s = new TreeSet<Integer>();
			for (int i = 0; i < tmpGraph.length; i++) {
				int noIncoming = 0;
				// determine number of incoming edges for node i
				for (int j = 0; j < tmpGraph[i].length; j++)
					noIncoming += (tmpGraph[j][i] > 0) ? 1 : 0;
				if (noIncoming == 0 && !orderedParagraphs.contains(i)) {
					s.add(i);
				}
			}
			if (!s.isEmpty()) {
				for (int i : s) {
					orderedParagraphs.add(i);
					for (int j = 0; j < tmpGraph[i].length; j++) {
						tmpGraph[i][j] = 0;
						tmpGraph[j][i] = 0;
					}
				}
			} else {
				break;
			}
		}
	}

	public List<Integer> getOrderedParagraphs() {
		return orderedParagraphs;
	}

	public double[][] getGraph() {
		return graph;
	}

	public double getAbsolutePositionAccuracy() {
		if (orderedParagraphs.size() < 3)
			return ComplexityIndices.IDENTITY;
		int no = 0;
		for (int i = 0; i < orderedParagraphs.size(); i++) {
			if (orderedParagraphs.get(i) == i) {
				no++;
			}
		}
		return ((double) no) / orderedParagraphs.size();
	}

	public double getAbsoluteDistanceAccuracy() {
		if (orderedParagraphs.size() < 3)
			return ComplexityIndices.IDENTITY;
		int dist = 0;
		for (int i = 0; i < orderedParagraphs.size(); i++) {
			dist += Math.abs(orderedParagraphs.get(i) - i);
		}
		return ((double) dist) / orderedParagraphs.size();
	}

	public double getAdjacencyAccuracy() {
		if (orderedParagraphs.size() < 3)
			return ComplexityIndices.IDENTITY;
		double dist = 0, no = 0;
		for (int i = 0; i < graph.length - 1; i++) {
			for (int j = i + 1; j < graph.length; j++) {
				if (graph[i][j] > 0) {
					dist += Math.abs(j - i - 1);
					no++;
				}
			}
		}
		if (no != 0)
			dist /= no;
		return dist;
	}

	public double getAverageFlowCohesion() {
		if (orderedParagraphs.size() < 3)
			return ComplexityIndices.IDENTITY;
		double coh = 0, no = 0;
		for (int i = 0; i < graph.length - 1; i++) {
			for (int j = i + 1; j < graph.length; j++) {
				if (graph[i][j] > 0) {
					coh += graph[i][j];
					no++;
				}
			}
		}
		if (no != 0)
			coh /= no;
		return coh;
	}

	public double getSpearmanCorrelation() {
		if (orderedParagraphs.size() < 3)
			return ComplexityIndices.IDENTITY;
		double[] initialOrder = new double[orderedParagraphs.size()];
		double[] order = new double[orderedParagraphs.size()];

		for (int i = 0; i < orderedParagraphs.size(); i++) {
			initialOrder[i] = i;
			order[i] = orderedParagraphs.get(i);
		}

		SpearmansCorrelation spearman = new SpearmansCorrelation();

		return spearman.correlation(initialOrder, order);
	}

	public double getMaxOrderedSequence() {
		if (orderedParagraphs.size() < 3)
			return ComplexityIndices.IDENTITY;
		int[] maxOrdered = new int[orderedParagraphs.size()];

		for (int i = 0; i < orderedParagraphs.size(); i++) {
			for (int j = 0; j < i; j++) {
				if (orderedParagraphs.get(j) < orderedParagraphs.get(i)) {
					maxOrdered[i] = Math.max(maxOrdered[i], maxOrdered[j] + 1);
				}
			}
		}
		int max = 0;
		for (int v : maxOrdered)
			max = Math.max(max, v + 1);

		return ((double) max) / orderedParagraphs.size();
	}
}
