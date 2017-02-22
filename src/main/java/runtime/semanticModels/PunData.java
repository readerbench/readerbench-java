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
package runtime.semanticModels;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import data.AbstractDocumentTemplate;
import data.document.Document;
import data.Lang;
import data.Word;
import data.discourse.Keyword;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openide.util.Exceptions;
import services.commons.Formatting;
import services.discourse.keywordMining.KeywordModeling;
import services.nlp.lemmatizer.StaticLemmatizer;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.WordNet.OntologySupport;
import services.semanticModels.word2vec.Word2VecModel;
import webService.ReaderBenchServer;

public class PunData {

    static final Logger LOGGER = Logger.getLogger("");

    public static List<Keyword> topSimilar(TreeMap<Word, Double> similarConcepts, Document d, int noKeywords) {
        List<Keyword> similar = new ArrayList<>();
        similarConcepts.entrySet().stream().forEach((entry) -> {
            if (!d.getWordOccurences().keySet().contains(entry.getKey())) {
                similar.add(new Keyword(entry.getKey(), entry.getValue()));
            }
        });
        Collections.sort(similar);
        return KeywordModeling.getSublist(similar, noKeywords, false, false);
    }

    public static void writeSimilar(String input, ISemanticModel semModel, BufferedWriter out) throws IOException {
        List<ISemanticModel> models = new ArrayList<>();
        models.add(semModel);
        Document pun_homograph = new Document(null, AbstractDocumentTemplate.getDocumentModel(input), models, semModel.getLanguage(), true);
        TreeMap<Word, Double> similarConcepts = semModel.getSimilarConcepts(pun_homograph, 0.3);
        List<Keyword> similar = topSimilar(similarConcepts, pun_homograph, 10);
        out.write(input + ":");
        for (Keyword k : similar) {
            out.write("," + k.getWord().getLemma() + "," + Formatting.formatNumber(k.getRelevance()));
        }
        out.write("\n");
    }

