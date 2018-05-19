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
package com.readerbench.processingservice.document;

import com.readerbench.coreservices.data.document.Document;
import com.readerbench.coreservices.data.document.MetaDocument;
import com.readerbench.coreservices.semanticmodels.SemanticModel;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.processingservice.Annotators;
import com.readerbench.processingservice.importdata.Txt2XmlConverter;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndices;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FilenameUtils;

public class ProcessDocumentCollection {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessDocumentCollection.class);

    public void writeHeader(String path, Lang lang) {
        // create measurements.csv header
        try (BufferedWriter out = new BufferedWriter(new FileWriter(path + "/" + new File(path).getName() + "-" + "measurements.csv", false))) {
            StringBuilder concat = new StringBuilder();
            concat.append("SEP=,\n");
            concat.append("File name,Paragraphs,Sentences,Words,Content words");
            ComplexityIndices.getIndices(lang).stream().forEach((factor) -> {
                concat.append(",RB.").append(factor.getAcronym());
            });
            out.write(concat.toString());
        } catch (Exception e) {
            LOGGER.error("Runtime error while initializing measurements.csv file");
            LOGGER.error(e.getMessage());
        }
    }

    public void processTexts(String processingPath, List<SemanticModel> models, Lang lang, boolean meta) {
        File dir = new File(processingPath);

        if (!dir.exists()) {
            return;
        }

        File[] files = dir.listFiles((File pathname) -> {
            return pathname.getName().toLowerCase().endsWith(".xml");
        });

        writeHeader(processingPath, lang);

        List<Annotators> annotators = Arrays.asList(Annotators.NLP_PREPROCESSING, Annotators.DIALOGISM, Annotators.TEXTUAL_COMPLEXITY);

        DocumentProcessingPipeline pipelineDoc = new DocumentProcessingPipeline(lang, models, annotators);
        MetaDocumentProcessingPipeline pipelineMetaDoc = new MetaDocumentProcessingPipeline(lang, models, annotators);
        for (File file : files) {
            LOGGER.info("Processing {} file", file.getName());

            Document d = null;
            try {
                if (meta) {
                    d = pipelineMetaDoc.createMetaDocumentFromXML(file.getPath(), MetaDocument.DocumentLevel.Section, 5);
                    pipelineDoc.processDocument(d);
                } else {
                    d = pipelineDoc.createDocumentFromXML(file.getPath());
                    pipelineDoc.processDocument(d);
                }
            } catch (Exception e) {
                LOGGER.error("Runtime error while processing {}: {}", new Object[]{file.getName(), e.getMessage()});
            }

            if (d != null) {
                try (BufferedWriter out = new BufferedWriter(new FileWriter(processingPath + "/" + new File(processingPath).getName() + "-" + "measurements.csv", true))) {
                    StringBuilder concat = new StringBuilder();
                    String fileName = FilenameUtils.removeExtension(file.getName().replaceAll(",", ""));
                    concat.append("\n").append(fileName);
                    concat.append(",").append(d.getNoBlocks());
                    concat.append(",").append(d.getNoSentences());
                    concat.append(",").append(d.getNoWords());
                    concat.append(",").append(d.getNoContentWords());
                    for (ComplexityIndex factor : ComplexityIndices.getIndices(lang)) {
                        concat.append(",").append(d.getComplexityIndices().get(factor));
                    }
                    out.write(concat.toString());
                } catch (IOException ex) {
                    LOGGER.error("Runtime error while initializing measurements.csv file");
                }
            }
        }
    }

    public static void main(String[] args) {
        Lang lang = Lang.en;
        List<SemanticModel> models = SemanticModel.loadModels("tasa", lang);
        String path = "C:\\ReaderBench\\ReaderBench\\resources\\in\\essays\\all essays";

        Txt2XmlConverter converter = new Txt2XmlConverter(lang);
        converter.parseTxtFiles(path, lang, "UTF-8", false);

        ProcessDocumentCollection processing = new ProcessDocumentCollection();
        processing.processTexts(path, models, lang, false);
    }
}
