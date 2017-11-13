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
package runtime.converters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;

import org.junit.Test;

import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.AbstractDocumentTemplate.BlockTemplate;
import data.Word;
import data.discourse.SemanticCohesion;
import data.discourse.Keyword;
import data.document.Document;
import data.Lang;
import java.util.EnumMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import services.commons.Formatting;
import services.complexity.ComplexityIndices;
import services.converters.PdfToTxtConverter;
import services.discourse.keywordMining.KeywordModeling;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.SimilarityType;

/**
 * Processes a path containing CV files in order to verify text extraction from
 * PDF files
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
@Deprecated
public class PdfToTextFrenchCVs {

    private static final Logger LOGGER = Logger.getLogger("");

    @Test
    public void process() {
        LOGGER.info("Starting French CVs processing...");
        StringBuilder sb = new StringBuilder();
        sb.append("sep=\t\nfile\tconcepts\n");
        try {
            Files.walk(Paths.get("resources/in/cv_analysis")).forEach(filePath -> {
                String filePathString = filePath.toString();
                if (filePathString.contains(".pdf")) {
                    LOGGER.log(Level.INFO, "Processing file: {0}", filePathString);
                    PdfToTxtConverter pdfConverter = new PdfToTxtConverter(filePathString, true);
                    pdfConverter.process();
                    List<ResultNode> nodes = getTopics(pdfConverter.getParsedText(), "resources/config/FR/LSA/Le_Monde", "resources/config/FR/LDA/Le_Monde", Lang.fr, false, false, false, 0.3);

                    StringBuilder sbNode = new StringBuilder();
                    nodes.stream().forEach((node) -> {
                        sbNode.append(node.name).append(" (").append(node.value).append("), ");
                    });
                    // delete last comma
                    if (sbNode.length() > 2) {
                        sbNode.setLength(sbNode.length() - 2);
                    }
                    sbNode.append("\n");
                    sb.append(filePath.getFileName().toString()).append("\t").append(sbNode.toString());
                    LOGGER.log(Level.INFO, "Finished processing file: {0}", filePathString);
                }
            });
            File file = new File("french_cvs.csv");
            FileUtils.writeStringToFile(file, sb.toString());
            LOGGER.log(Level.INFO, "Printed information to: {0}", file.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.info("Error opening path.");
            System.exit(-1);
        }
    }

    private List<ResultNode> getTopics(String query, String pathToLSA, String pathToLDA, Lang lang,
            boolean posTagging, boolean computeDialogism, boolean useBigrams, double threshold) {

        List<ResultNode> nodes = new ArrayList<>();
        AbstractDocument queryDoc = processQuery(query, pathToLSA, pathToLDA, lang, posTagging, computeDialogism, useBigrams);

        List<Keyword> topics = KeywordModeling.getSublist(queryDoc.getTopics(), 50, false, false);

        // build connected graph
        Map<Word, Boolean> visibleConcepts = new TreeMap<>();

        topics.stream().forEach((t) -> {
            visibleConcepts.put(t.getWord(), false);
        });

        // determine similarities
        visibleConcepts.keySet().stream().forEach((Word w1) -> {
            for (Word w2 : visibleConcepts.keySet()) {
                EnumMap<SimilarityType, Double> similarities = new EnumMap<>(SimilarityType.class);
                for (ISemanticModel model : queryDoc.getSemanticModels()) {
                    similarities.put(model.getType(), model.getSimilarity(w1.getModelVectors().get(model.getType()), w2.getModelVectors().get(model.getType())));
                }

                double sim = SemanticCohesion.getAggregatedSemanticMeasure(similarities);
                if (!w1.equals(w2) && sim >= threshold) {
                    visibleConcepts.put(w1, true);
                    visibleConcepts.put(w2, true);
                }
            }
        });

        for (Keyword t : topics) {
            if (visibleConcepts.get(t.getWord())) {
                nodes.add(new ResultNode(t.getWord().getLemma(), Formatting.formatNumber(t.getRelevance())));
            }
        }

        return nodes;
    }

    public AbstractDocument processQuery(String query, String pathToLSA, String pathToLDA, Lang lang,
            boolean posTagging, boolean computeDialogism, boolean useBigrams) {
        LOGGER.info("Processign query ...");
        AbstractDocumentTemplate contents = new AbstractDocumentTemplate();
        String[] blocks = query.split("\n");
        LOGGER.log(Level.INFO, "[Processing] There should be {0} blocks in the document", blocks.length);
        for (int i = 0; i < blocks.length; i++) {
            BlockTemplate block = contents.new BlockTemplate();
            block.setId(i);
            block.setContent(blocks[i]);
            contents.getBlocks().add(block);
        }

        List<ISemanticModel> models = new ArrayList<>();
        models.add(LSA.loadLSA(pathToLSA, lang));
        models.add(LDA.loadLDA(pathToLDA, lang));

        AbstractDocument queryDoc = new Document(null, contents, models, lang, posTagging);
        LOGGER.log(Level.INFO, "Built document has {0} blocks.", queryDoc.getBlocks().size());
        queryDoc.computeAll(computeDialogism, useBigrams);
        ComplexityIndices.computeComplexityFactors(queryDoc);

        return queryDoc;
    }

    class ResultNode implements Comparable<ResultNode> {

        private String name;
        private double value;

        public ResultNode(String name, double value) {
            super();
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getValue() {
            return value;
        }

        @Override
        public int compareTo(ResultNode o) {
            return (int) Math.signum(o.getValue() - this.getValue());
        }
    }
}