    public void comparePuns(String pathToFile, ISemanticModel semModel) {
        try (FileInputStream fis = new FileInputStream(new File(pathToFile));
                BufferedWriter out = new BufferedWriter(new FileWriter(pathToFile.replace(".xlsx", "_" + semModel.getClass().getSimpleName() + "_" + (new File(semModel.getPath()).getName()) + ".csv"), false))) {
            // Finds the workbook instance for XLSX file
            XSSFWorkbook myWorkBook = new XSSFWorkbook(fis);
            // Return first sheet from the XLSX workbook
            XSSFSheet mySheet = myWorkBook.getSheetAt(0);
            // Get iterator to all the rows in current sheet
            Iterator<Row> rowIterator = mySheet.iterator();
            // write header
//            out.write("pun word - pun input" + ","
//                    + "nonpun word - nonpun input" + ","
//                    + "pun word - homograph" + ","
//                    + "nonpun word - homograph" + ","
//                    + "pun word - dom" + ","
//                    + "pun word - sub" + ","
//                    + "pun word - unrelated" + ","
//                    + "nonpun word - dom" + ","
//                    + "nonpun word - sub" + ","
//                    + "nonpun word - unrelated" + ","
//                    + "homograph - dom" + ","
//                    + "homograph - sub" + ","
//                    + "homograph - unrelated" + "\n");

            // ignore header line
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            List<ISemanticModel> models = new ArrayList<>();
            models.add(semModel);
            // Traversing over each row of XLSX file
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

//                Document punInput = new Document(null, AbstractDocumentTemplate.getDocumentModel(row.getCell(0).getStringCellValue()), models, semModel.getLanguage(), true);
//                Document punWord = new Document(null, AbstractDocumentTemplate.getDocumentModel(row.getCell(1).getStringCellValue()), models, semModel.getLanguage(), true);
//
//                Document nonPunInput = new Document(null, AbstractDocumentTemplate.getDocumentModel(row.getCell(2).getStringCellValue()), models, semModel.getLanguage(), true);
//                Document nonPunWord = new Document(null, AbstractDocumentTemplate.getDocumentModel(row.getCell(3).getStringCellValue()), models, semModel.getLanguage(), true);
//
//                Document homograph = new Document(null, AbstractDocumentTemplate.getDocumentModel(row.getCell(4).getStringCellValue()), models, semModel.getLanguage(), true);
//                Document dom = new Document(null, AbstractDocumentTemplate.getDocumentModel(row.getCell(5).getStringCellValue()), models, semModel.getLanguage(), true);
//                Document sub = new Document(null, AbstractDocumentTemplate.getDocumentModel(row.getCell(6).getStringCellValue()), models, semModel.getLanguage(), true);
//                Document unrelated = new Document(null, AbstractDocumentTemplate.getDocumentModel(row.getCell(7).getStringCellValue()), models, semModel.getLanguage(), true);
                writeSimilar(row.getCell(1).getStringCellValue() + " " + row.getCell(4).getStringCellValue(), semModel, out);
                writeSimilar(row.getCell(3).getStringCellValue() + " " + row.getCell(4).getStringCellValue(), semModel, out);

                String homograph = row.getCell(4).getStringCellValue();
                Document homographDoc = new Document(null, AbstractDocumentTemplate.getDocumentModel(homograph), models, semModel.getLanguage(), true);
                TreeMap<Word, Double> similarConcepts = OntologySupport.getSimilarConcepts(homographDoc.getWordOccurences().keySet().toArray(new Word[0])[0]);

                out.write(row.getCell(4).getStringCellValue() + ":");
                for (Word w : similarConcepts.keySet()) {
                    if (!w.getLemma().equals(StaticLemmatizer.lemmaStatic(homograph, semModel.getLanguage()))) {
                        out.write("," + w.getLemma());
                    }
                }
                out.write("\n\n");
//                out.write(Formatting.formatNumber(semModel.getSimilarity(punWord, punInput)) + ","
//                        + Formatting.formatNumber(semModel.getSimilarity(nonPunWord, nonPunInput)) + ","
//                        + Formatting.formatNumber(semModel.getSimilarity(punWord, homograph)) + ","
//                        + Formatting.formatNumber(semModel.getSimilarity(nonPunWord, homograph)) + ","
//                        + Formatting.formatNumber(semModel.getSimilarity(punWord, dom)) + ","
//                        + Formatting.formatNumber(semModel.getSimilarity(punWord, sub)) + ","
//                        + Formatting.formatNumber(semModel.getSimilarity(punWord, unrelated)) + ","
//                        + Formatting.formatNumber(semModel.getSimilarity(nonPunWord, dom)) + ","
//                        + Formatting.formatNumber(semModel.getSimilarity(nonPunWord, sub)) + ","
//                        + Formatting.formatNumber(semModel.getSimilarity(nonPunWord, unrelated)) + ","
//                        + Formatting.formatNumber(semModel.getSimilarity(homograph, dom)) + ","
//                        + Formatting.formatNumber(semModel.getSimilarity(homograph, sub)) + ","
//                        + Formatting.formatNumber(semModel.getSimilarity(homograph, unrelated)) + "\n");
            }

            out.close();
            LOGGER.info("Finished processing all rows...");
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static void main(String[] args) {
        ReaderBenchServer.initializeDB();

        PunData comp = new PunData();
        String pathToFile = "resources/in/pun data/pun data v2.xlsx";
//        comp.comparePuns(pathToFile, LSA.loadLSA("resources/config/EN/LSA/TASA", Lang.en));
        comp.comparePuns(pathToFile, LSA.loadLSA("resources/config/EN/LSA/COCA_newspaper", Lang.en));
//
//        comp.comparePuns(pathToFile, LDA.loadLDA("resources/config/EN/LDA/TASA", Lang.en));
        comp.comparePuns(pathToFile, LDA.loadLDA("resources/config/EN/LDA/COCA_newspaper", Lang.en));

        comp.comparePuns(pathToFile, Word2VecModel.loadWord2Vec("resources/config/EN/word2vec/COCA_newspaper", Lang.en));
//        comp.comparePuns(pathToFile, Word2VecModel.loadWord2Vec("resources/config/EN/word2vec/TASA_epoch3_iter3", Lang.en));
        comp.comparePuns(pathToFile, Word2VecModel.loadGoogleNewsModel());
    }
}
