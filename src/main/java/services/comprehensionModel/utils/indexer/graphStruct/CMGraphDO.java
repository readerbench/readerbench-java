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
package services.comprehensionModel.utils.indexer.graphStruct;

import data.Word;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.TreeMap;
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
import org.openide.util.Lookup;
import services.commons.VectorAlgebra;
import services.semanticModels.ISemanticModel;
import services.semanticModels.WordNet.OntologySupport;

public class CMGraphDO {

    private List<CMNodeDO> nodeList;
    private List<CMEdgeDO> edgeList;

    public CMGraphDO() {
        this.nodeList = new ArrayList<>();
        this.edgeList = new ArrayList<>();
    }

    public static boolean nodeListContainsNode(List<CMNodeDO> nodeList, CMNodeDO otherNode) {
        return nodeList.stream().anyMatch((node) -> (node.equals(otherNode)));
    }

    public static boolean edgeListContainsEdge(List<CMEdgeDO> edgeList, CMEdgeDO otherEdge) {
        return edgeList.stream().anyMatch((edge) -> (edge.equals(otherEdge)));
    }

    public boolean containsNode(CMNodeDO otherNode) {
        return nodeListContainsNode(this.nodeList, otherNode);
    }
    public CMNodeDO getNode(CMNodeDO otherNode) {
        Optional<CMNodeDO> retrievedNode = nodeList.stream().filter(((node) -> (node.equals(otherNode)))).findFirst();
        try {
            return retrievedNode.get();
        } catch (NoSuchElementException e) {
            return null;
        }
    }
    public void removeNodeLinks(CMNodeDO node) {
        if(!this.containsNode(node)) {
            return;
        }
        CMNodeDO actualNode = this.getNode(node);
        this.edgeList.removeIf(edge -> {
            return edge.getOppositeNode(actualNode) != null;
        });
    }

    public void addNodeIfNotExists(CMNodeDO otherNode) {
        if (!this.containsNode(otherNode)) {
            this.nodeList.add(otherNode);
        }
    }

    private boolean containsEdge(CMEdgeDO otherEdge) {
        return edgeListContainsEdge(this.edgeList, otherEdge);
    }

    public List<CMNodeDO> getNodeList() {
        return nodeList;
    }

    public List<CMEdgeDO> getEdgeList() {
        return edgeList;
    }

    public void setNodeList(List<CMNodeDO> nodeList) {
        this.nodeList = nodeList;
    }

    public void setEdgeList(List<CMEdgeDO> edgeList) {
        this.edgeList = edgeList;
    }
    
    public List<CMEdgeDO> getEdgeList(CMNodeDO node) {
        List<CMEdgeDO> outEdgeList = new ArrayList<>();
        this.edgeList.stream().filter((edge) -> (edge.getNode1().equals(node) || edge.getNode2().equals(node))).forEach((edge) -> {
            outEdgeList.add(edge);
        });
        return outEdgeList;
    }

    public void combineWithLinksFrom(CMGraphDO otherGraph) {
        List<CMNodeDO> thisNodeList = new ArrayList<>(this.nodeList);
        for (CMNodeDO node : thisNodeList) {
            List<CMEdgeDO> otherGraphEdgeList = otherGraph.getEdgeList(node);
            otherGraphEdgeList.stream().filter((otherGraphEdge) -> (!this.containsEdge(otherGraphEdge))).map((otherGraphEdge) -> {
                this.addNodeIfNotExists(otherGraphEdge.getNode1());
                return otherGraphEdge;
            }).map((otherGraphEdge) -> {
                this.addNodeIfNotExists(otherGraphEdge.getNode2());
                return otherGraphEdge;
            }).forEach((otherGraphEdge) -> {
                this.edgeList.add(otherGraphEdge);
            }); // add direct nodes with links
        }
        // add missing links from the second graph
        thisNodeList = new ArrayList<>(this.nodeList);
        for (CMNodeDO node : thisNodeList) {
            List<CMEdgeDO> otherGraphEdgeList = otherGraph.getEdgeList(node);
            otherGraphEdgeList.stream().filter((otherGraphEdge) -> (!this.containsEdge(otherGraphEdge) && this.containsNode(otherGraphEdge.getNode1()))
                    && this.containsNode(otherGraphEdge.getNode2())).forEach((otherGraphEdge) -> {
                this.edgeList.add(otherGraphEdge);
            }); // add direct nodes with links
        }
    }
    
