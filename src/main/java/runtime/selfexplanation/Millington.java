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
package runtime.selfexplanation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import data.AbstractDocumentTemplate;
import data.discourse.SemanticCohesion;
import data.document.Document;
import data.document.Summary;
import data.Lang;
import data.document.ReadingStrategyType;
import org.openide.util.Exceptions;
import services.commons.Formatting;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndices;
import services.readingStrategies.ReadingStrategies;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.WordNet.SimilarityType;

public class Millington {

    static final Logger LOGGER = Logger.getLogger(Millington.class);

    private final String path;
    private final Map<String, String> loadedDocuments;
    private final LSA lsa;
    private final LDA lda;
    private final Lang lang;

    public Millington(String path) {
        this.path = path;
        this.loadedDocuments = new TreeMap<>();
        this.lang = Lang.eng;
        this.lsa = LSA.loadLSA("resources/config/EN/LSA/TASA", lang);
        this.lda = LDA.loadLDA("resources/config/EN/LDA/TASA", lang);
    }

    private String getTextFromFile(String path) {
        FileInputStream inputFile;
        InputStreamReader ir;
        String line;
        StringBuilder content = new StringBuilder("");
        try {
            inputFile = new FileInputStream(new File(path));
            ir = new InputStreamReader(inputFile, "UTF-8");
            try (BufferedReader in = new BufferedReader(ir)) {
                while ((line = in.readLine()) != null) {
                    if (line.trim().length() > 0) {
                        content.append(line.trim()).append("\n");
                    }
                }
            }
            return content.toString();
        } catch (FileNotFoundException e) {
            Exceptions.printStackTrace(e);
        } catch (UnsupportedEncodingException e) {
            Exceptions.printStackTrace(e);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }

    public void parseMillington(int maxNoRows) throws IOException {
        File myFile = new File(path + "/data.xlsx");
        FileInputStream fis = new FileInputStream(myFile);
        // Finds the workbook instance for XLSX file
        XSSFWorkbook myWorkBook = new XSSFWorkbook(fis);

        // Return first sheet from the XLSX workbook
        XSSFSheet mySheet = myWorkBook.getSheetAt(0);

        // Get iterator to all the rows in current sheet
        Iterator<Row> rowIterator = mySheet.iterator();

        int rowNo = 1;
        // ignore header line
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }

        // write header
        BufferedWriter out = new BufferedWriter(new FileWriter(path + "/output.csv", false));
        out.write("StudID,Attempt,Time to response,TextID,SentenceID,Score,Garbage,Frozen (binary),Vague/irrelevant,Repeat,Paraphrase,LocalBridging,Elaboration,Global");
        for (ReadingStrategyType rs : ReadingStrategyType.values()) {
            out.write("," + rs.getName());
        }
        for (String type : new String[]{"Previous", "Target", "Next", "Entire text"}) {
            for (String semDist : new String[]{"Wu-Palmer", "LSA", "LDA"}) {
                out.write("," + type + " (" + semDist + ")");
            }
        }
        List<ComplexityIndex> indices = ComplexityIndices.getIndices(Lang.eng);
        for (ComplexityIndex index : indices) {
            out.write("," + index.getAcronym());
        }
        out.write("\n");
        out.close();

        // Traversing over each row of XLSX file
        while (rowIterator.hasNext() && rowNo <= maxNoRows) {
            Row row = rowIterator.next();

            String studentID = row.getCell(22).getStringCellValue();

            int attempt = (int) row.getCell(2).getNumericCellValue();

            Double timeToResponse = row.getCell(3).getNumericCellValue();

            String fileName = row.getCell(5).getStringCellValue();

            String targetText = row.getCell(6).getStringCellValue();

            String studentSE = row.getCell(4).getStringCellValue();

            String content;

            int textID = (int) row.getCell(0).getNumericCellValue();

            int sentenceID = (int) row.getCell(1).getNumericCellValue();

            int finalScore = (int) row.getCell(7).getNumericCellValue();

            if (finalScore == 0) {
                continue;
            }

            int garbage = (int) row.getCell(14).getNumericCellValue();

            int frozen = (int) row.getCell(15).getNumericCellValue();

            int vague = (int) row.getCell(16).getNumericCellValue();

            int repeat = (int) row.getCell(17).getNumericCellValue();

            int paraphrase = (int) row.getCell(18).getNumericCellValue();

            int localBridging = (int) row.getCell(19).getNumericCellValue();

            int elaboration = (int) row.getCell(20).getNumericCellValue();

            int global = (int) row.getCell(21).getNumericCellValue();

            if (!loadedDocuments.containsKey(fileName)) {
                content = getTextFromFile(path + "/texts/" + fileName + ".txt");
                if (content != null) {
                    loadedDocuments.put(fileName, content);
                }
            } else {
                content = loadedDocuments.get(fileName);
            }

            if (!content.contains(targetText)) {
                LOGGER.error("Error processing row " + rowNo);
                continue;
            }

            String seRefText = content.substring(0, content.indexOf(targetText) + targetText.length());

            Document refDoc = new Document(null, AbstractDocumentTemplate.getDocumentModel(seRefText), lsa, lda, lang,
                    true, true);
            Summary se = new Summary(studentSE, refDoc, true, true);
            se.computeAll(true, false);
            List<SemanticCohesion> cohesionScores = new ArrayList<>();

            String prevText = content.substring(0, content.indexOf(targetText));
            Document prevDoc = new Document(null, AbstractDocumentTemplate.getDocumentModel(prevText), lsa, lda, lang,
                    true, true);
            cohesionScores.add(new SemanticCohesion(se, prevDoc));

            Document targetDoc = new Document(null, AbstractDocumentTemplate.getDocumentModel(targetText), lsa, lda,
                    lang, true, true);
            cohesionScores.add(new SemanticCohesion(se, targetDoc));

            String nextText = content.substring(content.indexOf(targetText) + targetText.length());
            Document nextDoc = new Document(null, AbstractDocumentTemplate.getDocumentModel(nextText), lsa, lda, lang,
                    true, true);
            cohesionScores.add(new SemanticCohesion(se, nextDoc));

            Document entireDoc = new Document(null, AbstractDocumentTemplate.getDocumentModel(content), lsa, lda, lang,
                    true, true);
            cohesionScores.add(new SemanticCohesion(se, entireDoc));

            out = new BufferedWriter(new FileWriter(path + "/output.csv", true));
            out.write(studentID + "," + attempt + "," + timeToResponse + "," + textID + "," + sentenceID);
            out.write("," + finalScore + "," + garbage + "," + frozen + "," + vague + "," + repeat + "," + paraphrase
                    + "," + localBridging + "," + elaboration + "," + global);
            for (ReadingStrategyType rs : ReadingStrategyType.values()) {
                out.write("," + se.getAutomatedRS().get(0).get(rs));
            }
            for (SemanticCohesion cohesionScore : cohesionScores) {
                out.write("," + Formatting.formatNumber(cohesionScore.getSemanticSimilarities().get(SimilarityType.WU_PALMER)) + ","
                        + Formatting.formatNumber(cohesionScore.getSemanticSimilarities().get(SimilarityType.LSA)) + ","
                        + Formatting.formatNumber(cohesionScore.getSemanticSimilarities().get(SimilarityType.LDA)));
            }
            for (ComplexityIndex index : indices) {
                out.write("," + se.getComplexityIndices().get(index));
            }
            out.write("\n");

            out.close();

            if (rowNo % 10 == 0) {
                LOGGER.info("Finished processing " + rowNo + " rows...");
            }
            rowNo++;
        }
        LOGGER.info("Finished processing all rows...");
    }

    public static void main(String[] args) {
        try {
            Millington m = new Millington("resources/in/Millington");
            m.parseMillington(50);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }
}
