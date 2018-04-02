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
package com.readerbench.comprehensionmodel.utils;

import com.readerbench.datasourceprovider.data.Word;
import com.readerbench.comprehensionmodel.utils.indexer.graphStruct.CMGraphDO;
import com.readerbench.comprehensionmodel.utils.indexer.graphStruct.CMNodeDO;
import com.readerbench.comprehensionmodel.utils.indexer.graphStruct.CMNodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

public class ActivationScoreLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivationScoreLogger.class);

    public static final String OUTPUT_FILE_NAME = "out/comprehension_model_scores.csv";
    private final List<Map<Word, WordActivation>> activationHistory;
    private final List<CMNodeDO> uniqueWordList;

    public ActivationScoreLogger() {
        this.activationHistory = new ArrayList<>();
        this.uniqueWordList = new ArrayList<>();
    }

    public void saveScores(Map<CMNodeDO, Double> activationMap) {
        Map<Word, WordActivation> activationMapCopy = new TreeMap<>();
        Iterator<CMNodeDO> nodeIterator = activationMap.keySet().iterator();
        while (nodeIterator.hasNext()) {
            CMNodeDO currentNode = nodeIterator.next();
            activationMapCopy.put(currentNode.getWord(), new WordActivation(activationMap.get(currentNode), currentNode.isActive()));
        }
        this.activationHistory.add(activationMapCopy);
    }

    public void saveNodes(CMGraphDO graph) {
        graph.getNodeList().stream().forEach((node) -> {
            this.addNodeIfNotExists(node);
        });
    }

    private void addNodeIfNotExists(CMNodeDO nodeToAdd) {
        boolean exists = false;
        for (int i = 0; i < this.uniqueWordList.size(); i++) {
            CMNodeDO currentNode = this.uniqueWordList.get(i);
            if (currentNode.equals(nodeToAdd)) {
                if (currentNode.getNodeType() != CMNodeType.TextBased && nodeToAdd.getNodeType() == CMNodeType.TextBased) {
                    this.uniqueWordList.set(i, nodeToAdd);
                }
                exists = true;
                break;
            }
        }
        if (!exists) {
            this.uniqueWordList.add(nodeToAdd);
        }
    }

    public void logSavedScores() {
        try (FileWriter fwrt = new FileWriter(ActivationScoreLogger.OUTPUT_FILE_NAME); BufferedWriter bfwrt = new BufferedWriter(fwrt)) {
            bfwrt.write("SEP=,");
            bfwrt.newLine();

            String header = "Word,Type";
            for (int i = 0; i < activationHistory.size(); i++) {
                header += ",Phrase " + (i + 1) + ",Active?";
            }
            bfwrt.write(header);

            for (CMNodeDO node : this.uniqueWordList) {
                if (node.getNodeType() == CMNodeType.TextBased) {
                    String line = this.getLogLineForNode(node, "Text-Based");
                    bfwrt.newLine();
                    bfwrt.write(line);
                }
            }

            for (CMNodeDO node : this.uniqueWordList) {
                if (node.getNodeType() != CMNodeType.TextBased) {
                    String line = this.getLogLineForNode(node, "Semantically Inferred");
                    bfwrt.newLine();
                    bfwrt.write(line);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    private String getLogLineForNode(CMNodeDO node, String nodeType) {
        String line = node.getWord().getLemma() + "," + nodeType;

        for (Map<Word, WordActivation> activationMap : this.activationHistory) {
            if (!activationMap.containsKey(node.getWord())) {
                line += ",0,";
            } else {
                line += "," + activationMap.get(node.getWord()).toString();
            }
        }
        return line;
    }
    
    public List<Map<Word, WordActivation>> getActivationHistory() {
        return this.activationHistory;
    }
    public List<CMNodeDO> getUniqueWordList() {
        return this.uniqueWordList;
    }
}
