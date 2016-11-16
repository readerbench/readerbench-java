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
import java.util.logging.Level;
import java.util.logging.Logger;
import services.commons.Formatting;

public class ParticipantEvaluation {

    static final Logger LOGGER = Logger.getLogger("");
    private static final String GENERIC_NAME = "Member";

    public static void buildParticipantGraph(DirectedGraph graph, GraphModel graphModel, List<Participant> participants,
            double[][] participantContributions, boolean displayEdgeLabels, boolean needsAnonymization) {

        Node[] participantNodes = new Node[participants.size()];

        // TODO change list for specific elements to ignore
        // Set<String> namesToIgnore = new TreeSet<String>(Arrays.asList(new
        // String[] { "2093911", "1516180", "90343" }));
        Color colorParticipant = Color.DARK_GRAY;

        // build all nodes
        for (int i = 0; i < participants.size(); i++) {
            // if (!namesToIgnore.contains(participants.get(i).getName())) {
            // build block element
            Node participant = null;
            if (needsAnonymization) {
                participant = graphModel.factory().newNode(GENERIC_NAME + " " + i);
                participant.setLabel(GENERIC_NAME + " " + i);
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
                if (participantContributions[i][j] > 0 // && !namesToIgnore.contains(participants.get(i).getName())
                        // && !namesToIgnore.contains(participants.get(j).getName())
                        ) {
                    Edge e = graphModel.factory().newEdge(participantNodes[i], participantNodes[j], 0,
                            participantContributions[i][j], true);
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
            List<Participant> lsPart = c.getParticipants();
            // determine strength of links
            for (int i = 0; i < c.getBlocks().size(); i++) {
                if (c.getBlocks().get(i) != null) {
                    Participant p1 = ((Utterance) c.getBlocks().get(i)).getParticipant();
                    int index1 = lsPart.indexOf(p1);
                    // c.getParticipantContributions()[index1][index1] += c
                    // .getBlocks().get(i).getOverallScore();
                    for (int j = 0; j < i; j++) {
                        if (c.getPrunnedBlockDistances()[i][j] != null) {
                            Participant p2 = ((Utterance) c.getBlocks().get(j)).getParticipant();
                            int index2 = lsPart.indexOf(p2);
                            c.getParticipantContributions()[index1][index2] += c.getBlocks().get(i).getIndividualScore()
                                    * c.getPrunnedBlockDistances()[i][j].getCohesion();
                        }
                    }
                }
            }
        }
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
        // count nouns and verbs per participant
        for (Participant p : c.getParticipants()) {
            for (Entry<Word, Integer> entry : p.getContributions().getWordOccurences().entrySet()) {
                if (entry.getKey().getPOS() != null) {
                    if (entry.getKey().getPOS().startsWith("N")) {
                        p.getIndices().put(CSCLIndices.NO_NOUNS,
                                p.getIndices().get(CSCLIndices.NO_NOUNS) + entry.getValue());
                    }
                    if (entry.getKey().getPOS().startsWith("V")) {
                        p.getIndices().put(CSCLIndices.NO_VERBS,
                                p.getIndices().get(CSCLIndices.NO_VERBS) + entry.getValue());
                    }
                }
            }
        }
    }

    public static void performSNA(Conversation c) {
        List<Participant> lsPart = c.getParticipants();
        performSNA(lsPart, c.getParticipantContributions(), true, null);
    }

    public static void performSNA(List<Participant> participants, double[][] participantContributions, boolean needsAnonymization, String exportPath) {

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

        ParticipantEvaluation.buildParticipantGraph(graph, graphModel, participants, participantContributions, true,
                needsAnonymization);

        GraphDistance distance = new GraphDistance();
        distance.setDirected(true);
        distance.execute(graphModel);

        // Determine various metrics
        Map<String, Participant> mappings = new TreeMap<>();
        for (int index = 0; index < participants.size(); index++) {
            if (needsAnonymization) {
                mappings.put(GENERIC_NAME + " " + index, participants.get(index));
            } else {
                mappings.put(participants.get(index).getName(), participants.get(index));
            }
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
            // Run YifanHuLayout for 100 passes
            YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
            layout.setGraphModel(graphModel);
            layout.resetPropertiesValues();
            // determine max weight
            float max = 0;
            for (Edge e : graph.getEdges()) {
                max = (float) Math.max(max, e.getWeight());
            }
            for (Edge e : graph.getEdges()) {
                e.setWeight(e.getWeight() / max);
            }
            layout.setOptimalDistance(max * 10);
            // layout.setOptimalDistance(100f);

            layout.initAlgo();
            for (int i = 0; i < 100 && layout.canAlgo(); i++) {
                layout.goAlgo();
            }
            layout.endAlgo();

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
            LOGGER.log(Level.INFO, "Exporting pdf: {0}", exportPath);
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
