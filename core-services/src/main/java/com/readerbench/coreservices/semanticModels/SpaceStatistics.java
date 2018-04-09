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
import com.readerbench.datasourceprovider.pojo.Lang;
import org.gephi.graph.api.*;
import org.gephi.project.api.ProjectController;
import org.gephi.statistics.plugin.*;
import org.openide.util.Lookup;
import com.readerbench.coreservices.commons.Formatting;
import com.readerbench.coreservices.semanticModels.LDA.LDA;
import com.readerbench.coreservices.semanticModels.utils.WordPairSimilarity;
import com.readerbench.coreservices.semanticModels.utils.WordSimilarityContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class SpaceStatistics {

    private static final double MINIMUM_IMPOSED_THRESHOLD = 0.3d;

    private static final Logger LOGGER = LoggerFactory.getLogger(SpaceStatistics.class);

    private final ISemanticModel semModel;
    private final int noWords;
    private List<WordPairSimilarity> relevantWordPairList;
    private WordSimilarityContainer wordSimilarityContainer;
    private final String indexPath;

    public SpaceStatistics(ISemanticModel semModel) {
        LOGGER.info("Loading " + semModel.getPath() + "...");
        this.semModel = semModel;
        this.noWords = semModel.getWordSet().size();
        this.indexPath = semModel.getPath() + File.separator + "index.ser";
    }

    public void buildWordDistances() {
        LOGGER.info("No. words in semantic model dictionary:\t" + noWords);
        double sim;
        double s00 = 0, s10 = 0, s20 = 0;
        double s01 = 0, s11 = 0, s21 = 0;
        List<WordPairSimilarity> allWordSimilarityPairList = new ArrayList<>();
        for (Map.Entry<Word, double[]> e1 : semModel.getWordRepresentations().entrySet()) {
            for (Map.Entry<Word, double[]> e2 : semModel.getWordRepresentations().entrySet()) {
                if (e1.getKey().getLemma().compareTo(e2.getKey().getLemma()) > 0) {
                    sim = semModel.getSimilarity(e1.getValue(), e2.getValue());
                    s00++;
                    s10 += sim;
                    s20 += Math.pow(sim, 2);
                    if (sim >= MINIMUM_IMPOSED_THRESHOLD) {
                        WordPairSimilarity pairSimilarity = new WordPairSimilarity(e1.getKey().getLemma(), e2.getKey().getLemma(), sim);
                        allWordSimilarityPairList.add(pairSimilarity);
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
        LOGGER.info("No. potential word associations:\t" + Formatting.formatNumber(s00));
        LOGGER.info("Average similarity for all word associations:\t" + Formatting.formatNumber(avg));
        LOGGER.info("Stdev similarity for all word associations:\t" + Formatting.formatNumber(stdev));

        avg = -1;
        stdev = -1;
        if (s01 != 0) {
            avg = s11 / s01;
            stdev = Math.sqrt(s01 * s21 - Math.pow(s11, 2)) / s01;
        }
        LOGGER.info("No. word associations (above minimum threshold):\t" + Formatting.formatNumber(s01));
        LOGGER.info("Average similarity for all word associations (above minimum threshold):\t" + Formatting.formatNumber(avg));
        LOGGER.info("Stdev similarity for all word associations (above minimum threshold):\t" + Formatting.formatNumber(stdev));

        // add only significant edges
        double threshold = avg - stdev;
        double s02 = 0, s12 = 0, s22 = 0;

        this.relevantWordPairList = new ArrayList<>();
        this.wordSimilarityContainer = new WordSimilarityContainer();

        for (WordPairSimilarity pair : allWordSimilarityPairList) {
            if (pair.getSimilarity() >= threshold) {
                relevantWordPairList.add(pair);
                wordSimilarityContainer.indexDistance(pair.getWord1(), pair.getWord2(), pair.getSimilarity());
                s02++;
                s12 += pair.getSimilarity();
                s22 += Math.pow(pair.getSimilarity(), 2);
            }
        }
        avg = -1;
        stdev = -1;
        if (s02 != 0) {
            avg = s12 / s02;
            stdev = Math.sqrt(s02 * s22 - Math.pow(s12, 2)) / s02;
        }
        LOGGER.info("No significant word associations (above avg-stdev):\t" + Formatting.formatNumber(s02));
        LOGGER.info("Average similarity for significant word associations (above avg-stdev):\t" + Formatting.formatNumber(avg));
        LOGGER.info("Stdev similarity for significant word associations (above avg-stdev):\t" + Formatting.formatNumber(stdev));

        this.saveSerializedRelevantSimilarities();
    }

    private void saveSerializedRelevantSimilarities() {
        try {
            File f = new File(this.indexPath.substring(0, this.indexPath.lastIndexOf("/")));
            f.mkdirs();

            FileOutputStream fout = new FileOutputStream(this.indexPath);
            try (ObjectOutputStream oos = new ObjectOutputStream(fout)) {
                oos.writeObject(this.wordSimilarityContainer);
            }
            LOGGER.info("Written serialized relevant similarities to " + this.indexPath);
        } catch (Exception ex) {
            LOGGER.error("Error serializing relevant similarities to " + this.indexPath + " - " + ex.getMessage());
            LOGGER.error(ex.getMessage());
        }
    }

    private boolean loadRelevantSimilarities() {
        try {
            InputStream file = new FileInputStream(this.indexPath);
            InputStream buffer = new BufferedInputStream(file);
            try (ObjectInput input = new ObjectInputStream(buffer)) {
                this.wordSimilarityContainer = (WordSimilarityContainer) input.readObject();
                LOGGER.info("Loaded relevant similarities ...");
            }
            return true;
        } catch (IOException | ClassNotFoundException ex) {
            LOGGER.error("Failed to load the serialized relevant similarities from " + this.indexPath + " - " + ex.getMessage());
            return false;
        }
    }

    public void computeGraphStatistics() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();

        // get models
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        DirectedGraph graph = graphModel.getDirectedGraph();
        Map<String, Node> associations = new TreeMap<>();

        // build all nodes
        semModel.getWordRepresentations().keySet().stream().map((w) -> {
            Node wordNode = graphModel.factory().newNode(w.getLemma());
            wordNode.setLabel(w.getLemma());
            associations.put(w.getLemma(), wordNode);
            return wordNode;
        }).forEach((wordNode) -> {
            graph.addNode(wordNode);
        });

        this.relevantWordPairList.stream().map((c) -> {
            Edge e = graphModel.factory().newEdge(associations.get(c.getWord1()), associations.get(c.getWord2()));
            e.setWeight((float) (c.getSimilarity()));
            return e;
        }).forEach((e) -> {
            graph.addEdge(e);
        });

        GraphDensity density = new GraphDensity();
        density.setDirected(false);
        density.execute(graphModel);
        LOGGER.info("Semantic model density:\t" + Formatting.formatNumber(density.getDensity()));

        Modularity modularity = new Modularity();
        modularity.execute(graphModel);
        LOGGER.info("Semantic model average modularity:\t" + Formatting.formatNumber(modularity.getModularity()));

        ConnectedComponents connectedComponents = new ConnectedComponents();
        connectedComponents.setDirected(false);
        connectedComponents.execute(graphModel);
        LOGGER.info("No connected components within semantic model:\t" + Formatting.formatNumber(connectedComponents.getConnectedComponentsCount()));

        ClusteringCoefficient clusteringCoefficient = new ClusteringCoefficient();
        clusteringCoefficient.setDirected(false);
        clusteringCoefficient.execute(graphModel);
        LOGGER.info("Semantic model average clustering coefficient:\t" + Formatting.formatNumber(clusteringCoefficient.getAverageClusteringCoefficient()));

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
            LOGGER.info("Average word betweenness:\t" + Formatting.formatNumber(avgBetweenness / graph.getNodeCount()));
            LOGGER.info("Average word closeness:\t" + Formatting.formatNumber(avgCloseness / graph.getNodeCount()));
            LOGGER.info("Average word eccentricity:\t" + Formatting.formatNumber(avgEccentricity / graph.getNodeCount()));
        }

        LOGGER.info("Semantic model diameter:\t" + Formatting.formatNumber(distance.getDiameter()));

        LOGGER.info("Semantic model path length:\t" + Formatting.formatNumber(distance.getPathLength()));
    }

    public WordSimilarityContainer getWordSimilarityContainer() {
        if (!this.loadRelevantSimilarities()) {
            this.buildWordDistances();
        }
        return this.wordSimilarityContainer;
    }

    public ISemanticModel getSemModel() {
        return semModel;
    }

    /**
     * Compares all pairs of concepts from the baseline to all subsequent
     * corpora
     *
     * The first space is the baseline
     *
     * @param pathToOutput
     * @param corpora
     */
    public static void compareSpaces(String pathToOutput, List<SpaceStatistics> corpora) {
        corpora.get(0).buildWordDistances();
        LOGGER.info("Writing comparisons based on baseline corpus ...");
        File output = new File(pathToOutput);

        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"),
                32768)) {
            out.write("Word 1, Word 2");
            for (SpaceStatistics space : corpora) {
                out.write("," + space.getSemModel().getPath());
            }

            for (WordPairSimilarity c : corpora.get(0).relevantWordPairList) {
                if (c.getSimilarity() > 0) {
                    String outputString = "\n" + c.getWord1() + "," + c.getWord2();
                    boolean viableEntry = true;
                    for (SpaceStatistics space : corpora) {
                        double similarity = space.getSemModel().getSimilarity(
                                Word.getWordFromConcept(c.getWord1(), space.getSemModel().getLanguage()),
                                Word.getWordFromConcept(c.getWord2(), space.getSemModel().getLanguage()));
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
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    /**
     * Determine word linkage power corpora
     *
     * The first space is the baseline
     *
     */
    private enum SNAIndices {
        DEGREE, SIM_DEGREE, BETWEENNESS, CLOSENESS, ECCENTRICITY
    }

    public static void determineWLP(String pathToOutput, SpaceStatistics space, LDA referenceSpace) {
        LOGGER.info("Writing word linkage power statistics based on baseline corpus ...");
        File output = new File(pathToOutput);

        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), "UTF-8"),
                32768)) {
            out.write("Word");
            for (SNAIndices index : SNAIndices.values()) {
                out.write("," + index.name() + "(" + space.getSemModel().getPath() + ")");
            }
            for (SNAIndices index : SNAIndices.values()) {
                out.write(",NORM_" + index.name() + "(" + space.getSemModel().getPath() + ")");
            }

            Map<String, EnumMap<SNAIndices, Double>> wlps = new TreeMap<>();

            space.getSemModel().getWordSet().stream().forEach((w) -> {
                EnumMap<SNAIndices, Double> scores = new EnumMap<>(SNAIndices.class
                );
                scores.put(SNAIndices.SIM_DEGREE, 0d);
                wlps.put(w.getLemma(), scores);
            });

            ProjectController pc;
            GraphModel graphModel;
            DirectedGraph graph;
            Map<String, Node> associations;
            GraphDistance distance;

            LOGGER.info("Processing SNA indices for " + space.getSemModel().getPath() + "...");
            space.buildWordDistances();
            pc = Lookup.getDefault().lookup(ProjectController.class);
            pc.newProject();

            // get models
            graphModel
                    = Lookup.getDefault().lookup(GraphController.class
                    ).getGraphModel();
            graph = graphModel.getDirectedGraph();
            associations = new TreeMap<>();

            // build all nodes
            space.getSemModel().getWordRepresentations().keySet().stream().map((w) -> {
                Node wordNode = graphModel.factory().newNode(w.getLemma());
                wordNode.setLabel(w.getLemma());
                associations.put(w.getLemma(), wordNode);
                return wordNode;
            }).forEach((wordNode) -> {
                graph.addNode(wordNode);
            });

            space.relevantWordPairList.stream().map((c) -> {
                Edge e = graphModel.factory().newEdge(associations.get(c.getWord1()), associations.get(c.getWord2()), 0,
                        c.getSimilarity(), false);
                graph.addEdge(e);
                wlps.get(c.getWord1()).put(SNAIndices.SIM_DEGREE,
                        wlps.get(c.getWord1()).get(SNAIndices.SIM_DEGREE) + c.getSimilarity());
                return c;
            }).forEach((c) -> {
                wlps.get(c.getWord2()).put(SNAIndices.SIM_DEGREE,
                        wlps.get(c.getWord2()).get(SNAIndices.SIM_DEGREE) + c.getSimilarity());
            });

            LOGGER.info("Computing SNA indices...");
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

            LOGGER.info("Writing final results...");
            // print results
            for (Word w : referenceSpace.getWordSet()) {
                out.write("\n" + w.getLemma());
                if (wlps.containsKey(w.getLemma())) {
                    for (SNAIndices index : SNAIndices.values()) {
                        out.write("," + wlps.get(w.getLemma()).get(index));
                    }
                    for (SNAIndices index : SNAIndices.values()) {
                        out.write("," + wlps.get(w.getLemma()).get(index) / space.getSemModel().getWordSet().size());
                    }
                } else {
                    out.write(",,,,");
                }
            }
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }

    }

    public static void main(String[] args) {
        

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
        // corpora.add(new SpaceStatistics(LDA.loadLDA("resources/in/HDP/grade" + i, Lang.eng)));
        // }
        // compareSpaces("resources/in/HDP/comparison HDP 12-.csv", corpora);
        int gradeLevel = 0;
        LDA matureSpace = LDA.loadLDA("resources/in/HDP/grade12", Lang.en);
        determineWLP("resources/in/HDP/WLP HDP " + gradeLevel + ".csv",
                new SpaceStatistics(LDA.loadLDA("resources/in/HDP/grade" + gradeLevel, Lang.en)), matureSpace);
    }

}
