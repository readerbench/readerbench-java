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
package com.readerbench.comprehensionmodel.utils.distanceStrategies.utils;

import com.readerbench.comprehensionmodel.utils.CMUtils;
import com.readerbench.comprehensionmodel.utils.indexer.graphStruct.CMEdgeDO;
import com.readerbench.comprehensionmodel.utils.indexer.graphStruct.CMEdgeType;
import com.readerbench.comprehensionmodel.utils.indexer.graphStruct.CMNodeDO;
import com.readerbench.comprehensionmodel.utils.indexer.graphStruct.CMNodeType;
import com.readerbench.datasourceprovider.data.Word;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class CMSyntacticGraph {

    private final CMUtils cmUtils;
    private final List<CMEdgeDO> edgeList;
    private final Set<Word> wordSet;

    public CMSyntacticGraph() {
        this.cmUtils = new CMUtils();
        this.edgeList = new ArrayList<>();
        this.wordSet = new TreeSet<>();
    }

    public void indexEdge(Word word1, Word word2) {
        CMNodeDO node1 = new CMNodeDO(word1, CMNodeType.TextBased);
        CMNodeDO node2 = new CMNodeDO(word2, CMNodeType.TextBased);
        CMEdgeDO edge = new CMEdgeDO(node1, node2, CMEdgeType.Syntactic, 1.0d);
        this.wordSet.add(word1);
        this.wordSet.add(word2);
        this.edgeList.add(edge);
    }

    public List<Word> getWordList() {
        return this.cmUtils.convertIteratorToList(this.wordSet.iterator());
    }

    public List<CMEdgeDO> getEdgeList() {
        return this.edgeList;
    }
}
