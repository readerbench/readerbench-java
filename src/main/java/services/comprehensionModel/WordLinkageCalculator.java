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
import services.comprehensionModel.utils.AoAMetric;
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

    private final LSA semanticModel;
    private final double threshold;
    private AbstractDocument document;

    private CMGraphDO graph;

    public WordLinkageCalculator(String text, LSA semanticModel, double threshold) {
        this.semanticModel = semanticModel;
        this.threshold = threshold;

        CMIndexer cmIndexer = new CMIndexer(text, semanticModel);
        this.graph = this.buildSemanticGraph(cmIndexer.getDocument());
        this.document = cmIndexer.getDocument();
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
                wordSet.addAll(sentence.getAllWords());
            });
        });
        return new ArrayList<>(wordSet);
    }

    public AoAMetric getScore(String wordAcquisitionFile) {
        Map<String, Double> aoa = getWordAcquisitionAge(wordAcquisitionFile);

        double scoreSum = 0.0, degreeSum = 0.0, idfSum = 0.0;
        double sumAoa = 0.0;
        double idsAoaSum = 0.0;
        double numNodes = 0.0;
        double totalNoOccurences = 0.0;
        
        for (CMNodeDO node : this.graph.getNodeList()) {
            double aoaScore = 0.0;
            if (aoa.containsKey(node.getWord().getLemma())) {
                aoaScore = aoa.get(node.getWord().getLemma());
            } else if (aoa.containsKey(node.getWord().toString())) {
                aoaScore = aoa.get(node.getWord().toString());
            }
            else {
                continue;
            }
            numNodes ++;
            double noOccurences = 1.0;
            if (document.getWordOccurences().containsKey(node.getWord())) {
                noOccurences = (double)document.getWordOccurences().get(node.getWord());
            }
            totalNoOccurences += noOccurences;
            
            double nodeDegree = (double) this.graph.getEdgeList(node).size();
            double idf = this.semanticModel.getWordIDf(node.getWord());
            
            idsAoaSum += noOccurences * idf * aoaScore;
            idfSum += idf;
            
            scoreSum += nodeDegree * aoaScore;
            degreeSum += nodeDegree;
            
            sumAoa += noOccurences * aoaScore;
        }
        if (degreeSum == 0.0) {
            return new AoAMetric();
        }
                
        AoAMetric metric = new AoAMetric();
        metric.setAvg(sumAoa / (numNodes * totalNoOccurences));
        metric.setWeightedAvg(scoreSum / degreeSum);
        metric.setWeightedIdfAvg(idsAoaSum / (idfSum * totalNoOccurences));
        return metric;
    }

    public static void analyzeFiles() {
        LSA semanticModel = LSA.loadLSA(CSCLConstants.LSA_PATH, Lang.en);
        double threshold = 0.3;

        String filePath = "resources/in/essays/essays_FYP_en/texts/";
        String saveLocation = "resources/in/essays/essays_FYP_en/";
        try {
            Map<String, List<AoAMetric>> scoreMap = new HashMap();
            
            String[] aoaFiles = {"Bird.csv", "Bristol.csv", "Cortese.csv", "Kuperman.csv", "Shock.csv"};

            File folder = new File(filePath);
            FileFilter filter = (File f) -> f.getName().endsWith(".txt");
            File[] files = folder.listFiles(filter);
            for (File file : files) {
                logger.info("Analyzing + " + file.getName() + "  ...");
                
                String text = readFile(file.getPath());
                WordLinkageCalculator calculator = new WordLinkageCalculator(text, semanticModel, threshold);
                
                List<AoAMetric> metricList = new ArrayList();
                for(String aoaFile : aoaFiles) {
                    AoAMetric metric = calculator.getScore(aoaFile);
                    metricList.add(metric);
                }
                
                String fileKey = file.getName().replace(".txt", "");
                scoreMap.put(fileKey, metricList);
            }
            
            BufferedWriter out = new BufferedWriter(new FileWriter(saveLocation + "/measurements_word_linkage.csv", true));
            StringBuilder concat = new StringBuilder();
            concat.append("File");
            for(String aoaFile : aoaFiles) {
                String scoreDesc = aoaFile.replace(".csv", "");
                concat.append("," + scoreDesc + " Avg," + scoreDesc + " Degree Avg," + scoreDesc + " Idf Avg");
            }
            concat.append("\n");
                
            scoreMap.entrySet().stream().forEach(score -> {
                String fileName = score.getKey();
                List<AoAMetric> metricList = score.getValue();
                
                concat.append(fileName);
                metricList.forEach((metric) -> {
                    concat.append("," + metric.getAvg() + "," + metric.getWeightedAvg() + "," + metric.getWeightedIdfAvg());
                });
                concat.append("\n");
            });
            try {
                out.write(concat.toString());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
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