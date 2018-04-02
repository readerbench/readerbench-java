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
package com.readerbench.comprehensionmodel;

import com.readerbench.data.AbstractDocument;
import com.readerbench.data.Lang;
import com.readerbench.data.Word;
import org.openide.util.Exceptions;
import com.readerbench.comprehensionmodel.utils.AoAMetric;
import com.readerbench.comprehensionmodel.utils.indexer.CMIndexer;
import com.readerbench.comprehensionmodel.utils.indexer.WordDistanceIndexer;
import com.readerbench.coreservices.semanticModels.LSA.LSA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.readerbench.services.ageOfExposure.TASAAnalyzer.getWordAcquisitionAge;

/**
 *
 * @author ionutparaschiv
 */
public class WordLinkageCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(WordLinkageCalculator.class);

    private final LSA semanticModel;
    private final double threshold;
    private AbstractDocument document;

    private com.readerbench.services.comprehensionModel.utils.indexer.graphStruct.CMGraphDO graph;

    public WordLinkageCalculator(String text, LSA semanticModel, double threshold) {
        this.semanticModel = semanticModel;
        this.threshold = threshold;

        CMIndexer cmIndexer = new CMIndexer(text, semanticModel);
        this.graph = this.buildSemanticGraph(cmIndexer.getDocument());
        this.document = cmIndexer.getDocument();
        List<WordDistanceIndexer> syntacticIndexers = cmIndexer.getSyntacticIndexerList();
        for (WordDistanceIndexer indexer : syntacticIndexers) {
            this.graph.combineWithLinksFrom(indexer.getCMGraph(com.readerbench.services.comprehensionModel.utils.indexer.graphStruct.CMNodeType.TextBased));
        }
    }

    private com.readerbench.services.comprehensionModel.utils.indexer.graphStruct.CMGraphDO buildSemanticGraph(AbstractDocument document) {
        com.readerbench.services.comprehensionModel.utils.indexer.graphStruct.CMGraphDO semanticGraph = new com.readerbench.services.comprehensionModel.utils.indexer.graphStruct.CMGraphDO();
        List<Word> wordList = this.getWordList(document);
        wordList.forEach((word) -> {
            semanticGraph.addNodeIfNotExistsOrUpdate(new com.readerbench.services.comprehensionModel.utils.indexer.graphStruct.CMNodeDO(word, com.readerbench.services.comprehensionModel.utils.indexer.graphStruct.CMNodeType.TextBased));
        });

        List<com.readerbench.services.comprehensionModel.utils.indexer.graphStruct.CMNodeDO> nodeList = semanticGraph.getNodeList();
        List<com.readerbench.services.comprehensionModel.utils.indexer.graphStruct.CMEdgeDO> edgeList = new ArrayList();
        for (int i = 0; i < nodeList.size(); i++) {
            for (int j = i + 1; j < nodeList.size(); j++) {
                double distance = this.semanticModel.getSimilarity(nodeList.get(i).getWord(), nodeList.get(j).getWord());
                if (distance >= this.threshold) {
                    com.readerbench.services.comprehensionModel.utils.indexer.graphStruct.CMEdgeDO edge = new com.readerbench.services.comprehensionModel.utils.indexer.graphStruct.CMEdgeDO(nodeList.get(i), nodeList.get(j), com.readerbench.services.comprehensionModel.utils.indexer.graphStruct.CMEdgeType.Semantic, distance);
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
        
        for (com.readerbench.services.comprehensionModel.utils.indexer.graphStruct.CMNodeDO node : this.graph.getNodeList()) {
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
            
            double nodeDegree = (double) this.graph.getEdgeList(node).size();
            double idf = this.semanticModel.getWordIDf(node.getWord());
            
            idsAoaSum += noOccurences * idf * aoaScore;
            idfSum += noOccurences * idf;
            
            scoreSum += nodeDegree * aoaScore;
            degreeSum += nodeDegree;
            
            totalNoOccurences += noOccurences;
            sumAoa += noOccurences * aoaScore;
        }
        if (degreeSum == 0.0) {
            return new AoAMetric();
        }
                
        AoAMetric metric = new AoAMetric();
        metric.setAvg(sumAoa / totalNoOccurences);
        metric.setWeightedAvg(scoreSum / degreeSum);
        metric.setWeightedIdfAvg(idsAoaSum / idfSum);
        return metric;
    }

    public static void analyzeFiles() {
        LSA semanticModel = LSA.loadLSA("resources/config/EN/LSA/COCA_newspaper", Lang.en);
//        LSA semanticModel = Word2VecModel.loadWord2Vec("resources/config/EN/word2vec/COCA_newspaper", Lang.en);
        double threshold = 0.3;

//        String filePath = "resources/in/essays/essays_FYP_en/texts/";
//        String saveLocation = "resources/in/essays/essays_FYP_en/";
        
//        String filePath = "resources/in/cohesion/Archive/texts/";
//        String saveLocation = "resources/in/cohesion/Archive/";

//        String filePath = "resources/in/cohesion/CohMetrix/texts/";
//        String saveLocation = "resources/in/cohesion/CohMetrix/";
        
//        String filePath = "resources/in/cohesion/msu timed/posttest essays fall 2009/";
//        String saveLocation = "resources/in/cohesion/msu timed/";

        String filePath = "resources/in/cohesion/msu timed/pretest spring 2010/1113 pretest essays/";
        String saveLocation = "resources/in/cohesion/msu timed/pretest spring 2010/";

        try {
            Map<String, List<AoAMetric>> scoreMap = new HashMap();
            Map<String, com.readerbench.services.comprehensionModel.utils.indexer.graphStruct.CMGraphStatistics> graphStatisticsMap = new HashMap();
            
            String[] aoaFiles = {"Bird.csv", "Bristol.csv", "Cortese.csv", "Kuperman.csv", "Shock.csv"};

            File folder = new File(filePath);
            FileFilter filter = (File f) -> f.getName().endsWith(".txt");
            File[] files = folder.listFiles(filter);
            for (File file : files) {
                LOGGER.info("Analyzing + " + file.getName() + "  ...");
                
                String text = readFile(file.getPath());
                WordLinkageCalculator calculator = new WordLinkageCalculator(text, semanticModel, threshold);
                
                List<AoAMetric> metricList = new ArrayList();
                for(String aoaFile : aoaFiles) {
                    AoAMetric metric = calculator.getScore(aoaFile);
                    metricList.add(metric);
                }
                
                String fileKey = file.getName().replace(".txt", "");
                scoreMap.put(fileKey, metricList);
                
                com.readerbench.services.comprehensionModel.utils.indexer.graphStruct.CMGraphStatistics statistics = calculator.graph.getGraphStatistics();
                graphStatisticsMap.put(fileKey, statistics);
            }
            
            BufferedWriter out = new BufferedWriter(new FileWriter(saveLocation + "/measurements_word_linkage.csv", true));
            StringBuilder concat = new StringBuilder();
            concat.append("File");
            for(String aoaFile : aoaFiles) {
                String scoreDesc = aoaFile.replace(".csv", "");
                concat.append("," + scoreDesc + " Avg," + scoreDesc + " Degree Avg," + scoreDesc + " Idf Avg");
            }
            concat.append(", Density, Connected Components Count, Average Clustering Coefficient, Betweenness, Closeness, Eccentricity, Diameter, Average Shortest Path Length");
            concat.append("\n");
                
            scoreMap.entrySet().stream().forEach(score -> {
                String fileName = score.getKey();
                List<AoAMetric> metricList = score.getValue();
                
                concat.append(fileName);
                metricList.forEach((metric) -> {
                    concat.append(",").append(metric.getAvg()).append(",").append(metric.getWeightedAvg()).append(",").append(metric.getWeightedIdfAvg());
                });
                
                com.readerbench.services.comprehensionModel.utils.indexer.graphStruct.CMGraphStatistics statistics = graphStatisticsMap.get(fileName);
                concat.append(",").append(statistics.getDensity());
                concat.append(",").append(statistics.getConnectedComponentsCount());
                concat.append(",").append(statistics.getAverageClusteringCoefficient());
                concat.append(",").append(statistics.getBetweenness());
                concat.append(",").append(statistics.getCloseness());
                concat.append(",").append(statistics.getEccentricity());
                concat.append(",").append(statistics.getDiameter());
                concat.append(",").append(statistics.getPathLength());
                
                concat.append("\n");
            });
            try {
                out.write(concat.toString());
            } catch (IOException ex) {
                LOGGER.error(ex.getMessage());
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