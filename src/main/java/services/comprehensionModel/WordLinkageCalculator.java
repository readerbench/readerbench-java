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
package services.comprehensionModel;

import data.AbstractDocument;
import data.Lang;
import data.Word;
import data.cscl.CSCLConstants;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import static services.ageOfExposure.TASAAnalyzer.getWordAcquisitionAge;
import services.comprehensionModel.utils.indexer.CMIndexer;
import services.comprehensionModel.utils.indexer.WordDistanceIndexer;
import services.comprehensionModel.utils.indexer.graphStruct.CMEdgeDO;
import services.comprehensionModel.utils.indexer.graphStruct.CMEdgeType;
import services.comprehensionModel.utils.indexer.graphStruct.CMGraphDO;
import services.comprehensionModel.utils.indexer.graphStruct.CMNodeDO;
import services.comprehensionModel.utils.indexer.graphStruct.CMNodeType;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LSA.LSA;

/**
 *
 * @author ionutparaschiv
 */
public class WordLinkageCalculator {
    public static final Logger logger = Logger.getLogger("");

    private final ISemanticModel semanticModel;
    private final double threshold;

    private CMGraphDO graph;

    public WordLinkageCalculator(String text, ISemanticModel semanticModel, double threshold) {
        this.semanticModel = semanticModel;
        this.threshold = threshold;

        CMIndexer cmIndexer = new CMIndexer(text, semanticModel);
        this.graph = this.buildSemanticGraph(cmIndexer.getDocument());
        List<WordDistanceIndexer> syntacticIndexers = cmIndexer.getSyntacticIndexerList();
        for (WordDistanceIndexer indexer : syntacticIndexers) {
            this.graph.combineWithLinksFrom(indexer.getCMGraph(CMNodeType.TextBased));
        }
    }

    private CMGraphDO buildSemanticGraph(AbstractDocument document) {
        CMGraphDO semanticGraph = new CMGraphDO();
        List<Word> wordList = this.getWordList(document);
        wordList.forEach((word) -> {
            semanticGraph.addNodeIfNotExists(new CMNodeDO(word, CMNodeType.TextBased));
        });

        List<CMNodeDO> nodeList = semanticGraph.getNodeList();
        List<CMEdgeDO> edgeList = new ArrayList();
        for (int i = 0; i < nodeList.size(); i++) {
            for (int j = i + 1; j < nodeList.size(); j++) {
                double distance = this.semanticModel.getSimilarity(nodeList.get(i).getWord(), nodeList.get(j).getWord());
                if (distance >= this.threshold) {
                    CMEdgeDO edge = new CMEdgeDO(nodeList.get(i), nodeList.get(j), CMEdgeType.Semantic, distance);
                    edgeList.add(edge);
                }
            }
        }
        semanticGraph.setEdgeList(edgeList);
        return semanticGraph;
    }

    private List<Word> getWordList(AbstractDocument document) {
        Set<Word> wordSet = new TreeSet();
        document.getBlocks().forEach((block) -> {
            block.getSentences().forEach((sentence) -> {
                List<Word> wordList = sentence.getAllWords();
                wordList.forEach((word) -> {
                    wordSet.add(word);
                });
            });
        });
        return new ArrayList<>(wordSet);
    }

    public double getScore(String wordAcquisitionFile) {
        Map<String, Double> birdAoA = getWordAcquisitionAge(wordAcquisitionFile);

        double scoreSum = 0.0;
        double degreeSum = 0.0;
        for (CMNodeDO node : this.graph.getNodeList()) {
            double nodeDegree = (double) this.graph.getEdgeList(node).size();

            double aoaScore = 0.0;
            if (birdAoA.containsKey(node.getWord().getLemma())) {
                aoaScore = birdAoA.get(node.getWord().getLemma());
            } else if (birdAoA.containsKey(node.getWord().toString())) {
                aoaScore = birdAoA.get(node.getWord().toString());
            }

            degreeSum += nodeDegree;
            scoreSum += nodeDegree * aoaScore;
        }
        if (degreeSum == 0.0) {
            return 0.0;
        }
        return scoreSum / degreeSum;
    }

    public static void analyzeFiles() {
        ISemanticModel semanticModel = LSA.loadLSA(CSCLConstants.LSA_PATH, Lang.en);
        double threshold = 0.3;

        String filePath = "resources/in/essays/essays_FYP_en/texts/";
        String saveLocation = "resources/in/essays/essays_FYP_en/";
        try {
            Map<String, Double> scoreMap = new HashMap();

            File folder = new File(filePath);
            FileFilter filter = (File f) -> f.getName().endsWith(".txt");
            File[] files = folder.listFiles(filter);
            for (File file : files) {
                logger.info("Analyzing + " + file.getName() + "  ...");
                
                String text = readFile(file.getPath());
                WordLinkageCalculator calculator = new WordLinkageCalculator(text, semanticModel, threshold);
                
                // Bird.csv Bristol.csv Cortese.csv Kuperman.csv Shock.csv
                double score = calculator.getScore("Bird.csv");
                String fileKey = file.getName().replace(".txt", ".xml");
                scoreMap.put(fileKey, score);
            }
            
            BufferedWriter out = new BufferedWriter(new FileWriter(saveLocation + "/measurements_word_linkage.csv", true));
            scoreMap.entrySet().stream().forEach(score -> {
                StringBuilder concat = new StringBuilder();
                concat.append(score.getKey() + "," + score.getValue() + "\n");
                try {
                    out.write(concat.toString());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) {
        WordLinkageCalculator.analyzeFiles();
    }
}