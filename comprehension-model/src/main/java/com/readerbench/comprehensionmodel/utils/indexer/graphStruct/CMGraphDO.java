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
package com.readerbench.comprehensionmodel.utils.indexer.graphStruct;

import com.readerbench.coreservices.data.Word;
import com.readerbench.coreservices.commons.VectorAlgebra;
import com.readerbench.coreservices.semanticmodels.SemanticModel;
import com.readerbench.coreservices.semanticmodels.wordnet.OntologySupport;

import java.util.*;
import java.util.stream.Collectors;

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

    public CMEdgeDO getEdge(CMEdgeDO otherEdge) {
        Optional<CMEdgeDO> retrievedEdge = edgeList.stream().filter(((edge) -> (edge.equals(otherEdge)))).findFirst();
        try {
            return retrievedEdge.get();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public void removeNodeLinks(CMNodeDO node) {
        if (!this.containsNode(node)) {
            return;
        }
        CMNodeDO actualNode = this.getNode(node);
        this.edgeList.removeIf(edge -> {
            return edge.getOppositeNode(actualNode) != null;
        });
    }

    public void addNodeIfNotExistsOrUpdate(CMNodeDO otherNode) {
        if (!this.containsNode(otherNode)) {
            this.nodeList.add(otherNode);
        } else {
            CMNodeDO actualNode = this.getNode(otherNode);
            actualNode.setActivationScore(actualNode.getActivationScore() + otherNode.getActivationScore());
            actualNode.deactivate();
            if (otherNode.isActive()) {
                actualNode.activate();
            }
            // if the other node is inferred we keey the actual node as it is
            if (otherNode.getNodeType() == CMNodeType.TextBased) {
                actualNode.setNodeType(CMNodeType.TextBased);
            }
        }
    }

    public void addEdgeIfNotExistsOrUpdate(CMEdgeDO otherEdge) {
        if (!this.containsEdge(otherEdge)) {
            this.edgeList.add(otherEdge);
        } else {
            CMEdgeDO actualEdge = this.getEdge(otherEdge);
            actualEdge.deactivate();
            if (otherEdge.isActive()) {
                actualEdge.activate();
            }
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

    public List<CMEdgeDO> getActiveEdgeList(CMNodeDO node) {
        return this.getEdgeList(node).stream().filter(edge -> edge.isActive()).collect(Collectors.toList());
    }

    public void combineWithLinksFrom(CMGraphDO otherGraph) {
        List<CMNodeDO> thisNodeList = new ArrayList<>(this.nodeList);
        for (CMNodeDO node : thisNodeList) {
            List<CMEdgeDO> otherGraphEdgeList = otherGraph.getEdgeList(node);
            otherGraphEdgeList.stream().filter((otherGraphEdge) -> (!this.containsEdge(otherGraphEdge))).map((otherGraphEdge) -> {
                this.addNodeIfNotExistsOrUpdate(otherGraphEdge.getNode1());
                return otherGraphEdge;
            }).map((otherGraphEdge) -> {
                this.addNodeIfNotExistsOrUpdate(otherGraphEdge.getNode2());
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

    public void combineWithSyntacticLinksFrom(CMGraphDO syntacticGraph, SemanticModel semanticModel, int maxDictionaryExpansion) {
        List<SemanticModel> models = new ArrayList();
        models.add(semanticModel);

        Set<CMNodeDO> allPotentialInferredNodeSet = new TreeSet<>();

        // ** STEP 1 ** Add all the links & nodes from the syntactic graph
        for (CMNodeDO node : syntacticGraph.getNodeList()) {
            node.getWord().setSemanticModels(models);
            node.activate();
            node.incrementActivationScore();

            // add the new text based nodes or update existing ones
            this.addNodeIfNotExistsOrUpdate(node);

            // add the related words from the dictionary as nodes with no links
            TreeMap<Word, Double> similarConcepts = OntologySupport.getSimilarConcepts(node.getWord());
            Iterator<Word> dictionaryWordIterator = similarConcepts.keySet().iterator();
            while (dictionaryWordIterator.hasNext()) {
                Word dictionaryWord = dictionaryWordIterator.next();
                if (dictionaryWord.isNoun() || dictionaryWord.isVerb()) {
                    dictionaryWord.setSemanticModels(models);
                    CMNodeDO inferredNode = new CMNodeDO(dictionaryWord, CMNodeType.Inferred);
                    inferredNode.activate();
                    allPotentialInferredNodeSet.add(inferredNode);
                }
            }
        }
        syntacticGraph.edgeList.stream().forEach((otherGraphEdge) -> {
            this.addEdgeIfNotExistsOrUpdate(otherGraphEdge);
        });

        System.out.println("--- Current Graph After Adding Syntactic & Semantic ---");
        System.out.println(this);

        // deactivate all te semantic links
        this.edgeList.stream().forEach(edge -> {
            if (edge.getEdgeType() == CMEdgeType.Semantic) {
                edge.deactivate();
            }
        });

        // get all the potential inferred edge list
        final List<CMNodeDO> potentialInferredNodeList = new ArrayList<>();
        allPotentialInferredNodeSet.stream().forEach(potentialInferredNode -> {
            double avgSimilarity = 0.0;

            for (CMNodeDO syntacticNode : syntacticGraph.getNodeList()) {
                avgSimilarity += semanticModel.getSimilarity(potentialInferredNode.getWord(), syntacticNode.getWord());
            }

            if (syntacticGraph.getNodeList().size() > 0) {
                avgSimilarity = avgSimilarity / ((double) syntacticGraph.getNodeList().size());
            }

            // use the activationScore to keep its mean avg similarity to the rest of the inferred nodes
            potentialInferredNode.setActivationScore(avgSimilarity);
            potentialInferredNodeList.add(potentialInferredNode);
        });

        // sort the potential inferred edge list
        Collections.sort(potentialInferredNodeList, (n1, n2) -> (-1) * new Double(n1.getActivationScore()).compareTo(n2.getActivationScore()));

        // expand up to maxDictionaryExpansion dictionary words
        int maxInferredExpand = maxDictionaryExpansion;
        
        List<CMNodeDO> inferredNodeList = potentialInferredNodeList.subList(0, Math.min(maxInferredExpand, potentialInferredNodeList.size()));

        inferredNodeList.stream().forEach(inferredNode -> {
            this.addNodeIfNotExistsOrUpdate(inferredNode);
        });

        List<CMNodeDO> activeNodeList = this.nodeList.stream().filter(node -> node.isActive()).collect(Collectors.toList());
        int N = activeNodeList.size();
        double[] distances = new double[N * (N - 1)];
        List<CMEdgeDO> potentialEdgeList = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            for (int j = i + 1; j < N; j++) {
                CMNodeDO node1 = activeNodeList.get(i), node2 = activeNodeList.get(j);
                double similarity = semanticModel.getSimilarity(node1.getWord(), node2.getWord());
                distances[i * N + j] = similarity;
                CMEdgeDO edge = new CMEdgeDO(node1, node2, CMEdgeType.Semantic, similarity);
                edge.activate();
                potentialEdgeList.add(edge);
            }
        }
        double avg = VectorAlgebra.avg(distances);
        double stdev = VectorAlgebra.stdev(distances);
        double minDistance = Math.min(0.3, avg + stdev);

        System.out.println("AVG = " + avg);
        System.out.println("STDEV = " + stdev);
        System.out.println("MIN DISTANCE = " + minDistance);

        potentialEdgeList.stream().filter((potentialEdge) -> (potentialEdge.getScore() >= minDistance))
                .forEach((potentialEdge) -> {
                    this.addEdgeIfNotExistsOrUpdate(potentialEdge);
                });

        System.out.println("--- Current Graph After Adding Potential Semantic Links ---");
        System.out.println(this);
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

    public Map<CMNodeDO, Double> getActivationMap() {
        Map<CMNodeDO, Double> activationMap = new TreeMap();
        this.getNodeList().stream().forEach(node -> {
            activationMap.put(node, node.getActivationScore());
        });
        return activationMap;
    }
}