    public void combineWithSyntacticLinksFrom(CMGraphDO syntacticGraph, ISemanticModel semanticModel, int noTopSimilarWords) {
        List<ISemanticModel> models = new ArrayList();
        models.add(semanticModel);
        
        // set of all the potential semantic words
        Map<Word, Double> potentialInferredWords = new TreeMap<>();
        
        // ** STEP 1 ** Add all the links & nodes from the syntactic graph
        for (CMNodeDO node : syntacticGraph.getNodeList()) {
            node.getWord().setSemanticModels(models);
            
            // add the new text based nodes
            if(this.containsNode(node)) {
                CMNodeDO associatedNode = this.getNode(node);
                associatedNode.setNodeType(CMNodeType.TextBased);
            }
            else {
                this.addNodeIfNotExists(node);
            }
            
            // add the related words from the dictionary in the potentialInferredWords set
            TreeMap<Word, Double> similarConcepts = OntologySupport.getSimilarConcepts(node.getWord());
            Iterator<Word> dictionaryWordIterator = similarConcepts.keySet().iterator();
            while(dictionaryWordIterator.hasNext()) {
                Word dictionaryWord = dictionaryWordIterator.next();
                if(dictionaryWord.isNoun() || dictionaryWord.isVerb()) {
                    dictionaryWord.setSemanticModels(models);
                    potentialInferredWords.put(dictionaryWord, 0.0);
                }
            }
        }
        syntacticGraph.edgeList.stream().filter((otherGraphEdge) -> (!edgeListContainsEdge(this.edgeList, otherGraphEdge))).forEach((otherGraphEdge) -> {
            this.edgeList.add(otherGraphEdge);
        });
        
        // ** STEP 2 ** Remove the links for the Inferred Words that currently exist within the graph
        List<CMNodeDO> thisNodeList = new ArrayList<>(this.nodeList);
        for (CMNodeDO node : thisNodeList) {
            if(node.getNodeType() == CMNodeType.Inferred) {
                this.removeNodeLinks(node);
            }
        }
        
        // ** STEP 3 ** Build array with all the distances between the potentialInferredWords and all the graph's nodes
        Iterator<Word> it = potentialInferredWords.keySet().iterator();
        double[] distances = new double[potentialInferredWords.keySet().size() * this.nodeList.size()];
        int noPotentialWord = 0;
        while(it.hasNext()) {
            Word potentialWord = it.next();
            
            double maxDistance = 0.0;
            int n = 0;
            for(CMNodeDO node: this.nodeList) {
                double similarity = semanticModel.getSimilarity(node.getWord(), potentialWord);
                maxDistance = Math.max(maxDistance, similarity);
                distances[this.nodeList.size() * noPotentialWord + n] = similarity;
                n ++;
            }
            potentialInferredWords.put(potentialWord, maxDistance);
            noPotentialWord++;
        }
        
        double avg = VectorAlgebra.avg(distances);
        double stdev = VectorAlgebra.stdev(distances);
        double minDistance = Math.min(0.3, avg);
        
        System.out.println(">> Avg = " + avg + " StDev = " + stdev);
        System.out.println(">> MinDistance = " + minDistance);
        
        // ** STEP 4 ** Add only potential nodes that have at least a semantic distance > minDistance
        it = potentialInferredWords.keySet().iterator();
        while(it.hasNext()) {
            Word potentialWord = it.next();
            double distance = potentialInferredWords.get(potentialWord);
            if(distance >= minDistance) {
                CMNodeDO node = new CMNodeDO(potentialWord, CMNodeType.Inferred);
                this.addNodeIfNotExists(node);
            }
        }
        
        // ** STEP 5 ** add all their semantic links using the semantic model
        thisNodeList = new ArrayList<>(this.nodeList);
        for(int i = 0; i < thisNodeList.size(); i++) {
            for(int j = i + 1; j < thisNodeList.size(); j++) {
                CMNodeDO node1 = this.nodeList.get(i), node2 = this.nodeList.get(j);
                double distance = semanticModel.getSimilarity(node1.getWord(), node2.getWord());
                if(distance >= minDistance) {
                    CMEdgeDO wEdge = new CMEdgeDO(node1, node2, CMEdgeType.Semantic, distance);
                    this.edgeList.add(wEdge);
                }
            }
        }
   }

