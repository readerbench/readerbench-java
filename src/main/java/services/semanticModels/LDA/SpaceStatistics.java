package services.semanticModels.LDA;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.statistics.plugin.ClusteringCoefficient;
import org.gephi.statistics.plugin.ConnectedComponents;
import org.gephi.statistics.plugin.GraphDensity;
import org.gephi.statistics.plugin.GraphDistance;
import org.gephi.statistics.plugin.Modularity;
import org.openide.util.Lookup;

import DAO.Word;
import edu.cmu.lti.jawjaw.pobj.Lang;
import services.commons.Formatting;

public class SpaceStatistics {
	private static final double MINIMUM_IMPOSED_THRESHOLD = 0.3d;

	static Logger logger = Logger.getLogger(SpaceStatistics.class);

	private LDA lda;
	private int noWords;
	private List<Connection> significantSimilarities;

	public SpaceStatistics(LDA lda) {
		logger.info("Building statistics for " + lda.getPath() + "...");
		this.lda = lda;
		this.noWords = lda.getWordProbDistributions().size();

		significantSimilarities = new ArrayList<Connection>();
	}

	public void buildWordDistances() {
		System.out.println("No. words in semantic model dictionary:\t" + noWords);
		double sim;
		double s00 = 0, s10 = 0, s20 = 0;
		double s01 = 0, s11 = 0, s21 = 0;
		List<Connection> allSimilarities = new ArrayList<Connection>();
		for (Entry<Word, double[]> e1 : lda.getWordProbDistributions().entrySet()) {
			for (Entry<Word, double[]> e2 : lda.getWordProbDistributions().entrySet()) {
				if (e1.getKey().getLemma().compareTo(e2.getKey().getLemma()) > 0) {
					sim = LDA.getSimilarity(e1.getValue(), e2.getValue());
					s00++;
					s10 += sim;
					s20 += Math.pow(sim, 2);
					if (sim >= MINIMUM_IMPOSED_THRESHOLD) {
						allSimilarities.add(new Connection(e1.getKey().getLemma(), e2.getKey().getLemma(), sim));
						s01++;
						s11 += sim;
						s21 += Math.pow(sim, 2);
					}
				}
			}
		}
		double avg = -1, stdev = -1;
		if (s00 != 0) {
			avg = s10 / s00;
			stdev = Math.sqrt(s00 * s20 - Math.pow(s10, 2)) / s00;
		}
		System.out.println("No. potential word associations:\t" + Formatting.formatNumber(s00));
		System.out.println("Average similarity for all word associations:\t" + Formatting.formatNumber(avg));
		System.out.println("Stdev similarity for all word associations:\t" + Formatting.formatNumber(stdev));

		avg = -1;
		stdev = -1;
		if (s01 != 0) {
			avg = s11 / s01;
			stdev = Math.sqrt(s01 * s21 - Math.pow(s11, 2)) / s01;
		}
		System.out.println("No. word associations (above minimum threshold):\t" + Formatting.formatNumber(s01));
		System.out.println("Average similarity for all word associations (above minimum threshold):\t"
				+ Formatting.formatNumber(avg));
		System.out.println("Stdev similarity for all word associations (above minimum threshold):\t"
				+ Formatting.formatNumber(stdev));

		// add only significant edges
		double threshold = avg - stdev;
		double s02 = 0, s12 = 0, s22 = 0;

		for (Connection c : allSimilarities) {
			if (c.getSimilarity() >= threshold) {
				significantSimilarities.add(c);
				s02++;
				s12 += c.getSimilarity();
				s22 += Math.pow(c.getSimilarity(), 2);
			}
		}
		avg = -1;
		stdev = -1;
		if (s02 != 0) {
			avg = s12 / s02;
			stdev = Math.sqrt(s02 * s22 - Math.pow(s12, 2)) / s02;
		}
		System.out.println("No significant word associations (above avg-stdev):\t" + Formatting.formatNumber(s02));
		System.out.println("Average similarity for significant word associations (above avg-stdev):\t"
				+ Formatting.formatNumber(avg));
		System.out.println("Stdev similarity for significant word associations (above avg-stdev):\t"
				+ Formatting.formatNumber(stdev));
	}

