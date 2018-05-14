/**
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

import com.readerbench.comprehensionmodel.utils.indexer.graphStruct.*;
import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.comprehensionmodel.utils.AoAMetric;
import com.readerbench.comprehensionmodel.utils.indexer.CMIndexer;
import com.readerbench.comprehensionmodel.utils.indexer.WordDistanceIndexer;
import com.readerbench.coreservices.data.Word;
import com.readerbench.coreservices.semanticmodels.SemanticModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 *
 * @author ionutparaschiv
 */
public class WordLinkageCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(WordLinkageCalculator.class);

    private final SemanticModel semanticModel;
    private final double threshold;
    private AbstractDocument document;

    private CMGraphDO graph;

    public WordLinkageCalculator(String text, SemanticModel semanticModel, double threshold) {
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
            semanticGraph.addNodeIfNotExistsOrUpdate(new CMNodeDO(word, CMNodeType.TextBased));
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
        double totalNoOccurences = 0.0;

        for (CMNodeDO node : this.graph.getNodeList()) {
            double aoaScore;
            if (aoa.containsKey(node.getWord().getLemma())) {
                aoaScore = aoa.get(node.getWord().getLemma());
            } else if (aoa.containsKey(node.getWord().toString())) {
                aoaScore = aoa.get(node.getWord().toString());
            } else {
                continue;
            }
            double noOccurences = 1.0;
            if (document.getWordOccurences().containsKey(node.getWord())) {
                noOccurences = (double) document.getWordOccurences().get(node.getWord());
            }

            double nodeDegree = (double) this.graph.getEdgeList(node).size();

            idsAoaSum += noOccurences * aoaScore;
            idfSum += noOccurences;

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

    /**
     *
     * @param normFile
     * @return
     */
    public static Map<String, Double> getWordAcquisitionAge(String normFile) {
        Map<String, Double> aoaWords = new HashMap<>();
        LOGGER.info("Loading file {}...", normFile);

        /* Compute the AgeOfAcquisition Dictionary */
        String tokens[];
        String line;
        String word;
        try {
            try (BufferedReader br = new BufferedReader(
                    new FileReader("resources/config/EN/word lists/AoA/" + normFile))) {
                while ((line = br.readLine()) != null) {
                    tokens = line.split(",");
                    word = tokens[0].trim().replaceAll(" ", "");

                    if (tokens[1].equals("NA")) {
                        continue;
                    }

                    Double.parseDouble(tokens[1]);
                    aoaWords.put(word, Double.parseDouble(tokens[1]));
                }
            }
        } catch (IOException | NumberFormatException e) {
            LOGGER.error(e.getMessage());
        }
        return aoaWords;
    }

    static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, StandardCharsets.UTF_8);
    }

}
