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
package services.comprehensionModel.utils.pageRank;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import services.comprehensionModel.utils.indexer.graphStruct.CMNodeDO;

public class NodeRank implements Comparable<NodeRank> {
    private CMNodeDO node;
    private Double value;

    public CMNodeDO getNode() {
        return node;
    }

    public Double getValue() {
        return value;
    }

    @Override
    public int compareTo(NodeRank otherRank) {
        if (this.value < otherRank.value) {
            return -1;
        }
        if (this.value > otherRank.value) {
            return 1;
        }
        return 0;
    }
    
    @Override
    public String toString() {
        return "{" + this.node.getWord() + ": " + this.value + "}";
    }

    public static List<NodeRank> convertMapToNodeRankList(Map<CMNodeDO, Double> nodeActivationScoreMap) {
        List<NodeRank> rankList = new ArrayList<>();
        Iterator<CMNodeDO> nodeIterator = nodeActivationScoreMap.keySet().iterator();
        while (nodeIterator.hasNext()) {
            NodeRank rank = new NodeRank();
            rank.node = nodeIterator.next();
            rank.value = nodeActivationScoreMap.get(rank.node);
            rankList.add(rank);
        }
        return rankList;

    }
}