	public void computeGraphStatistics() {
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();

		// get models
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
		DirectedGraph graph = graphModel.getDirectedGraph();
		Map<String, Node> associations = new TreeMap<String, Node>();

		// build all nodes
		for (Word w : lda.getWordProbDistributions().keySet()) {
			Node wordNode = graphModel.factory().newNode(w.getLemma());
			wordNode.getNodeData().setLabel(w.getLemma());
			associations.put(w.getLemma(), wordNode);
			graph.addNode(wordNode);
		}

		for (Connection c : significantSimilarities) {
			graph.addEdge(associations.get(c.getWord1()), associations.get(c.getWord2()));
			Edge e = graph.getEdge(associations.get(c.getWord1()), associations.get(c.getWord2()));
			e.setWeight((float) (c.getSimilarity()));
		}

		GraphDensity density = new GraphDensity();
		density.setDirected(false);
		density.execute(graphModel, attributeModel);
		System.out.println("Semantic model density:\t" + Formatting.formatNumber(density.getDensity()));

		Modularity modularity = new Modularity();
		modularity.execute(graphModel, attributeModel);
		System.out
				.println("Semantic model average modularity:\t" + Formatting.formatNumber(modularity.getModularity()));

		ConnectedComponents connectedComponents = new ConnectedComponents();
		connectedComponents.setDirected(false);
		connectedComponents.execute(graphModel, attributeModel);
		System.out.println("No connected components within semantic model:\t"
				+ Formatting.formatNumber(connectedComponents.getConnectedComponentsCount()));

		ClusteringCoefficient clusteringCoefficient = new ClusteringCoefficient();
		clusteringCoefficient.setDirected(false);
		clusteringCoefficient.execute(graphModel, attributeModel);
		System.out.println("Semantic model average clustering coefficient:\t"
				+ Formatting.formatNumber(clusteringCoefficient.getAverageClusteringCoefficient()));

		GraphDistance distance = new GraphDistance();
		distance.setDirected(false);
		distance.execute(graphModel, attributeModel);

		// Determine various metrics
		AttributeColumn betweennessColumn = attributeModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);

		AttributeColumn closenessColumn = attributeModel.getNodeTable().getColumn(GraphDistance.CLOSENESS);

		AttributeColumn eccentricityColumn = attributeModel.getNodeTable().getColumn(GraphDistance.ECCENTRICITY);

		double avgBetweenness = 0, avgCloseness = 0, avgEccentricity = 0;

		for (Node n : graph.getNodes()) {
			avgBetweenness += ((Double) n.getNodeData().getAttributes().getValue(betweennessColumn.getIndex()));
			avgCloseness += ((Double) n.getNodeData().getAttributes().getValue(closenessColumn.getIndex()));
			avgEccentricity += ((Double) n.getNodeData().getAttributes().getValue(eccentricityColumn.getIndex()));
		}
		if (graph.getNodeCount() != 0) {
			System.out.println(
					"Average word betweenness:\t" + Formatting.formatNumber(avgBetweenness / graph.getNodeCount()));
			System.out.println(
					"Average word closeness:\t" + Formatting.formatNumber(avgCloseness / graph.getNodeCount()));
			System.out.println(
					"Average word eccentricity:\t" + Formatting.formatNumber(avgEccentricity / graph.getNodeCount()));
		}

		System.out.println("Semantic model diameter:\t" + Formatting.formatNumber(distance.getDiameter()));

		System.out.println("Semantic model path length:\t" + Formatting.formatNumber(distance.getPathLength()));
	}

	private class Connection {
		private String word1;
		private String word2;
		private double similarity;

		public Connection(String word1, String word2, double similarity) {
			super();
			this.word1 = word1;
			this.word2 = word2;
			this.similarity = similarity;
		}

		public String getWord1() {
			return word1;
		}

		public String getWord2() {
			return word2;
		}

		public double getSimilarity() {
			return similarity;
		}
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();

		SpaceStatistics ss = new SpaceStatistics(LDA.loadLDA("in/HDP/grade4", Lang.eng));
		ss.buildWordDistances();
		ss.computeGraphStatistics();
	}

}
