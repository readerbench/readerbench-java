package services.discourse.CSCL;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.plugin.RankingLabelSizeTransformer;
import org.gephi.appearance.plugin.RankingNodeSizeTransformer;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.types.DependantOriginalColor;
import org.gephi.project.api.ProjectController;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;

import data.Block;
import data.Word;
import data.cscl.CSCLIndices;
import data.cscl.Conversation;
import data.cscl.Participant;
import data.cscl.Utterance;
import data.discourse.SemanticCohesion;
import data.discourse.Topic;
import services.commons.Formatting;

public class ParticipantEvaluation {
	static Logger logger = Logger.getLogger(ParticipantEvaluation.class);

	public static void buildParticipantGraph(String genericName, DirectedGraph graph, GraphModel graphModel,
			List<Participant> participants, double[][] participantContributions, boolean displayEdgeLabels,
			boolean isAnonymized) {

		Node[] participantNodes = new Node[participants.size()];

		// TODO change list for specific elements to ignore
		// Set<String> namesToIgnore = new TreeSet<String>(Arrays.asList(new
		// String[] { "2093911", "1516180", "90343" }));

		Color colorParticipant = Color.LIGHT_GRAY;

		// build all nodes
		for (int i = 0; i < participants.size(); i++) {
			// if (!namesToIgnore.contains(participants.get(i).getName())) {
			// build block element
			Node participant = null;
			if (isAnonymized) {
				participant = graphModel.factory().newNode(genericName + " " + i);
				participant.setLabel(genericName + " " + i);
			} else {
				participant = graphModel.factory().newNode(participants.get(i).getName());
				participant.setLabel(participants.get(i).getName());
			}
			participant.setX((float) ((0.01 + Math.random()) * 1000) - 500);
			participant.setY((float) ((0.01 + Math.random()) * 1000) - 500);
			participant.setColor(colorParticipant);
			graph.addNode(participant);
			participantNodes[i] = participant;
			// } else {
			// logger.info("Ignoring " + participants.get(i).getName());
			// }
		}

		// determine max value
		double maxVal = Double.MIN_VALUE;
		for (int i = 0; i < participants.size(); i++) {
			for (int j = 0; j < participants.size(); j++) {
				// if (!namesToIgnore.contains(participants.get(i).getName())
				// && !namesToIgnore.contains(participants.get(j).getName())) {
				maxVal = Math.max(maxVal, participantContributions[i][j]);
				// }
			}
		}

		for (int i = 0; i < participants.size(); i++) {
			for (int j = 0; j < participants.size(); j++) {
				if (participantContributions[i][j] > 0
				// && !namesToIgnore.contains(participants.get(i).getName())
				// && !namesToIgnore.contains(participants.get(j).getName())
				) {
					Edge e = graphModel.factory().newEdge(participantNodes[i], participantNodes[j], 0,
							participantContributions[i][j] / maxVal, true);
					if (displayEdgeLabels) {
						e.setLabel(Formatting.formatNumber(participantContributions[i][j]) + "");
					} else {
						e.setLabel("");
					}
					graph.addEdge(e);
				}
			}
		}
	}

	public static void evaluateInteraction(Conversation c) {
		if (c.getParticipants().size() > 0) {
			c.setParticipantContributions(new double[c.getParticipants().size()][c.getParticipants().size()]);
			List<Participant> lsPart = getParticipantList(c);
			// determine strength of links
			for (int i = 0; i < c.getBlocks().size() - 1; i++) {
				if (c.getBlocks().get(i) != null) {
					Participant p1 = ((Utterance) c.getBlocks().get(i)).getParticipant();
					int index1 = lsPart.indexOf(p1);
					// c.getParticipantContributions()[index1][index1] += c
					// .getBlocks().get(i).getOverallScore();
					for (int j = i + 1; j < Math.min(c.getBlocks().size(), i + SemanticCohesion.WINDOW_SIZE + 1); j++) {
						if (c.getPrunnedBlockDistances()[j][i] != null) {
							Participant p2 = ((Utterance) c.getBlocks().get(j)).getParticipant();
							int index2 = lsPart.indexOf(p2);
							c.getParticipantContributions()[index2][index1] += c.getBlocks().get(j).getOverallScore()
									* c.getPrunnedBlockDistances()[j][i].getCohesion();
							c.getParticipantContributions()[index1][index2] += c.getBlocks().get(i).getOverallScore()
									* c.getPrunnedBlockDistances()[j][i].getCohesion();
						}
					}
				}
			}
		}
	}

