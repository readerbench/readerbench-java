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
package com.readerbench.coreservices.cscl;

import com.readerbench.datasourceprovider.data.cscl.CSCLCriteria;
import com.readerbench.datasourceprovider.data.cscl.Utterance;
import com.readerbench.datasourceprovider.data.cscl.Conversation;
import com.readerbench.datasourceprovider.data.cscl.CSCLIndices;
import com.readerbench.datasourceprovider.data.cscl.Participant;
import com.readerbench.coreservices.rhythm.RhythmTool;
import com.readerbench.datasourceprovider.data.Block;
import com.readerbench.datasourceprovider.data.Sentence;
import com.readerbench.datasourceprovider.data.Word;
import org.gephi.graph.api.*;
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
import com.readerbench.datasourceprovider.data.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ParticipantEvaluation {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParticipantEvaluation.class);

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
            Node participant;
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
            // LOGGER.info("Ignoring " + participants.get(i).getName());
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
            // determine strength of links
            for (int i = 0; i < c.getBlocks().size(); i++) {
                if (c.getBlocks().get(i) != null) {
                    Participant p1 = ((Utterance) c.getBlocks().get(i)).getParticipant();
                    int index1 = c.getParticipants().indexOf(p1);
                    c.getParticipantContributions()[index1][index1] += c.getBlocks().get(i).getScore();
                    for (int j = 0; j < i; j++) {
                        if (c.getPrunnedBlockDistances()[i][j] != null) {
                            Participant p2 = ((Utterance) c.getBlocks().get(j)).getParticipant();
                            int index2 = c.getParticipants().indexOf(p2);
                            c.getParticipantContributions()[index1][index2] += c.getBlocks().get(i).getScore() * c.getPrunnedBlockDistances()[i][j].getCohesion();
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
                    u.getParticipant().getIndices().put(CSCLIndices.SCORE,
                            u.getParticipant().getIndices().get(CSCLIndices.SCORE) + b.getScore());
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

    public static void performSNA(List<Participant> participants, double[][] participantContributions,
            boolean needsAnonymization, String exportPath) {
        for (int index1 = 0; index1 < participants.size(); index1++) {
            for (int index2 = 0; index2 < participants.size(); index2++) {
                if (index1 != index2) {
                    participants.get(index1).getIndices().put(CSCLIndices.OUTDEGREE,
                            participants.get(index1).getIndices().get(CSCLIndices.OUTDEGREE)
                            + participantContributions[index1][index2]);
                    participants.get(index2).getIndices().put(CSCLIndices.INDEGREE,
                            participants.get(index2).getIndices().get(CSCLIndices.INDEGREE)
                            + participantContributions[index1][index2]);
                } else {
                    participants.get(index1).getIndices().put(CSCLIndices.OUTDEGREE,
                            participants.get(index1).getIndices().get(CSCLIndices.OUTDEGREE)
                            + participantContributions[index1][index1]);
                }
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
            LOGGER.info("Exporting pdf: {}", exportPath);
            ExportController ec = Lookup.getDefault().lookup(ExportController.class);
            try {
                ec.exportFile(new File(exportPath));
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
                return;
            }
        }
    }

    public static void extractRhythmicIndex(Conversation c) {
        Map<Participant, List<Integer>> rhythmicIndPerPart = new TreeMap<>();

        if (c.getParticipants().size() > 0) {
            for (Block b : c.getBlocks()) {
                if (b != null) {
                    Utterance u = (Utterance) b;
                    Participant p = u.getParticipant();
                    if (!rhythmicIndPerPart.containsKey(p)) {
                        rhythmicIndPerPart.put(p, new ArrayList<>());
                    }
                    for (Sentence s : u.getSentences()) {
                        List<Word> unit = s.getAllWords();
                        int ind = RhythmTool.calcRhythmicIndexSM(unit);
                        if (ind != RhythmTool.UNDEFINED) {
                            rhythmicIndPerPart.get(p).add(ind);
                        }
                    }
                }
            }
        }

        for (Entry<Participant, List<Integer>> entry : rhythmicIndPerPart.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            int maxIndex = Collections.max(entry.getValue());
            entry.getKey().getIndices().put(CSCLIndices.RHYTHMIC_INDEX, 1.0 * maxIndex);
            entry.getKey().getIndices().put(CSCLIndices.FREQ_MAX_INDEX, 1.0 * Collections.frequency(entry.getValue(),
                    maxIndex) / entry.getValue().size());
        }
    }

    public static void extractRhythmicCoefficient(Conversation c) {
        Map<Participant, Map<Integer, Integer>> cntSyllables = new TreeMap<>();
        Map<Participant, Integer> deviations = new TreeMap<>();

        if (c.getParticipants().size() > 0) {
            for (Block b : c.getBlocks()) {
                if (b != null) {
                    Utterance u = (Utterance) b;
                    Participant p = u.getParticipant();
                    if (!cntSyllables.containsKey(p)) {
                        cntSyllables.put(p, new TreeMap());
                    }
                    for (Sentence s : u.getSentences()) {
                        List<Word> unit = s.getAllWords();
                        List<Integer> repr = RhythmTool.getNumericalRepresentation(unit);
                        if (repr.isEmpty()) {
                            continue;
                        }
                        int NT = (repr.get(0) == 0) ? repr.size() - 1 : repr.size();
                        int NA = repr.stream().mapToInt(Integer::intValue).sum();
                        Map<Integer, Integer> nrSylls = cntSyllables.get(p);
                        for (Integer nr : repr) {
                            if (nr == 0) {
                                continue;
                            }
                            nrSylls.put(nr,
                                    nrSylls.containsKey(nr) ? nrSylls.get(nr) + 1 : 1);
                        }
                        int devs = RhythmTool.calcDeviations(repr);
                        deviations.put(p,
                                deviations.containsKey(p) ? deviations.get(p) + 1 : devs);
                    }
                }
            }
        }

        for (Participant p : c.getParticipants()) {
            Map<Integer, Integer> nrSylls = cntSyllables.get(p);
            int totalNumber = nrSylls.values().stream().reduce(0, Integer::sum);
            for (Entry<Integer, Integer> entry : nrSylls.entrySet()) {
                double syllFreq = 1.0 * entry.getValue() / totalNumber;
            }
            int dominantInd = RhythmTool.getDominantIndex(nrSylls.values().stream()
                    .collect(Collectors.toList()));
            int keyOfMaxVal = nrSylls.keySet().stream()
                    .collect(Collectors.toList()).get(dominantInd);
            int sum = nrSylls.get(keyOfMaxVal);
            sum += (nrSylls.containsKey(keyOfMaxVal - 1)) ? nrSylls.get(keyOfMaxVal - 1) : 0;
            sum += (nrSylls.containsKey(keyOfMaxVal + 1)) ? nrSylls.get(keyOfMaxVal + 1) : 0;
            double coeff = 1.0 * (deviations.get(p) + totalNumber - sum) / totalNumber;
            p.getIndices().put(CSCLIndices.RHYTHMIC_COEFFICIENT, coeff);
        }
    }

    public static void computeEntropyForRegularityMeasure(Conversation c) {
        Date chatStartTime = null;
        Date chatEndTime = null;
        long chatTime = 0;
        long frameTime = 5 * 60;    // seconds
        Map<Participant, List<Date>> timestamps = new TreeMap<>();

        for (Participant p : c.getParticipants()) {
            List<Date> dates = new ArrayList<>();
            for (Block b : p.getContributions().getBlocks()) {
                Date d = ((Utterance) b).getTime();
                dates.add(d);
                if (chatStartTime == null && chatEndTime == null) {
                    chatStartTime = chatEndTime = d;
                } else {
                    if (d.before(chatStartTime)) {
                        chatStartTime = d;
                    }
                    if (d.after(chatEndTime)) {
                        chatEndTime = d;
                    }
                }
            }
            timestamps.put(p, dates);
        }
        chatTime = (chatEndTime.getTime() - chatStartTime.getTime()) / 1000;

        Map<Participant, List<Double>> noInterventions = new TreeMap<>();
        int index, size;
        long diff;

        size = (int) Math.ceil((double) chatTime / frameTime);
        for (Entry<Participant, List<Date>> entry : timestamps.entrySet()) {
            List<Double> arr = new ArrayList<>(Collections.nCopies(size, 0.0));
            for (Date d : entry.getValue()) {
                diff = (d.getTime() - chatStartTime.getTime()) / 1000;
                index = (int) Math.floor((double) diff / frameTime);
                arr.set(index, arr.get(index) + 1);
            }
            noInterventions.put(entry.getKey(), arr);
        }

        for (Entry<Participant, List<Double>> entry : noInterventions.entrySet()) {
            double value = CSCLCriteria.getValue(CSCLCriteria.PEAK_CHAT_FRAME,
                    entry.getValue().stream().mapToDouble(d -> d).toArray());
            entry.getKey().getIndices().put(CSCLIndices.PERSONAL_REGULARITY_ENTROPY, value);
//            System.out.println(entry.getKey().getName() + " " + value);
        }

    }
}
