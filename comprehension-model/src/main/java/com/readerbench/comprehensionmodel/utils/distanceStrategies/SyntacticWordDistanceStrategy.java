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
package com.readerbench.comprehensionmodel.utils.distanceStrategies;

import com.readerbench.comprehensionmodel.utils.distanceStrategies.utils.CMSyntacticGraph;
import com.readerbench.comprehensionmodel.utils.indexer.graphStruct.CMEdgeDO;
import com.readerbench.comprehensionmodel.utils.indexer.graphStruct.CMEdgeType;
import com.readerbench.datasourceprovider.data.Word;

import java.util.List;

public class SyntacticWordDistanceStrategy implements IWordDistanceStrategy, java.io.Serializable {

    private static final long serialVersionUID = -8051746464532082314L;
    private final List<CMEdgeDO> edgeList;

    public SyntacticWordDistanceStrategy(CMSyntacticGraph syntacticGraph) {
        this.edgeList = syntacticGraph.getEdgeList();
    }

    @Override
    public double getDistance(Word w1, Word w2) {
        for (CMEdgeDO edge : this.edgeList) {
            Word dependentEdge = edge.getNode1().getWord();
            Word governorEdge = edge.getNode2().getWord();

            if ((dependentEdge.equals(w1) && governorEdge.equals(w2))
                    || (dependentEdge.equals(w2) && governorEdge.equals(w1))) {
                return 1.0;
            }
        }
        return 0.0;
    }

    @Override
    public CMEdgeType getCMEdgeType() {
        return CMEdgeType.Syntactic;
    }
}
