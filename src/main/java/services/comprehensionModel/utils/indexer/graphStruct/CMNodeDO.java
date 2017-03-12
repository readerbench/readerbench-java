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

public class CMNodeDO implements Comparable<CMNodeDO> {

    private final Word word;
    private CMNodeType nodeType;
    private boolean isActive;

    public CMNodeDO(Word word, CMNodeType nodeType) {
        this.word = word;
        this.nodeType = nodeType;
        this.isActive = false;
    }

    public Word getWord() {
        return word;
    }

    public CMNodeType getNodeType() {
        return nodeType;
    }
    public void setNodeType(CMNodeType nodeType) {
        this.nodeType = nodeType;
    }

    public boolean isActive() {
        return isActive;
    }

    public void activate() {
        isActive = true;
    }

    public void deactivate() {
        isActive = false;
    }

    @Override
    public boolean equals(Object obj) {
        CMNodeDO node = (CMNodeDO) obj;
        return this.word.getLemma().equals(node.word.getLemma());
    }
    public int hashCode() {
        return this.word.getLemma().hashCode();
    }
    @Override
    public int compareTo(CMNodeDO otherNode) {
        return this.word.getLemma().compareTo(otherNode.word.getLemma());
    }
    public String toString() {
        return this.word.getLemma() + " (" + this.nodeType.toString()
                + ")" + " " + (this.isActive ? "1" : "0");
    }
}