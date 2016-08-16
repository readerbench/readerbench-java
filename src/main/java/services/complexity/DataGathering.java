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
package services.complexity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import data.AbstractDocument.SaveType;
import data.Lang;
import data.complexity.Measurement;
import data.document.Document;
import data.document.MetaDocument;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;

public class DataGathering {

    static Logger logger = Logger.getLogger(DataGathering.class);

    public static final int MAX_PROCESSED_FILES = 10000;

    public static void writeHeader(String path) {
        // create measurements.csv header
        try {
            FileWriter fstream = new FileWriter(path + "/measurements.csv", false);
            BufferedWriter out = new BufferedWriter(fstream);
            StringBuffer concat = new StringBuffer();
            concat.append("Grade Level,File name,Genre,Complexity,Paragraphs,Sentences,Words,Content words");
            for (int i = 0; i < ComplexityIndices.NO_COMPLEXITY_INDICES; i++) {
                concat.append(",").append(ComplexityIndices.TEXTUAL_COMPLEXITY_INDEX_ACRONYMS[i]);
            }
            out.write(concat.toString());
            out.close();
        } catch (Exception e) {
            logger.error("Runtime error while initializing measurements.csv file");
            e.printStackTrace();
        }
    }

    public static void processTexts(String path, int gradeLevel, boolean writeHeader, LSA lsa, LDA lda, Lang lang,
            boolean usePOSTagging, boolean computeDialogism) throws IOException {
        processTexts(path, path, gradeLevel, writeHeader, lsa, lda, lang, usePOSTagging, computeDialogism);
    }

    public static void processTexts(String processingPath, String saveLocation, int gradeLevel, boolean writeHeader,
            LSA lsa, LDA lda, Lang lang, boolean usePOSTagging, boolean computeDialogism) throws IOException {
        processTexts(processingPath, saveLocation, gradeLevel, writeHeader, lsa, lda, lang, usePOSTagging, computeDialogism, false);
    }

    public static void processTexts(String processingPath, String saveLocation, int gradeLevel, boolean writeHeader,
            LSA lsa, LDA lda, Lang lang, boolean usePOSTagging, boolean computeDialogism, boolean meta) throws IOException {
        File dir = new File(processingPath);

        if (!dir.exists()) {
            throw new IOException("Inexistent Folder: " + dir.getPath());
        }

        File[] files = dir.listFiles((File pathname) -> {
            return pathname.getName().toLowerCase().endsWith(".xml");
        });

        if (writeHeader) {
            writeHeader(saveLocation);
        }

        int noProcessedFiles = 0;
        for (File file : files) {
            logger.info("Processing " + file.getName() + " file");
            // Create file

            Document d = null;
            try {
                if (meta) {
                    d = MetaDocument.load(file, lsa, lda, lang, usePOSTagging, true, MetaDocument.DocumentLevel.Subsection, 5);
                } else {
                    d = Document.load(file, lsa, lda, lang, usePOSTagging, true);
                }
                d.computeAll(computeDialogism, null, null);
            } catch (Exception e) {
                logger.error("Runtime error while processing " + file.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }

            if (d != null) {
                try {
                    FileWriter fstream = new FileWriter(saveLocation + "/measurements.csv", true);
                    BufferedWriter out = new BufferedWriter(fstream);
                    StringBuilder concat = new StringBuilder();

                    concat.append("\n").append(gradeLevel).append(",").append(file.getName().replaceAll(",", ""))
                            .append(",").append((d.getGenre() != null ? d.getGenre().trim() : "")).append(",")
                            .append((d.getComplexityLevel() != null ? d.getComplexityLevel().trim() : ""));
                    concat.append(",").append(d.getNoBlocks());
                    concat.append(",").append(d.getNoSentences());
                    concat.append(",").append(d.getNoWords());
                    concat.append(",").append(d.getNoContentWords());
                    for (int i = 0; i < ComplexityIndices.NO_COMPLEXITY_INDICES; i++) {
                        concat.append(",").append(d.getComplexityIndices()[i]);
                    }
                    out.write(concat.toString());
                    out.close();
                } catch (IOException e) {
                    logger.error("Runtime error while initializing measurements.csv file");
                    e.printStackTrace();
                    throw e;
                }
            }

            noProcessedFiles++;
            if (noProcessedFiles >= MAX_PROCESSED_FILES) {
                break;
            }
        }
    }

    public static Map<Double, List<Measurement>> getMeasurements(String fileName) {
        Map<Double, List<Measurement>> result = new TreeMap<>();

        try {
            BufferedReader input = new BufferedReader(new FileReader(fileName));
            try {
                // disregard first line
                String line = input.readLine();
                while ((line = input.readLine()) != null) {
                    String[] fields = line.split("[;,]");
                    double[] values = new double[fields.length - 4];

                    double classNumber = Double.parseDouble(fields[0]);
                    for (int i = 4; i < fields.length; i++) {
                        values[i - 4] = Double.parseDouble(fields[i]);
                    }
                    if (!result.containsKey(classNumber)) {
                        result.put(classNumber, new ArrayList<>());
                    }
                    result.get(classNumber).add(new Measurement(classNumber, values));
                }
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return result;
    }
}