	private static List<Participant> getParticipantList(Conversation c) {
		Iterator<Participant> it = c.getParticipants().iterator();
		List<Participant> lsPart = new ArrayList<Participant>();
		while (it.hasNext()) {
			Participant part = it.next();
			lsPart.add(part);
		}
		return lsPart;
	}

	public static void evaluateInvolvement(Conversation c) {
		if (c.getParticipants().size() > 0) {
			for (Block b : c.getBlocks()) {
				if (b != null) {
					Utterance u = (Utterance) b;
					u.getParticipant().getIndices().put(CSCLIndices.OVERALL_SCORE,
							u.getParticipant().getIndices().get(CSCLIndices.OVERALL_SCORE) + b.getOverallScore());
					u.getParticipant().getIndices().put(CSCLIndices.PERSONAL_KB,
							u.getParticipant().getIndices().get(CSCLIndices.PERSONAL_KB) + u.getPersonalKB());
					u.getParticipant().getIndices().put(CSCLIndices.SOCIAL_KB,
							u.getParticipant().getIndices().get(CSCLIndices.SOCIAL_KB) + u.getSocialKB());
					u.getParticipant().getIndices().put(CSCLIndices.NO_CONTRIBUTION,
							u.getParticipant().getIndices().get(CSCLIndices.NO_CONTRIBUTION) + 1);
				}
			}
		}
	}

	public static void evaluateUsedConcepts(Conversation c) {
		// determine cumulated effect of top 10 topics (nouns and verbs only)
		int noSelectedTopics = 0;
		for (Topic topic : c.getTopics()) {
			if (topic.getWord().getPOS() == null
					|| (topic.getWord().getPOS().startsWith("N") || topic.getWord().getPOS().startsWith("V"))) {
				noSelectedTopics++;
				// update for all participants
				for (Participant p : c.getParticipants()) {
					if (p.getInterventions().getWordOccurences().containsKey(topic.getWord())) {
						double relevance = p.getInterventions().getWordOccurences().get(topic.getWord())
								* topic.getRelevance();
						p.getIndices().put(CSCLIndices.RELEVANCE_TOP10_TOPICS,
								p.getIndices().get(CSCLIndices.RELEVANCE_TOP10_TOPICS) + relevance);
					}
				}
				if (noSelectedTopics == 10)
					break;
			}
		}

		// count nouns and verbs per participant
		for (Participant p : c.getParticipants()) {
			for (Entry<Word, Integer> entry : p.getInterventions().getWordOccurences().entrySet()) {
				if (entry.getKey().getPOS() != null) {
					if (entry.getKey().getPOS().startsWith("N"))
						p.getIndices().put(CSCLIndices.NO_NOUNS,
								p.getIndices().get(CSCLIndices.RELEVANCE_TOP10_TOPICS) + entry.getValue());
					if (entry.getKey().getPOS().startsWith("V"))
						p.getIndices().put(CSCLIndices.NO_VERBS,
								p.getIndices().get(CSCLIndices.RELEVANCE_TOP10_TOPICS) + entry.getValue());
				}
			}
		}
	}

	public static void performSNA(Conversation c) {
		List<Participant> lsPart = getParticipantList(c);
		performSNA(lsPart, c.getParticipantContributions(), true, null);
	}

