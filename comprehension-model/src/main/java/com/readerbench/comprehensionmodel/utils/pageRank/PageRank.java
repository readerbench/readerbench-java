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
package com.readerbench.comprehensionmodel.utils.pageRank;

import com.readerbench.comprehensionmodel.utils.indexer.graphStruct.CMEdgeDO;
import com.readerbench.comprehensionmodel.utils.indexer.graphStruct.CMGraphDO;
import com.readerbench.comprehensionmodel.utils.indexer.graphStruct.CMNodeDO;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PageRank {

    public static int MAX_ITER = 100000;
    public static double EPS = 0.0001;
    public static double PROB = 0.85;

    public PageRank() {
    }

    public void runPageRank(CMGraphDO graph) {
        Map<CMNodeDO, Double> pageRankValues = graph.getActivationMap();
        Map<CMNodeDO, Double> newPageRankValues = this.runPageRank(pageRankValues, graph);
        newPageRankValues.entrySet().stream().forEach(entry -> {
            CMNodeDO actualNode = graph.getNode(entry.getKey());
            actualNode.setActivationScore(entry.getValue());
        });
    }

    private Map<CMNodeDO, Double> runPageRank(Map<CMNodeDO, Double> pageRankValues, CMGraphDO graph) {
        int algIteration = 0;
        Map<CMNodeDO, Double> currentPageRankValues = new TreeMap<>(pageRankValues);
        while (algIteration < PageRank.MAX_ITER) {
            double r = this.calculateR(currentPageRankValues, graph);

            Map<CMNodeDO, Double> tempPageRankValues = new TreeMap<>();
            boolean done = true;
            for (CMNodeDO node : graph.getNodeList()) {
                double tempPRValue = this.computeTempPageRankValue(currentPageRankValues, graph, node, r);
                double prevPRValue = this.getPageRankValue(currentPageRankValues, node, graph);

                tempPageRankValues.put(node, tempPRValue);

                if ((tempPRValue - prevPRValue) / prevPRValue >= PageRank.EPS) {
                    done = false;
                }
            }
            currentPageRankValues = tempPageRankValues;
            if (done) {
                break;
            }
            algIteration++;
        }
        return currentPageRankValues;
    }

    private double calculateR(Map<CMNodeDO, Double> pageRankValues, CMGraphDO graph) {
        double r = 0;
        double N = (double) graph.getNodeList().size();
        for (CMNodeDO node : graph.getNodeList()) {
            List<CMEdgeDO> nodeEdgeList = graph.getActiveEdgeList(node);
            double nodeDegree = (double) nodeEdgeList.size();
            double nodePageRankVal = this.getPageRankValue(pageRankValues, node, graph);
            if (nodeDegree > 0) {
                r += (1.0 - PageRank.PROB) * (nodePageRankVal / N);
            } else {
                r += (nodePageRankVal / N);
            }
        }
        return r;
    }

    private double computeTempPageRankValue(Map<CMNodeDO, Double> pageRankValues, CMGraphDO graph, CMNodeDO node, double r) {
        double res = r;
        List<CMEdgeDO> nodeEdgeList = graph.getActiveEdgeList(node);
        for (CMEdgeDO edge : nodeEdgeList) {
            CMNodeDO neighbor = edge.getOppositeNode(node);
            List<CMEdgeDO> neighborEdgeList = graph.getActiveEdgeList(neighbor);
            double normalize = (double) neighborEdgeList.size();
            res += PageRank.PROB * (this.getPageRankValue(pageRankValues, neighbor, graph) / normalize);
        }
        return res;
    }

    private double getPageRankValue(Map<CMNodeDO, Double> pageRankValues, CMNodeDO node, CMGraphDO graph) {
        if (pageRankValues.containsKey(node)) {
            return pageRankValues.get(node);
        }
        return 1 / ((double) graph.getNodeList().size());
    }
}
