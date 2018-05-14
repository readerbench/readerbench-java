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
package com.readerbench.processingservice.common;

import com.readerbench.coreservices.data.document.Document;
import com.readerbench.coreservices.data.document.MetaDocument;
import com.readerbench.coreservices.semanticmodels.SemanticModel;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.processingservice.Annotators;
import com.readerbench.processingservice.document.DocumentProcessingPipeline;
import com.readerbench.processingservice.document.MetaDocumentProcessingPipeline;
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

public class DataGathering {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataGathering.class);

    public static void writeHeader(String path, Lang lang, boolean writeName) {
        // create measurements.csv header
        try (BufferedWriter out = new BufferedWriter(new FileWriter(path + "/" + new File(path).getName() + "-" + "measurements.csv", false))) {
            StringBuilder concat = new StringBuilder();
            concat.append("SEP=,\n");
            if (writeName) {
                concat.append("File name,Folder Name,Paragraphs,Sentences,Words,Content words");
            } else {
                concat.append("File name,Paragraphs,Sentences,Words,Content words");
            }
            ComplexityIndices.getIndices(lang).stream().forEach((factor) -> {
                concat.append(",RB.").append(factor.getAcronym());
            });
            out.write(concat.toString());
        } catch (Exception e) {
            LOGGER.error("Runtime error while initializing measurements.csv file");
            LOGGER.error(e.getMessage());
        }
    }

    public static void processTexts(String path, String folderName, boolean writeHeader, List<SemanticModel> models, Lang lang) throws IOException {
        processTexts(path, path, folderName, writeHeader, models, lang);
    }

    public static void processTexts(String processingPath, String saveLocation, String folderName, boolean writeHeader, List<SemanticModel> models, Lang lang) throws IOException {
        processTexts(processingPath, saveLocation, folderName, writeHeader, models, lang, false);
    }

    public static void processMetaDocuments(String processingPath, List<SemanticModel> models, Lang lang) throws IOException {
        processTexts(processingPath, processingPath, "", true, models, lang, true);
    }

    public static void processTexts(String processingPath, String saveLocation, String folderName, boolean writeHeader, List<SemanticModel> models, Lang lang, boolean meta) throws IOException {
        File dir = new File(processingPath);

        if (!dir.exists()) {
            throw new IOException("Inexistent Folder: " + dir.getPath());
        }

        File[] files = dir.listFiles((File pathname) -> {
            return pathname.getName().toLowerCase().endsWith(".xml");
        });

        if (writeHeader & folderName.equals("")) {
            writeHeader(saveLocation, lang, false);
        }
        List<Annotators> annotators = Arrays.asList(Annotators.NLP_PREPROCESSING, Annotators.DIALOGISM, Annotators.TEXTUAL_COMPLEXITY);

        DocumentProcessingPipeline pipelineDoc = new DocumentProcessingPipeline(lang, models, annotators);
        MetaDocumentProcessingPipeline pipelineMetaDoc = new MetaDocumentProcessingPipeline(lang, models, annotators);
        for (File file : files) {
            LOGGER.info("Processing {} file", file.getName());
            // Create file

            Document d = null;
            try {
                if (meta) {
                    d = pipelineMetaDoc.createMetaDocumentFromXML(file.getPath(), MetaDocument.DocumentLevel.Section, 5);
                } else {
                    d = pipelineDoc.createDocumentFromXML(file.getPath());
                }
            } catch (Exception e) {
                LOGGER.error("Runtime error while processing {}: {}", new Object[]{file.getName(), e.getMessage()});
                LOGGER.error(e.getMessage());
            }

            if (d != null) {
                try (BufferedWriter out = new BufferedWriter(new FileWriter(saveLocation + "/" + new File(saveLocation).getName() + "-" + "measurements.csv", true))) {
                    StringBuilder concat = new StringBuilder();
                    String fileName = FilenameUtils.removeExtension(file.getName().replaceAll(",", ""));
                    concat.append("\n").append(fileName);
                    if (!folderName.equals("")) {
                        concat.append(",").append(folderName);
                    };
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
                    LOGGER.error(ex.getMessage());
                    throw ex;
                }
            }
        }
    }
}
