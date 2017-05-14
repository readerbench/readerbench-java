package runtime.reviews;

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
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.solr.client.solrj.SolrServerException;
import org.openide.util.Exceptions;
import services.complexity.ComplexityIndexType;
import webService.ReaderBenchServer;

/**
 * Created by Dorinela on 5/9/2017.
 */
public class FrenchData {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(FrenchData.class);

    private static String LSA_PATH = "resources/config/FR/LSA/Le_Monde";
    private static String LDA_PATH = "resources/config/FR/LDA/Le_Monde";
    private static String WORD2VEC_PATH = "resources/config/FR/word2vec/Le_Monde";

    public static void main(String[] args) {
        ReaderBenchServer.initializeDB();

        LOGGER.info("Retrieving reviews ... ");
        FrenchData frenchData = new FrenchData();
        List<Doc> docs = frenchData.getDocumentsByQuery("*:*");
        LOGGER.info("Finished retrieving " + docs.size() + " documents");

        Map<SimilarityType, String> modelPaths = new EnumMap<>(SimilarityType.class);
        modelPaths.put(SimilarityType.LSA, LSA_PATH);
        modelPaths.put(SimilarityType.LDA, LDA_PATH);
        //modelPaths.put(SimilarityType.WORD2VEC, WORD2VEC_PATH);
        List<ISemanticModel> models = SimilarityType.loadVectorModels(modelPaths, Lang.fr);

        XSSFWorkbook workbook = null;
        try {
            XSSFSheet sheet;
            if (workbook == null) {
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet();
            } else {
                sheet = workbook.getSheetAt(0);
            }

            for (int i = 0; i < docs.size(); i++) {
                LOGGER.info("Starting the processing of review " + i);
                AbstractDocument abstractDocument = frenchData.loadDocument(docs.get(i).getContent(), models);
                LOGGER.info("Finished creating RB internal reprezentation ...");

                Map<ComplexityIndex, Double> complexityIndex = Arrays.stream(ComplexityIndexType.values()).parallel()
                        .filter(t -> t.equals(ComplexityIndexType.WORD_LISTS))
                        .map(cat -> cat.getFactory())
                        .flatMap(f -> f.build(abstractDocument.getLanguage()).stream())
                        .collect(Collectors.toMap(Function.identity(), f -> f.compute(abstractDocument)));

                Map<String, Double> complexityIndexValue = new HashMap<>();
                for (Map.Entry<ComplexityIndex, Double> entry : complexityIndex.entrySet()) {
                    complexityIndexValue.put(entry.getKey().getAcronym(), entry.getValue());
                }

                Map<String, Double> sortMap = new LinkedHashMap<>();

                complexityIndexValue.entrySet().stream()
                        .sorted(Map.Entry.<String, Double>comparingByKey())
                        .forEachOrdered(x -> sortMap.put(x.getKey(), x.getValue()));

                int rows = sheet.getPhysicalNumberOfRows();
                Row row = sheet.createRow(rows);

                Cell cell = row.createCell(0);
                cell.setCellValue(docs.get(i).getGame());

                cell = row.createCell(1);
                cell.setCellValue(docs.get(i).getScore());

                int start = 2;
                for (Map.Entry<String, Double> entry : sortMap.entrySet()) {
                    //System.out.print(entry.getKey() + ",");
                    cell = row.createCell(start);
                    cell.setCellValue(entry.getValue());
                    start++;
                }
            }

            FileOutputStream outputStream = new FileOutputStream("resources/french_reviews.xlsx");
            workbook.write(outputStream);
            System.out.println("Finish to write all data in file !!!");
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
            LOGGER.error("Exception: " + e.getMessage());
        }
    }

    public AbstractDocument loadDocument(String text, List<ISemanticModel> models) {
        AbstractDocumentTemplate contents = AbstractDocumentTemplate.getDocumentModel(text);
        return new Document(contents, models, Lang.fr, true);
    }

    /**
     * Get documents from SOLR by a specific query
     *
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
        } catch (IOException | NumberFormatException | SolrServerException e) {
            LOGGER.error("Error in getting documents from SOLR: {}", e.getMessage());
            Exceptions.printStackTrace(e);
        }
        return new ArrayList<>();
    }

}
