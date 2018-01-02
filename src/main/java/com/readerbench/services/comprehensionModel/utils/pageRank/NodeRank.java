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
package com.readerbench.services.comprehensionModel.utils.pageRank;

import com.readerbench.services.comprehensionModel.utils.indexer.graphStruct.CMNodeDO;

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
}
