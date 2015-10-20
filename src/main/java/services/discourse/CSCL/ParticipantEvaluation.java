package services.discourse.CSCL;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

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
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;

import DAO.Block;
import DAO.Word;
import DAO.cscl.Conversation;
import DAO.cscl.Participant;
import DAO.cscl.Utterance;
import DAO.discourse.SemanticCohesion;
import DAO.discourse.Topic;

public class ParticipantEvaluation {
	static Logger logger = Logger.getLogger(ParticipantEvaluation.class);

	public static void buildParticipantGraph(String genericName, DirectedGraph graph, GraphModel graphModel,
			List<Participant> participants, double[][] participantContributions, boolean displayEdgeLabels,
			boolean isAnonymized) {
		DecimalFormat formatter = new DecimalFormat("#.##");

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
				participant.getNodeData().setLabel(genericName + " " + i);
			} else {
				participant = graphModel.factory().newNode(participants.get(i).getName());
				participant.getNodeData().setLabel(participants.get(i).getName());
			}
			participant.getNodeData().setColor((float) (colorParticipant.getRed()) / 256,
					(float) (colorParticipant.getGreen()) / 256, (float) (colorParticipant.getBlue()) / 256);
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
					graph.addEdge(participantNodes[i], participantNodes[j]);
					Edge e = graph.getEdge(participantNodes[i], participantNodes[j]);
					e.setWeight((float) (participantContributions[i][j] / maxVal));
					if (displayEdgeLabels) {
						e.getEdgeData().setLabel(formatter.format(participantContributions[i][j]));
					} else {
						e.getEdgeData().setLabel("");
					}
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
					u.getParticipant().setOverallScore(u.getParticipant().getOverallScore() + b.getOverallScore());
					u.getParticipant().setPersonalKB(u.getParticipant().getPersonalKB() + u.getPersonalKB());
					u.getParticipant().setSocialKB(u.getParticipant().getSocialKB() + u.getSocialKB());
					u.getParticipant().setNoContributions(u.getParticipant().getNoContributions() + 1);
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
						p.setRelevanceTop10Topics(p.getRelevanceTop10Topics() + relevance);
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
						p.setNoNouns(p.getNoNouns() + entry.getValue());
					if (entry.getKey().getPOS().startsWith("V"))
						p.setNoVerbs(p.getNoVerbs() + entry.getValue());
				}
			}
		}
	}

	public static void performSNA(Conversation c) {
		List<Participant> lsPart = getParticipantList(c);
		performSNA(lsPart, c.getParticipantContributions(), true);
	}

	public static void performSNA(List<Participant> participants, double[][] participantContributions,
			boolean isAnonymized) {

		for (int index1 = 0; index1 < participants.size(); index1++) {
			for (int index2 = 0; index2 < participants.size(); index2++) {
				participants.get(index1).setOutdegree(
						participants.get(index1).getOutdegree() + participantContributions[index1][index2]);
				participants.get(index2)
						.setIndegree(participants.get(index2).getIndegree() + participantContributions[index1][index2]);
			}
		}

		// determine for each participant betweenness, closeness and
		// eccentricity scores
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();

		// get models
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
		DirectedGraph graph = graphModel.getDirectedGraph();

		ParticipantEvaluation.buildParticipantGraph("Member", graph, graphModel, participants, participantContributions,
				true, isAnonymized);

		GraphDistance distance = new GraphDistance();
		distance.setDirected(true);
		distance.execute(graphModel, attributeModel);

		// Determine various metrics
		AttributeColumn betweennessColumn = attributeModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);

		AttributeColumn closenessColumn = attributeModel.getNodeTable().getColumn(GraphDistance.CLOSENESS);

		AttributeColumn eccentricityColumn = attributeModel.getNodeTable().getColumn(GraphDistance.ECCENTRICITY);

		Map<String, Participant> mappings = new TreeMap<String, Participant>();

		for (int index = 0; index < participants.size(); index++) {
			if (isAnonymized)
				mappings.put("Member " + index, participants.get(index));
			else
				mappings.put(participants.get(index).getName(), participants.get(index));
		}

		for (Node n : graph.getNodes()) {
			Participant p = mappings.get(n.getNodeData().getLabel());
			p.setBetweenness((Double) n.getNodeData().getAttributes().getValue(betweennessColumn.getIndex()));
			p.setCloseness((Double) n.getNodeData().getAttributes().getValue(closenessColumn.getIndex()));
			p.setEccentricity((Double) n.getNodeData().getAttributes().getValue(eccentricityColumn.getIndex()));
		}
	}
}
