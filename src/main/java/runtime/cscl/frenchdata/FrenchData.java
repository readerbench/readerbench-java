package runtime.cscl.frenchdata;

import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.Lang;
import data.document.Document;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.LoggerFactory;
import services.complexity.ComplexityIndex;
import services.semanticModels.ISemanticModel;
import services.semanticModels.SimilarityType;

import java.io.*;
import java.util.*;

/**
 * Created by Dorinela on 5/9/2017.
 */
public class FrenchData {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(FrenchData.class);

    private static String LSA_PATH = "resources/config/FR/LSA/Le_Monde";
    private static String LDA_PATH = "resources/config/FR/LDA/Le_Monde";
//    private static String WORD2VEC_PATH = "resources/config/FR/word2vec/Le_Monde";

    public static void main(String[] args) {

        FrenchData frenchData = new FrenchData();
        List<Doc> docs =  frenchData.getDocumentsByQuery("*:*");

        XSSFWorkbook workbook = null;
        try {
            XSSFSheet sheet;
            if (workbook == null) {
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet();
            }
            sheet = workbook.getSheetAt(0);

            for (int i = 0; i < 1; i++) {
                AbstractDocument abstractDocument = frenchData.loadDocument(docs.get(i).getContent());
                abstractDocument.computeAll(true);
                Map<ComplexityIndex, Double> complexityIndex = abstractDocument.getComplexityIndices();

                int rows = sheet.getPhysicalNumberOfRows();
                Row row = sheet.createRow(rows);

                Cell cell = row.createCell(0);
                cell.setCellValue(docs.get(i).getGame());

                cell = row.createCell(1);
                cell.setCellValue(docs.get(i).getScore());

                int start = 2;
                for (Map.Entry<ComplexityIndex, Double> entry : complexityIndex.entrySet())
                {
                    cell = row.createCell(start);
                    cell.setCellValue(entry.getValue());
                    start++;
                }
            }

            FileOutputStream outputStream = new FileOutputStream("D:\\Facultate\\MASTER\\french_reviews\\french_reviews.xlsx");
            workbook.write(outputStream);
            System.out.println("Finish to write all data in file !!!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception: " + e.getMessage());
        }

//        System.out.println("----------- Start --------");
//        for (int i = 0; i < 1; i++) {
//            AbstractDocument abstractDocument = frenchData.loadDocument(docs.get(i).getContent());
//            abstractDocument.computeAll(true);
//            Map<ComplexityIndex, Double> complexityIndex = abstractDocument.getComplexityIndices();
//            frenchData.writeFinalDataList(docs.get(i), complexityIndex);
//        }
//        System.out.println("----------- Finish --------");
    }

    public void writeFinalDataList(Doc doc, Map<ComplexityIndex, Double> complexityIndex) {
        XSSFWorkbook workbook = null;
        try {
            XSSFSheet sheet;
            if (workbook == null) {
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet();
            }
            sheet = workbook.getSheetAt(0);
            //for(FinalMessage finalMessage : finalMessages) {
                int rows = sheet.getPhysicalNumberOfRows();
                Row row = sheet.createRow(rows);

                Cell cell = row.createCell(0);
                cell.setCellValue(doc.getGame());

                cell = row.createCell(1);
                cell.setCellValue(doc.getScore());

                int start = 2;
                for (Map.Entry<ComplexityIndex, Double> entry : complexityIndex.entrySet())
                {
                    cell = row.createCell(start);
                    cell.setCellValue(entry.getValue());
                    start++;
                }
            //}
            FileOutputStream outputStream = new FileOutputStream("C:\\Users\\Administrator\\Desktop\\french_reviews\\french_reviews.xlsx");
            workbook.write(outputStream);
            System.out.println("Finish to write all data in file !!!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception: " + e.getMessage());
        }
    }

    public void writeFile(List<Doc> docs) throws IOException {
//        File fout = new File("D:\\Facultate\\MASTER\\french_reviews\\french_reviews.txt");
//        FileOutputStream fos = new FileOutputStream(fout);
//
//        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
//
//        for (Doc doc : docs) {
//            bw.write(doc.getContent().trim());
//        }
//        bw.close();

        PrintWriter pw = new PrintWriter(new FileWriter("D:\\Facultate\\MASTER\\french_reviews\\french_reviews.txt"));
        for (Doc doc : docs) {
            pw.write(doc.getContent().trim());
        }
        pw.close();

    }

    public AbstractDocument loadDocument(String text) {

        Map<SimilarityType, String> modelPaths = new EnumMap<>(SimilarityType.class);
        modelPaths.put(SimilarityType.LSA, LSA_PATH);
        modelPaths.put(SimilarityType.LDA, LDA_PATH);
//        modelPaths.put(SimilarityType.WORD2VEC, WORD2VEC_PATH);
        List<ISemanticModel> models = SimilarityType.loadVectorModels(modelPaths, Lang.fr);

        AbstractDocumentTemplate contents = AbstractDocumentTemplate.getDocumentModel(text);
        return new Document(contents, models, Lang.fr, true);
    }

    /**
     * Get documents from SOLR by a specific query
     * @param query - query
     * @return - SolrDocumentList
     */
    public List<Doc> getDocumentsByQuery(String query) {

        List<Doc> docs = new ArrayList<>();
        SolrClient solrClient = new HttpSolrClient.Builder("http://141.85.232.56:8983/solr/reviews_french").build();
        /**
         * Create SOLR Query
         */
        SolrQuery solrQuery = new SolrQuery().setRows(30000);
        solrQuery.set("q", query);

        try {
            QueryResponse response = solrClient.query(solrQuery);
            SolrDocumentList solrDocuments = response.getResults();
            LOGGER.info("Total documents: " + solrDocuments.getNumFound());

            /**
             * iterate over documents and process them
             */
            for (SolrDocument solrDocument : solrDocuments) {
                String game = solrDocument.getFieldValue("title").toString();
                Integer score = Integer.valueOf(solrDocument.getFieldValue("originalScore").toString());
                String content = solrDocument.getFieldValue("content").toString();

                Doc doc = new Doc(game, score, content);
                docs.add(doc);

            }

            return docs;

        } catch (Exception e) {
            LOGGER.error("Error in getting documents from SOLR: {}", e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

}
