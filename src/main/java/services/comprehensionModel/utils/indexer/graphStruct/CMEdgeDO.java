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

public class CMEdgeDO {

    private final CMNodeDO node1;
    private final CMNodeDO node2;
    private final CMEdgeType edgeType;
    private final double score;
    private boolean isActive;

    public CMEdgeDO(CMNodeDO node1, CMNodeDO node2, CMEdgeType edgeType, double score) {
        this.node1 = node1;
        this.node2 = node2;
        this.edgeType = edgeType;
        this.score = score;
        this.isActive = true;
    }

    public CMNodeDO getNode1() {
        return node1;
    }

    public CMNodeDO getNode2() {
        return node2;
    }

    public CMEdgeType getEdgeType() {
        return edgeType;
    }

    public double getScore() {
        return score;
    }

    public boolean equals(CMEdgeDO otherEdge) {
        return ((this.node1.equals(otherEdge.node1) && this.node2.equals(otherEdge.node2))
                || (this.node1.equals(otherEdge.node2) && this.node2.equals(otherEdge.node1)))
                && this.edgeType == otherEdge.edgeType
                && this.score == otherEdge.score;
    }

    public String getEdgeTypeString() {
        return edgeType.toString();
    }

    public CMNodeDO getOppositeNode(CMNodeDO node) {
        if (this.node1.equals(node)) {
            return this.node2;
        }
        if (this.node2.equals(node)) {
            return this.node1;
        }
        return null;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    @Override
    public String toString() {
        return this.node1.getWord().getLemma() + " - " + this.node2.getWord().getLemma()
                + " (" + this.getEdgeTypeString() + ")" + ": "
                + this.score + " " + (this.isActive ? "1" : "0");
    }
}
