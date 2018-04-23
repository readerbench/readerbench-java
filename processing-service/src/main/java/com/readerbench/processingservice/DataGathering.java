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
package com.readerbench.processingservice;

import com.readerbench.datasourceprovider.data.semanticmodels.ISemanticModel;
import com.readerbench.datasourceprovider.pojo.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class DataGathering {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataGathering.class);

//    public static void writeHeader(String path, Lang lang, boolean writeName) {
//        // create measurements.csv header
//        try (BufferedWriter out = new BufferedWriter(new FileWriter(path + "/" + new File(path).getName() + "-" + "measurements.csv", false))) {
//            StringBuilder concat = new StringBuilder();
//            concat.append("SEP=,\n");
//            if (writeName) {
//                concat.append("File name,Folder Name,Paragraphs,Sentences,Words,Content words");
//            } else {
//                concat.append("File name,Paragraphs,Sentences,Words,Content words");
//            }
//            ComplexityIndices.getIndices(lang).stream().forEach((factor) -> {
//                concat.append(",RB.").append(factor.getAcronym());
//            });
//            out.write(concat.toString());
//        } catch (Exception e) {
//            LOGGER.error("Runtime error while initializing measurements.csv file");
//            LOGGER.error(e.getMessage());
//        }
//    }

    public static void processTexts(String path, String folderName, boolean writeHeader,
                                    List<ISemanticModel> models, Lang lang, boolean usePOSTagging,
                                    boolean computeDialogism) throws IOException {
        processTexts(path, path, folderName, writeHeader, models, lang, usePOSTagging, computeDialogism);
    }

    public static void processTexts(String processingPath, String saveLocation,
            String folderName, boolean writeHeader, List<ISemanticModel> models,
            Lang lang, boolean usePOSTagging, boolean computeDialogism) throws IOException {
        //processTexts(processingPath, saveLocation, folderName, writeHeader, models, lang, usePOSTagging, computeDialogism, false, false);
    }

    public static void processMetaDocuments(String processingPath, List<ISemanticModel> models,
            Lang lang, boolean usePOSTagging, boolean computeDialogism) throws IOException {
        //processTexts(processingPath, processingPath, "", true, models, lang, usePOSTagging, computeDialogism, false, true);
    }

//    public static void processTexts(String processingPath, String saveLocation,
//            String folderName, boolean writeHeader, List<ISemanticModel> models, Lang lang,
//            boolean usePOSTagging, boolean computeDialogism, boolean useBigrams, boolean meta) throws IOException {
//        File dir = new File(processingPath);
//
//        if (!dir.exists()) {
//            throw new IOException("Inexistent Folder: " + dir.getPath());
//        }
//
//        File[] files = dir.listFiles((File pathname) -> {
//            return pathname.getName().toLowerCase().endsWith(".xml");
//        });
//
//        if (writeHeader & folderName.equals("")) {
//            writeHeader(saveLocation, lang, false);
//        }
//
//        for (File file : files) {
//            LOGGER.info("Processing {} file", file.getName());
//            // Create file
//
//            Document d = null;
//            try {
//                if (meta) {
//                    d = MetaDocument.load(file, models, lang, usePOSTagging, MetaDocument.DocumentLevel.Section, 5);
//                } else {
//                    d = Document.load(file, models, lang, usePOSTagging);
//                }
//                d.computeAll(computeDialogism, useBigrams);
//            } catch (Exception e) {
//                LOGGER.error("Runtime error while processing {}: {}", new Object[]{file.getName(), e.getMessage()});
//                LOGGER.error(e.getMessage());
//            }
//
//            if (d != null) {
//                try (BufferedWriter out = new BufferedWriter(new FileWriter(saveLocation + "/" + new File(saveLocation).getName() + "-" + "measurements.csv", true))) {
//                    StringBuilder concat = new StringBuilder();
//                    String fileName = FilenameUtils.removeExtension(file.getName().replaceAll(",", ""));
//                    concat.append("\n").append(fileName);
//                    if (!folderName.equals("")) {
//                        concat.append(",").append(folderName);
//                    };
//                    concat.append(",").append(d.getNoBlocks());
//                    concat.append(",").append(d.getNoSentences());
//                    concat.append(",").append(d.getNoWords());
//                    concat.append(",").append(d.getNoContentWords());
//                    for (ComplexityIndex factor : ComplexityIndices.getIndices(lang)) {
//                        concat.append(",").append(d.getComplexityIndices().get(factor));
//                    }
//                    out.write(concat.toString());
//                } catch (IOException ex) {
//                    LOGGER.error("Runtime error while initializing measurements.csv file");
//                    LOGGER.error(ex.getMessage());
//                    throw ex;
//                }
//            }
//        }
//    }
}
