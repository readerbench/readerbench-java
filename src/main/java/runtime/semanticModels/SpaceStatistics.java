package runtime.semanticModels;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.gephi.graph.api.Column;
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

import data.Word;
import data.document.Document;
import data.Lang;
import services.commons.Formatting;
import services.semanticModels.LDA.LDA;

public class SpaceStatistics {
	private static final double MINIMUM_IMPOSED_THRESHOLD = 0.3d;

	static Logger logger = Logger.getLogger(SpaceStatistics.class);

	private LDA lda;
	private int noWords;
	private List<Connection> relevantSimilarities;

	public SpaceStatistics(LDA lda) {
		logger.info("Loading " + lda.getPath() + "...");
		this.lda = lda;
		this.noWords = lda.getWordProbDistributions().size();

		relevantSimilarities = new ArrayList<Connection>();
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
				relevantSimilarities.add(c);
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
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
		DirectedGraph graph = graphModel.getDirectedGraph();
		Map<String, Node> associations = new TreeMap<String, Node>();

		// build all nodes
		for (Word w : lda.getWordProbDistributions().keySet()) {
			Node wordNode = graphModel.factory().newNode(w.getLemma());
			wordNode.setLabel(w.getLemma());
			associations.put(w.getLemma(), wordNode);
			graph.addNode(wordNode);
		}

		for (Connection c : relevantSimilarities) {
			Edge e = graphModel.factory().newEdge(associations.get(c.getWord1()), associations.get(c.getWord2()));
			e.setWeight((float) (c.getSimilarity()));
			graph.addEdge(e);
		}

		GraphDensity density = new GraphDensity();
		density.setDirected(false);
		density.execute(graphModel);
		System.out.println("Semantic model density:\t" + Formatting.formatNumber(density.getDensity()));

		Modularity modularity = new Modularity();
		modularity.execute(graphModel);
		System.out
				.println("Semantic model average modularity:\t" + Formatting.formatNumber(modularity.getModularity()));

		ConnectedComponents connectedComponents = new ConnectedComponents();
		connectedComponents.setDirected(false);
		connectedComponents.execute(graphModel);
		System.out.println("No connected components within semantic model:\t"
				+ Formatting.formatNumber(connectedComponents.getConnectedComponentsCount()));

		ClusteringCoefficient clusteringCoefficient = new ClusteringCoefficient();
		clusteringCoefficient.setDirected(false);
		clusteringCoefficient.execute(graphModel);
		System.out.println("Semantic model average clustering coefficient:\t"
				+ Formatting.formatNumber(clusteringCoefficient.getAverageClusteringCoefficient()));

		GraphDistance distance = new GraphDistance();
		distance.setDirected(false);
		distance.execute(graphModel);

		// Determine various metrics
		double avgBetweenness = 0, avgCloseness = 0, avgEccentricity = 0;
		Column betweeennessColumn = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
		Column closenessColumn = graphModel.getNodeTable().getColumn(GraphDistance.CLOSENESS);
		Column eccentricityColumn = graphModel.getNodeTable().getColumn(GraphDistance.CLOSENESS);

		for (Node n : graph.getNodes()) {
			avgBetweenness += (Double) n.getAttribute(betweeennessColumn);
			avgCloseness += (Double) n.getAttribute(closenessColumn);
			avgEccentricity += (Double) n.getAttribute(eccentricityColumn);
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

	public List<Connection> getRelevantSimilarities() {
		return relevantSimilarities;
	}

	public LDA getLDA() {
		return lda;
	}

	/**
	 * Compares all pairs of concepts from the baseline to all subsequent
	 * corpora
	 * 
	 * The first space is the baseline
	 * 
	 * @param baseline
	 * @param corpora
	 */
	public static void compareSpaces(String pathToOutput, List<SpaceStatistics> corpora) {
		corpora.get(0).buildWordDistances();
		logger.info("Writing comparisons based on baseline corpus ...");
		File output = new File(pathToOutput);

		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"),
					32768);
			out.write("Word 1, Word 2");
			for (SpaceStatistics space : corpora) {
				out.write("," + space.getLDA().getPath());
			}

			for (Connection c : corpora.get(0).getRelevantSimilarities()) {
				if (c.getSimilarity() > 0) {
					String outputString = "\n" + c.getWord1() + "," + c.getWord2();
					boolean viableEntry = true;
					for (SpaceStatistics space : corpora) {
						double similarity = space.getLDA().getSimilarity(
								Word.getWordFromConcept(c.getWord1(), space.getLDA().getLanguage()),
								Word.getWordFromConcept(c.getWord2(), space.getLDA().getLanguage()));
						if (similarity > 0) {
							outputString += "," + similarity;
						} else {
							viableEntry = false;
							break;
						}
					}
					if (viableEntry) {
						out.write(outputString);
					}
				}
			}
			out.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Determine word linkage power corpora
	 * 
	 * The first space is the baseline
	 * 
	 * @param baseline
	 * @param corpora
	 */

	private enum SNAIndices {
		DEGREE, SIM_DEGREE, BETWEENNESS, CLOSENESS, ECCENTRICITY
	}

	public static void determineWLP(String pathToOutput, SpaceStatistics space, LDA referenceSpace) {
		logger.info("Writing word linkage power statistics based on baseline corpus ...");
		File output = new File(pathToOutput);

		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"),
					32768);
			out.write("Word");
			for (SNAIndices index : SNAIndices.values())
				out.write("," + index.name() + "(" + space.getLDA().getPath() + ")");
			for (SNAIndices index : SNAIndices.values())
				out.write(",NORM_" + index.name() + "(" + space.getLDA().getPath() + ")");

			Map<String, EnumMap<SNAIndices, Double>> wlps = new TreeMap<String, EnumMap<SNAIndices, Double>>();
			for (Word w : space.getLDA().getWordSet()) {
				EnumMap<SNAIndices, Double> scores = new EnumMap<SNAIndices, Double>(SNAIndices.class);
				scores.put(SNAIndices.SIM_DEGREE, 0d);
				wlps.put(w.getLemma(), scores);
			}

			ProjectController pc;
			GraphModel graphModel;
			DirectedGraph graph;
			Map<String, Node> associations;
			GraphDistance distance;

			logger.info("Processing SNA indices for " + space.getLDA().getPath() + "...");
			space.buildWordDistances();
			pc = Lookup.getDefault().lookup(ProjectController.class);
			pc.newProject();

			// get models
			graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
			graph = graphModel.getDirectedGraph();
			associations = new TreeMap<String, Node>();

			// build all nodes
			for (Word w : space.getLDA().getWordProbDistributions().keySet()) {
				Node wordNode = graphModel.factory().newNode(w.getLemma());
				wordNode.setLabel(w.getLemma());
				associations.put(w.getLemma(), wordNode);
				graph.addNode(wordNode);
			}

			for (Connection c : space.getRelevantSimilarities()) {
				Edge e = graphModel.factory().newEdge(associations.get(c.getWord1()), associations.get(c.getWord2()), 0,
						c.getSimilarity(), false);
				graph.addEdge(e);

				wlps.get(c.getWord1()).put(SNAIndices.SIM_DEGREE,
						wlps.get(c.getWord1()).get(SNAIndices.SIM_DEGREE) + c.getSimilarity());
				wlps.get(c.getWord2()).put(SNAIndices.SIM_DEGREE,
						wlps.get(c.getWord2()).get(SNAIndices.SIM_DEGREE) + c.getSimilarity());
			}

			logger.info("Computing SNA indices...");
			distance = new GraphDistance();
			distance.setDirected(false);
			distance.execute(graphModel);

			// Determine various metrics
			Column betweeennessColumn = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
			Column closenessColumn = graphModel.getNodeTable().getColumn(GraphDistance.CLOSENESS);
			Column eccentricityColumn = graphModel.getNodeTable().getColumn(GraphDistance.ECCENTRICITY);

			for (Node n : graph.getNodes()) {
				double degree = graph.getDegree(n);
				wlps.get(n.getLabel()).put(SNAIndices.DEGREE, degree);

				double betweennessScore = (Double) n.getAttribute(betweeennessColumn);
				wlps.get(n.getLabel()).put(SNAIndices.BETWEENNESS, betweennessScore);

				double closenessScore = (Double) n.getAttribute(closenessColumn);
				wlps.get(n.getLabel()).put(SNAIndices.CLOSENESS, closenessScore);

				double eccentricityScore = (Double) n.getAttribute(eccentricityColumn);
				wlps.get(n.getLabel()).put(SNAIndices.ECCENTRICITY, eccentricityScore);
			}

			logger.info("Writing final results...");
			// print results
			for (Word w : referenceSpace.getWordSet()) {
				out.write("\n" + w.getLemma());
				if (wlps.containsKey(w.getLemma())) {
					for (SNAIndices index : SNAIndices.values()) {
						out.write("," + wlps.get(w.getLemma()).get(index));
					}
					for (SNAIndices index : SNAIndices.values()) {
						out.write("," + wlps.get(w.getLemma()).get(index) / space.getLDA().getWordSet().size());
					}
				} else {
					out.write(",,,,");
				}
			}

			out.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Compares all pairs of norms from the baseline to all subsequent corpora
	 * 
	 * @param corpora
	 */
	public static void compareSpaces(String pathToInputNorms, int countMax, String pathToOutput,
			List<SpaceStatistics> corpora) {
		logger.info("Loading frequent word associations ...");
		WordAssociationTest comp = new WordAssociationTest();
		comp.initialLoad(pathToInputNorms, corpora.get(0).getLDA(), countMax);

		logger.info("Writing comparisons for frequent word associations ...");
		File output = new File(pathToOutput);

		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"),
					32768);
			out.write("Word 1, Word 2");
			for (SpaceStatistics space : corpora) {
				out.write("," + space.getLDA().getPath());
			}

			for (Document doc1 : comp.getWordAssociations().keySet()) {
				for (Document doc2 : comp.getWordAssociations().get(doc1).keySet()) {
					String outputString = "\n" + doc1.getProcessedText() + "," + doc2.getProcessedText();
					boolean viableEntry = true;
					for (SpaceStatistics space : corpora) {
						double similarity = space.getLDA().getSimilarity(doc1, doc2);
						if (similarity > 0) {
							outputString += "," + similarity;
						} else {
							viableEntry = false;
							break;
						}
					}
					if (viableEntry) {
						out.write(outputString);
					}
				}
			}
			out.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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

		// SpaceStatistics ss = new SpaceStatistics(LDA.loadLDA("in/HDP/grade4",
		// Lang.eng));
		// ss.buildWordDistances();
		// ss.computeGraphStatistics();
		// int initialGrade = 2;
		// SpaceStatistics baseline = new
		// SpaceStatistics(LDA.loadLDA("resources/in/HDP/grade" + initialGrade,
		// Lang.eng));
		// List<SpaceStatistics> corpora = new ArrayList<SpaceStatistics>();
		//
		// corpora.add(baseline);
		// for (int i = initialGrade - 1; i > 0; i--) {
		// corpora.add(new SpaceStatistics(LDA.loadLDA("resources/in/HDP/grade"
		// + i, Lang.eng)));
		// }

		// compareSpaces("resources/in/HDP/comparison HDP 12-.csv", corpora);
		//
		// compareSpaces("resources/config/LSA/word_associations_en.txt", 3,
		// "resources/in/HDP/comparison HDP Nelson.csv", corpora);

		int gradeLevel = 0;
		LDA matureSpace = LDA.loadLDA("resources/in/HDP/grade12", Lang.eng);
		determineWLP("resources/in/HDP/WLP HDP " + gradeLevel + ".csv",
				new SpaceStatistics(LDA.loadLDA("resources/in/HDP/grade" + gradeLevel, Lang.eng)), matureSpace);
	}

}