	public static void performSNA(List<Participant> participants, double[][] participantContributions,
			boolean isAnonymized, String exportPath) {

		for (int index1 = 0; index1 < participants.size(); index1++) {
			for (int index2 = 0; index2 < participants.size(); index2++) {
				participants.get(index1).getIndices().put(CSCLIndices.OUTDEGREE,
						participants.get(index1).getIndices().get(CSCLIndices.OUTDEGREE)
								+ participantContributions[index1][index2]);
				participants.get(index2).getIndices().put(CSCLIndices.INDEGREE,
						participants.get(index2).getIndices().get(CSCLIndices.INDEGREE)
								+ participantContributions[index1][index2]);
			}
		}

		// determine for each participant betweenness, closeness and
		// eccentricity scores
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();

		// get models
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
		DirectedGraph graph = graphModel.getDirectedGraph();

		ParticipantEvaluation.buildParticipantGraph("Member", graph, graphModel, participants, participantContributions,
				true, isAnonymized);

		// Run YifanHuLayout for 100 passes
		YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
		layout.setGraphModel(graphModel);
		layout.resetPropertiesValues();
		layout.setOptimalDistance(100f);

		layout.initAlgo();
		for (int i = 0; i < 100 && layout.canAlgo(); i++) {
			layout.goAlgo();
		}
		layout.endAlgo();

		GraphDistance distance = new GraphDistance();
		distance.setDirected(true);
		distance.execute(graphModel);

		// Determine various metrics
		Map<String, Participant> mappings = new TreeMap<String, Participant>();
		for (int index = 0; index < participants.size(); index++) {
			if (isAnonymized)
				mappings.put("Member " + index, participants.get(index));
			else
				mappings.put(participants.get(index).getName(), participants.get(index));
		}

		Column betweeennessColumn = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
		Column closenessColumn = graphModel.getNodeTable().getColumn(GraphDistance.CLOSENESS);
		Column eccentricityColumn = graphModel.getNodeTable().getColumn(GraphDistance.ECCENTRICITY);
		for (Node n : graph.getNodes()) {
			Participant p = mappings.get(n.getLabel());
			p.getIndices().put(CSCLIndices.BETWEENNESS,
					p.getIndices().get(CSCLIndices.BETWEENNESS) + (Double) n.getAttribute(betweeennessColumn));
			p.getIndices().put(CSCLIndices.CLOSENESS,
					p.getIndices().get(CSCLIndices.CLOSENESS) + (Double) n.getAttribute(closenessColumn));
			p.getIndices().put(CSCLIndices.ECCENTRICITY,
					p.getIndices().get(CSCLIndices.ECCENTRICITY) + (Double) n.getAttribute(eccentricityColumn));
		}

		if (exportPath != null) {
			AppearanceController appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
			AppearanceModel appearanceModel = appearanceController.getModel();

			// Rank size by centrality
			Column centralityColumn = graphModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
			Function centralityRanking = appearanceModel.getNodeFunction(graph, centralityColumn,
					RankingNodeSizeTransformer.class);
			RankingNodeSizeTransformer centralityTransformer = (RankingNodeSizeTransformer) centralityRanking
					.getTransformer();
			centralityTransformer.setMinSize(5);
			centralityTransformer.setMaxSize(40);
			appearanceController.transform(centralityRanking);

			// Rank label size - set a multiplier size
			Function centralityRanking2 = appearanceModel.getNodeFunction(graph, centralityColumn,
					RankingLabelSizeTransformer.class);
			RankingLabelSizeTransformer labelSizeTransformer = (RankingLabelSizeTransformer) centralityRanking2
					.getTransformer();
			labelSizeTransformer.setMinSize(1);
			labelSizeTransformer.setMaxSize(5);
			appearanceController.transform(centralityRanking2);

			// Preview configuration
			PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
			PreviewModel previewModel = previewController.getModel();
			previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
			previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_COLOR,
					new DependantOriginalColor(Color.BLACK));
			previewModel.getProperties().putValue(PreviewProperty.EDGE_CURVED, Boolean.TRUE);
			previewModel.getProperties().putValue(PreviewProperty.EDGE_RADIUS, 10f);
			previewModel.getProperties().putValue(PreviewProperty.SHOW_EDGE_LABELS, Boolean.TRUE);
			previewModel.getProperties().putValue(PreviewProperty.NODE_LABEL_PROPORTIONAL_SIZE, Boolean.FALSE);

			// Export
			logger.info("Exporting pdf: " + exportPath);
			ExportController ec = Lookup.getDefault().lookup(ExportController.class);
			try {
				ec.exportFile(new File(exportPath));
			} catch (IOException ex) {
				ex.printStackTrace();
				return;
			}
		}
	}
}
