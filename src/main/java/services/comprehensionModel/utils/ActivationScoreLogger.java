package services.comprehensionModel.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import data.Word;
import org.openide.util.Exceptions;
import services.comprehensionModel.utils.indexer.graphStruct.CMGraphDO;
import services.comprehensionModel.utils.indexer.graphStruct.CMNodeDO;
import services.comprehensionModel.utils.indexer.graphStruct.CMNodeType;

public class ActivationScoreLogger {

    public static final String OUTPUT_FILE_NAME = "out/comprehension_model_scores.csv";
    private final List<Map<Word, WordActivation>> activationHistory;
    private final List<CMNodeDO> uniqueWordList;

    private class WordActivation {

        private final double activationValue;
        private final boolean isActive;

        public WordActivation(double activationValue, boolean isActive) {
            this.activationValue = activationValue;
            this.isActive = isActive;
        }

        public double getActivationValue() {
            return activationValue;
        }

        public boolean isActive() {
            return isActive;
        }

        @Override
        public String toString() {
            return this.activationValue + "," + (this.isActive ? "X" : "");
        }
    }

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
        graph.nodeList.stream().forEach((node) -> {
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
            Exceptions.printStackTrace(e);
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

}
