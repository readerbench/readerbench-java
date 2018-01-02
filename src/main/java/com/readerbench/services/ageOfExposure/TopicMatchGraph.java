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
package com.readerbench.services.ageOfExposure;

import com.readerbench.services.commons.Formatting;

import java.util.LinkedList;
import java.util.Queue;

/**
 * A bipartite graph.
 *
 * @author Teodor Rosu
 *
 */
public class TopicMatchGraph {

    private final int flow[][];
    private final double cost[][];
    private final int edge[][];

    private final int nodes;
    private final int sourceId;
    private final int sinkId;

    public TopicMatchGraph(int size) {
        this.nodes = size + 2;
        this.sourceId = size;
        this.sinkId = size + 1;

        this.flow = new int[this.nodes][];
        for (int i = 0; i < this.nodes; i++) {
            this.flow[i] = new int[this.nodes];
        }

        this.cost = new double[this.nodes][];
        for (int i = 0; i < this.nodes; i++) {
            this.cost[i] = new double[this.nodes];
        }
        this.cost[sourceId][sinkId] = 0D;

        this.edge = new int[this.nodes][];
        for (int i = 0; i < this.nodes; i++) {
            this.edge[i] = new int[this.nodes];
        }
    }

    public void addEdge(int start, int end, double cost) {
        this.flow[start][end] = 0;
        this.cost[start][end] = cost;
        this.cost[end][start] = -cost;
        this.edge[start][end] = 1;

        this.flow[sourceId][start] = 0;
        this.edge[sourceId][start] = 1;
        this.cost[sourceId][start] = 0D;

        this.flow[end][sinkId] = 0;
        this.edge[end][sinkId] = 1;
        this.cost[end][sinkId] = 0D;
        // this.cost[sourceId][sinkId] = this.cost[sinkId][end] = 0D;
    }

    public double getEdge(int start, int end) {
        return this.cost[start][end];
    }

    public Integer[] computeAssociations(int resultSize) {
        // int resultSize = 0;
        // for (int i = 0; i < this.nodes; i++)
        // if (edge[sourceId][i] != 0)
        // resultSize++;

        Integer[] result = new Integer[resultSize];

        double d[] = new double[this.nodes];
        boolean inQueue[] = new boolean[this.nodes];
        int parent[] = new int[this.nodes];
        final double INF = Double.MAX_VALUE;
        int i, j;

        // While there is a path use Bellman-Ford to find best path.
        while (true) {
            // 1.init
            for (i = 0; i < d.length; i++) {
                d[i] = INF;
                parent[i] = -1;
                inQueue[i] = false;
            }
            Queue<Integer> queue = new LinkedList<>();
            queue.offer(sourceId);
            inQueue[sourceId] = true;
            d[sourceId] = 0;

            // 2.find min cost path
            Integer currentNode;
            while (queue.peek() != null) {
                currentNode = queue.poll();
                inQueue[currentNode] = false;

                for (i = 0; i < this.nodes; i++) {
                    if (edge[currentNode][i] - flow[currentNode][i] > 0
                            && d[i] > d[currentNode] + cost[currentNode][i]) {
                        parent[i] = currentNode;
                        d[i] = d[currentNode] + cost[currentNode][i];
                        if (inQueue[i] == false) {
                            queue.offer(i);
                            inQueue[i] = true;
                        }
                    }
                }
            }

            // 3. no more paths
            if (d[sinkId] == INF) {
                break;
            }

            // 4. update flow on the new path (minFlow always is 1)
            i = sinkId;
            while (i != sourceId) {
                flow[parent[i]][i] += 1;
                flow[i][parent[i]] -= 1;
                i = parent[i];
            }
        }

        // Find associations (x, y) such that there is flow
        for (i = 0; i < this.nodes; i++) {
            for (j = 0; j < this.nodes; j++) {
                if (flow[i][j] == 1 && i != sourceId && j != sinkId) {
                    result[i] = j;
                    break;
                }
            }
        }
        return result;
    }

    public static void main(String args[]) {
        TopicMatchGraph graph = new TopicMatchGraph(7);
        graph.addEdge(0, 6, 0.3);
        graph.addEdge(0, 4, 0.2);
        graph.addEdge(0, 5, 0.2);

        graph.addEdge(1, 4, 0.1);

        graph.addEdge(2, 5, 0.1);
        graph.addEdge(2, 4, 0.2);
        graph.addEdge(2, 6, 0.3);

        graph.addEdge(3, 4, 0.1);
        graph.addEdge(3, 5, 0.1);
        graph.addEdge(3, 6, 0.1);

        System.out.println("Graph:");
        System.out.println(graph);

        Integer[] assoc = graph.computeAssociations(4);
        for (int i = 0; i < assoc.length; i++) {
            System.out.println(i + "--" + assoc[i]);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.nodes; i++) {
            for (int j = 0; j < this.nodes; j++) {
                if (cost[i][j] > 0) {
                    sb.append(i).append(" >> ").append(j).append(": ").append(Formatting.formatNumber(cost[i][j])).append(";\n");
                }
            }
        }
        return sb.toString();
    }
}