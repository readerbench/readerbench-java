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

import java.util.ArrayList;
import java.util.List;

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

    private void addNodeIfNotExists(CMNodeDO otherNode) {
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
}