    public CMGraphDO getCombinedGraph(CMGraphDO otherGraph) {
        List<CMNodeDO> thisNodeList = new ArrayList<>(this.nodeList);
        otherGraph.nodeList.stream().filter((otherGraphNode) -> (!nodeListContainsNode(thisNodeList, otherGraphNode))).forEach((otherGraphNode) -> {
            thisNodeList.add(otherGraphNode);
        });

        List<CMEdgeDO> thisEdgeList = new ArrayList<>(this.edgeList);
        otherGraph.edgeList.stream().filter((otherGraphEdge) -> (!edgeListContainsEdge(thisEdgeList, otherGraphEdge))).forEach((otherGraphEdge) -> {
            thisEdgeList.add(otherGraphEdge);
        });

        CMGraphDO outGraph = new CMGraphDO();
        outGraph.nodeList = thisNodeList;
        outGraph.edgeList = thisEdgeList;

        return outGraph;
    }
    
    @Override
    public String toString() {
        return this.nodeList.toString() + "\n" + this.edgeList.toString();
    }
    
    
    public CMGraphStatistics getGraphStatistics() {
        CMGraphStatistics statistics = new CMGraphStatistics();
        
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();

        // get models
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        DirectedGraph graph = graphModel.getDirectedGraph();
        Map<String, Node> associations = new TreeMap<>();

        // build all nodes
        this.nodeList.forEach(node -> {
            Node wordNode = graphModel.factory().newNode(node.getWord().getLemma());
            wordNode.setLabel(node.getWord().getLemma());
            associations.put(node.getWord().getLemma(), wordNode);
            graph.addNode(wordNode);
        });
        
        this.edgeList.forEach(edge -> {
            Edge e = graphModel.factory().newEdge(associations.get(edge.getNode1().getWord().getLemma()), associations.get(edge.getNode2().getWord().getLemma()));
            e.setWeight((float) (edge.getScore()));
            graph.addEdge(e);
        });
        

        GraphDensity density = new GraphDensity();
        density.setDirected(false);
        density.execute(graphModel);
        
        statistics.setDensity(density.getDensity());

        ConnectedComponents connectedComponents = new ConnectedComponents();
        connectedComponents.setDirected(false);
        connectedComponents.execute(graphModel);
        statistics.setConnectedComponentsCount(connectedComponents.getConnectedComponentsCount());

        ClusteringCoefficient clusteringCoefficient = new ClusteringCoefficient();
        clusteringCoefficient.setDirected(false);
        clusteringCoefficient.execute(graphModel);
        statistics.setAverageClusteringCoefficient(clusteringCoefficient.getAverageClusteringCoefficient());

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
            statistics.setBetweenness(avgBetweenness / (double)graph.getNodeCount());
            statistics.setCloseness(avgCloseness / (double)graph.getNodeCount());
            statistics.setEccentricity(avgEccentricity / (double)graph.getNodeCount());
        }
        
        statistics.setDiameter(distance.getDiameter());
        statistics.setPathLength(distance.getPathLength());

        return statistics;
    }
}
