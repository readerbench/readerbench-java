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
package webService;

import com.sun.jersey.api.client.ClientResponse;
import dao.CategoryDAO;
import dao.WordDAO;
import data.AbstractDocument;
import data.AbstractDocument.SaveType;
import data.AbstractDocumentTemplate;
import data.Block;
import data.Lang;
import data.Word;
import data.cscl.Community;
import data.cscl.Conversation;
import data.discourse.SemanticCohesion;
import data.document.Document;
import data.document.ReadingStrategyType;
import data.document.Summary;
import data.pojo.Category;
import data.pojo.CategoryPhrase;
import data.sentiment.SentimentWeights;
import java.awt.Color;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.servlet.MultipartConfigElement;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openide.util.Exceptions;
import services.commons.Formatting;
import services.converters.PdfToTextConverter;
import services.mail.SendMail;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import spark.Spark;
import webService.cv.CVHelper;
import webService.keywords.KeywordsHelper;
import webService.query.QueryHelper;
import webService.queryResult.QueryResultCscl;
import webService.queryResult.QueryResultCv;
import webService.queryResult.QueryResultCvCover;
import webService.queryResult.QueryResultMailgun;
import webService.queryResult.QueryResultSearch;
import webService.queryResult.QueryResultSelfExplanation;
import webService.queryResult.QueryResultSemanticAnnotation;
import webService.queryResult.QueryResultSentiment;
import webService.queryResult.QueryResultTextCategorization;
import webService.queryResult.QueryResultTextualComplexity;
import webService.queryResult.QueryResultTopic;
import webService.queryResult.QueryResultvCoP;
import webService.result.ResultCategory;
import webService.result.ResultCv;
import webService.result.ResultCvCover;
import webService.result.ResultCvOrCover;
import webService.result.ResultKeyword;
import webService.result.ResultPdfToText;
import webService.result.ResultReadingStrategy;
import webService.result.ResultSelfExplanation;
import webService.result.ResultSemanticAnnotation;
import webService.result.ResultTextCategorization;
import webService.result.ResultTopic;
import webService.result.ResultvCoP;
import webService.semanticSearch.SearchClient;
import webService.services.ConceptMap;
import webService.services.TextualComplexity;
import webService.services.cscl.CSCL;
import webService.services.utils.FileProcessor;
import webService.services.vCoP.CommunityInteraction;

public class ReaderBenchServer {

    private static final Logger LOGGER = Logger.getLogger(ReaderBenchServer.class);
    public static final int PORT = 8080;

    public static final Color COLOR_TOPIC = new Color(204, 204, 204); // silver
    public static final Color COLOR_INFERRED_CONCEPT = new Color(102, 102, 255); // orchid

    private static List<AbstractDocument> loadedDocs;
    private static String loadedPath;

    public List<ResultCategory> getCategories(String documentContent, String pathToLSA, String pathToLDA, Lang lang,
            boolean usePOSTagging, boolean computeDialogism, double threshold) {

        List<ResultCategory> resultCategories = new ArrayList<>();

        AbstractDocument queryDoc = QueryHelper.processQuery(documentContent, pathToLSA, pathToLDA, lang, usePOSTagging,
                computeDialogism);
        List<Category> dbCategories = CategoryDAO.getInstance().findAll();

        for (Category cat : dbCategories) {
            List<CategoryPhrase> categoryPhrases = cat.getCategoryPhraseList();
            StringBuilder sb = new StringBuilder();
            for (CategoryPhrase categoryPhrase : categoryPhrases) {
                sb.append(categoryPhrase.getLabel());
                sb.append(", ");
            }
        }

        for (Category cat : dbCategories) {
            List<CategoryPhrase> categoryPhrases = cat.getCategoryPhraseList();
            StringBuilder sb = new StringBuilder();
            for (CategoryPhrase categoryPhrase : categoryPhrases) {
                sb.append(categoryPhrase.getLabel());
                sb.append(" ");
            }

            AbstractDocument queryCategory = QueryHelper.processQuery(sb.toString(), pathToLSA, pathToLDA, lang,
                    usePOSTagging, computeDialogism);
            SemanticCohesion sc = new SemanticCohesion(queryCategory, queryDoc);
            resultCategories.add(new ResultCategory(cat.getLabel(), Formatting.formatNumber(sc.getCohesion())));
        }

        Collections.sort(resultCategories, ResultCategory.ResultCategoryRelevanceComparator);
        return resultCategories;
    }

    private ResultSemanticAnnotation getSemanticAnnotation(AbstractDocument abstractDocument,
            AbstractDocument keywordsDocument, AbstractDocument document, Set<String> keywordsList, String pathToLSA,
            String pathToLDA, Lang lang, boolean usePOSTagging, boolean computeDialogism, double threshold) {

        // concepts
        ResultTopic resultTopic = ConceptMap.getTopics(document, threshold, null);
        List<ResultKeyword> resultKeywords = KeywordsHelper.getKeywords(document, keywordsDocument, keywordsList,
                pathToLSA, pathToLDA, lang, usePOSTagging, computeDialogism, threshold);
        List<ResultCategory> resultCategories = getCategories(document.getText(), pathToLSA, pathToLDA, lang,
                usePOSTagging, computeDialogism, threshold);

        // (abstract, document) relevance
        SemanticCohesion scAbstractDocument = new SemanticCohesion(abstractDocument, document);

        // (abstract, keywords) relevance
        SemanticCohesion scKeywordsAbstract = new SemanticCohesion(abstractDocument, keywordsDocument);

        // (keywords, document) relevance
        SemanticCohesion scKeywordsDocument = new SemanticCohesion(keywordsDocument, document);

        ResultSemanticAnnotation rsa = new ResultSemanticAnnotation(resultTopic,
                Formatting.formatNumber(scAbstractDocument.getCohesion()),
                Formatting.formatNumber(scKeywordsAbstract.getCohesion()),
                Formatting.formatNumber(scKeywordsDocument.getCohesion()), resultKeywords, resultCategories);

        return rsa;
    }

    private ResultSelfExplanation getSelfExplanation(String initialText, String selfExplanation, String pathToLSA,
            String pathToLDA, Lang lang, boolean usePOSTagging, boolean computeDialogism) {

        Document queryInitialText = new Document(null, AbstractDocumentTemplate.getDocumentModel(initialText),
                LSA.loadLSA(pathToLSA, lang), LDA.loadLDA(pathToLDA, lang), lang, usePOSTagging, false);

        Summary s = new Summary(selfExplanation, queryInitialText, true, true);

        s.computeAll(computeDialogism, false);

        List<ResultReadingStrategy> readingStrategies = new ArrayList<>();
        for (ReadingStrategyType rs : ReadingStrategyType.values()) {
            readingStrategies.add(new ResultReadingStrategy(rs.getName(), s.getAutomatedRS().get(0).get(rs)));
        }

        StringBuilder summary = new StringBuilder();
        for (Block b : s.getBlocks()) {
            LOGGER.info("Block alternate text: " + b.getAlternateText());
            summary.append(b.getAlternateText());
            summary.append("<br/>");
        }

        summary.append(s.getAlternateText());

        return new ResultSelfExplanation(summary.toString(), readingStrategies);
    }

    private ResultPdfToText getTextFromPdf(String uri, boolean localFile) {
        PdfToTextConverter pdfConverter = new PdfToTextConverter();
        if (localFile) {
            // return new
            // ResultPdfToText(PdfToTextConverter.pdftoText("resources/papers/"
            // + uri + ".pdf", true));
            return new ResultPdfToText(pdfConverter.pdftoText(uri, true));
        } else {
            return new ResultPdfToText(pdfConverter.pdftoText(uri, false));
        }
    }

    public void start() {
        Spark.port(PORT);

        Spark.staticFileLocation("/public");

        Spark.get("/", (request, response) -> {
            return "OK";
        });
        Spark.before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Request-Method", "*");
            response.header("Access-Control-Allow-Headers", "*");
        });
        Spark.get("/getTopics", (request, response) -> {
            response.type("application/json");

            String text = request.queryParams("text");
            String language = request.queryParams("lang");
            String pathToLSA = request.queryParams("lsa");
            String pathToLDA = request.queryParams("lda");
            boolean usePOSTagging = Boolean.parseBoolean(request.queryParams("postagging"));
            boolean computeDialogism = Boolean.parseBoolean(request.queryParams("dialogism"));
            double threshold = Double.parseDouble(request.queryParams("threshold"));

            QueryResultTopic queryResult = new QueryResultTopic();
            queryResult.setData(ConceptMap.getTopics(QueryHelper.processQuery(text, pathToLSA, pathToLDA,
                    Lang.getLang(language), usePOSTagging, computeDialogism), threshold, null));
            String result = queryResult.convertToJson();
            // return Charset.forName("UTF-8").encode(result);
            return result;
        });
        Spark.get("/getSentiment", (request, response) -> {
            response.type("application/json");

            String text = request.queryParams("text");
            String language = request.queryParams("lang");
            String pathToLSA = request.queryParams("lsa");
            String pathToLDA = request.queryParams("lda");
            boolean usePOSTagging = Boolean.parseBoolean(request.queryParams("postagging"));
            boolean computeDialogism = Boolean.parseBoolean(request.queryParams("dialogism"));

            QueryResultSentiment queryResult = new QueryResultSentiment();
            queryResult.setData(webService.services.SentimentAnalysis.getSentiment(QueryHelper.processQuery(text,
                    pathToLSA, pathToLDA, Lang.getLang(language), usePOSTagging, computeDialogism)));
            return queryResult.convertToJson();
        });
        Spark.get("/getComplexity", (request, response) -> {
            response.type("application/json");

            String text = request.queryParams("text");
            String language = request.queryParams("lang");
            String pathToLSA = request.queryParams("lsa");
            String pathToLDA = request.queryParams("lda");
            boolean usePOSTagging = Boolean.parseBoolean(request.queryParams("postagging"));
            boolean computeDialogism = Boolean.parseBoolean(request.queryParams("dialogism"));

            QueryResultTextualComplexity queryResult = new QueryResultTextualComplexity();
            TextualComplexity textualComplexity = new TextualComplexity(QueryHelper.processQuery(text, pathToLSA,
                    pathToLDA, Lang.getLang(language), usePOSTagging, computeDialogism), Lang.getLang(language),
                    usePOSTagging, computeDialogism);
            queryResult.setData(textualComplexity.getComplexityIndices());
            return queryResult.convertToJson();
        });
        Spark.get("/search", (request, response) -> {

            response.type("application/json");

            String text = request.queryParams("text");
            String path = request.queryParams("path");

            int maxContentSize = Integer.MAX_VALUE;
            String maxContentSizeStr = request.queryParams("mcs");
            if (maxContentSizeStr != null) {
                maxContentSize = Integer.parseInt(maxContentSizeStr);
            }

            QueryResultSearch queryResult = new QueryResultSearch();
            queryResult.setData(SearchClient.search(text, setDocuments(path), maxContentSize));
            return queryResult.convertToJson();
        });
        Spark.get("/getTopicsFromPdf", (request, response) -> {
            response.type("application/json");

            String uri = request.queryParams("uri");
            LOGGER.info("URI primit");
            LOGGER.info(uri);

            /*
			 * QueryResultPdfToText queryResult = new QueryResultPdfToText();
			 * queryResult.data = getTextFromPdf(uri); String result =
			 * convertToJson(queryResult);
             */
            String q = getTextFromPdf(uri, true).getContent();
            String pathToLSA = request.queryParams("lsa");
            String pathToLDA = request.queryParams("lda");
            String language = request.queryParams("lang");
            boolean usePOSTagging = Boolean.parseBoolean(request.queryParams("postagging"));
            boolean computeDialogism = Boolean.parseBoolean(request.queryParams("dialogism"));
            double threshold = Double.parseDouble(request.queryParams("threshold"));

            QueryResultTopic queryResult = new QueryResultTopic();
            queryResult.setData(ConceptMap.getTopics(QueryHelper.processQuery(q, pathToLSA, pathToLDA,
                    Lang.getLang(language), usePOSTagging, computeDialogism), threshold, null));
            String result = queryResult.convertToJson();
            // return Charset.forName("UTF-8").encode(result);
            return result;

        });
        Spark.post("/semanticProcessUri", (request, response) -> {
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());

            response.type("application/json");

            String uri = (String) json.get("uri");
            String documentContent;
            if (uri == null || uri.isEmpty()) {
                LOGGER.error("URI an URL are empty. Aborting...");
                System.exit(-1);
            }
            if (uri.contains("http") || uri.contains("https") || uri.contains("ftp")) {
                documentContent = getTextFromPdf(uri, false).getContent();
            } else {
                documentContent = getTextFromPdf(uri, true).getContent();
            }

            String documentAbstract = (String) json.get("abstract");
            String keywords = (String) json.get("keywords");
            String language = (String) json.get("lang");
            String pathToLSA = (String) json.get("lsa");
            String pathToLDA = (String) json.get("lda");
            boolean usePOSTagging = (boolean) json.get("postagging");
            boolean computeDialogism = Boolean.parseBoolean(request.queryParams("dialogism"));
            double threshold = (double) json.get("threshold");

            Lang lang = Lang.getLang(language);

            Set<String> keywordsList = new HashSet<>(Arrays.asList(keywords.split(",")));

            AbstractDocument document = QueryHelper.processQuery(documentContent, pathToLSA, pathToLDA, lang,
                    usePOSTagging, computeDialogism);
            AbstractDocument keywordsDocument = QueryHelper.processQuery(keywords, pathToLSA, pathToLDA, lang,
                    usePOSTagging, computeDialogism);
            AbstractDocument abstractDocument = QueryHelper.processQuery(documentAbstract, pathToLSA, pathToLDA, lang,
                    usePOSTagging, computeDialogism);

            QueryResultSemanticAnnotation queryResult = new QueryResultSemanticAnnotation();
            queryResult.setData(getSemanticAnnotation(abstractDocument, keywordsDocument, document, keywordsList,
                    pathToLSA, pathToLDA, Lang.getLang(language), usePOSTagging, computeDialogism, threshold));
            String result = queryResult.convertToJson();
            // return Charset.forName("UTF-8").encode(result);
            return result;

        });
        Spark.post("/semanticProcess", (request, response) -> {
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());

            response.type("application/json");

            String file = (String) json.get("file");
            String documentContent = getTextFromPdf("tmp/" + file, true).getContent();

            String documentAbstract = (String) json.get("abstract");
            String keywords = (String) json.get("keywords");
            String language = (String) json.get("lang");
            String pathToLSA = (String) json.get("lsa");
            String pathToLDA = (String) json.get("lda");
            boolean usePOSTagging = (boolean) json.get("postagging");
            boolean computeDialogism = Boolean.parseBoolean(request.queryParams("dialogism"));
            double threshold = (double) json.get("threshold");

            Set<String> keywordsList = new HashSet<>(Arrays.asList(keywords.split(",")));

            Lang lang = Lang.getLang(language);

            AbstractDocument document = QueryHelper.processQuery(documentContent, pathToLSA, pathToLDA, lang,
                    usePOSTagging, computeDialogism);
            AbstractDocument keywordsDocument = QueryHelper.processQuery(keywords, pathToLSA, pathToLDA, lang,
                    usePOSTagging, computeDialogism);
            AbstractDocument abstractDocument = QueryHelper.processQuery(documentAbstract, pathToLSA, pathToLDA, lang,
                    usePOSTagging, computeDialogism);

            QueryResultSemanticAnnotation queryResult = new QueryResultSemanticAnnotation();
            queryResult.setData(getSemanticAnnotation(abstractDocument, keywordsDocument, document, keywordsList,
                    pathToLSA, pathToLDA, Lang.getLang(language), usePOSTagging, computeDialogism, threshold));

            String result = queryResult.convertToJson();
            // return Charset.forName("UTF-8").encode(result);
            return result;

        });
        Spark.post("/selfExplanation", (request, response) -> {
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());

            response.type("application/json");

            String text = (String) json.get("text");
            String explanation = (String) json.get("explanation");
            String language = (String) json.get("lang");
            String pathToLSA = (String) json.get("lsa");
            String pathToLDA = (String) json.get("lda");
            boolean usePOSTagging = (boolean) json.get("postagging");
            boolean computeDialogism = Boolean.parseBoolean(request.queryParams("dialogism"));

            Lang lang = Lang.getLang(language);

            QueryResultSelfExplanation queryResult = new QueryResultSelfExplanation();
            queryResult.setData(
                    getSelfExplanation(text, explanation, pathToLSA, pathToLDA, lang, usePOSTagging, computeDialogism));
            String result = queryResult.convertToJson();
            // return Charset.forName("UTF-8").encode(result);
            return result;

        });
        Spark.post("/csclProcessing", (request, response) -> {
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());

            response.type("application/json");

            // String conversationText = (String) json.get("conversation");
            // String conversationPath = (String) json.get("conversationPath");
            String csclFile = (String) json.get("csclFile");
            String language = (String) json.get("lang");
            String pathToLSA = (String) json.get("lsa");
            String pathToLDA = (String) json.get("lda");
            boolean usePOSTagging = (boolean) json.get("postagging");
            boolean computeDialogism = Boolean.parseBoolean(request.queryParams("dialogism"));
            double threshold = (Double) json.get("threshold");

            Lang lang = Lang.getLang(language);
            Conversation conversation = Conversation.load(new File("tmp/" + csclFile), LSA.loadLSA(pathToLSA, lang),
                    LDA.loadLDA(pathToLDA, lang), lang, usePOSTagging, false);
            conversation.computeAll(computeDialogism, null, null, SaveType.NONE);
            AbstractDocument conversationDocument = QueryHelper.processQuery(conversation.getText(), pathToLSA,
                    pathToLDA, Lang.getLang(language), usePOSTagging, computeDialogism);

            QueryResultCscl queryResult = new QueryResultCscl();
            queryResult.setData(CSCL.getAll(conversationDocument, conversation, threshold));

            return queryResult.convertToJson();

        });
        Spark.post("/textCategorization", (request, response) -> {
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());

            response.type("application/json");

            String uri = (String) json.get("uri");

            String documentContent;
            if (uri == null || uri.isEmpty()) {
                LOGGER.error("URI an URL are empty. Aborting...");
                System.exit(-1);
            }
            if (uri.contains("http") || uri.contains("https") || uri.contains("ftp")) {
                documentContent = getTextFromPdf(uri, false).getContent();
            } else {
                documentContent = getTextFromPdf(uri, true).getContent();
            }

            String language = (String) json.get("lang");
            String pathToLSA = (String) json.get("lsa");
            String pathToLDA = (String) json.get("lda");
            boolean usePOSTagging = (boolean) json.get("postagging");
            boolean computeDialogism = Boolean.parseBoolean(request.queryParams("dialogism"));
            double threshold = (double) json.get("threshold");

            ResultTopic resultTopic = ConceptMap.getTopics(QueryHelper.processQuery(documentContent, pathToLSA, pathToLDA,
                    Lang.getLang(language), usePOSTagging, computeDialogism), threshold, null);
            List<ResultCategory> resultCategories = getCategories(documentContent, pathToLSA, pathToLDA, Lang.getLang(language),
                    usePOSTagging, computeDialogism, threshold);

            QueryResultTextCategorization queryResult = new QueryResultTextCategorization();
            queryResult.setData(new ResultTextCategorization(resultTopic, resultCategories));

            return queryResult.convertToJson();

        });
        Spark.post("/cvCoverProcessing", (request, response) -> {
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());

            response.type("application/json");

            String cvFile = (String) json.get("cvFile");
            String coverFile = (String) json.get("coverFile");
            String language = (String) json.get("lang");
            String pathToLSA = (String) json.get("lsa");
            String pathToLDA = (String) json.get("lda");
            boolean usePOSTagging = (boolean) json.get("postagging");
            boolean computeDialogism = Boolean.parseBoolean(request.queryParams("dialogism"));
            double threshold = (Double) json.get("threshold");

            Lang lang = Lang.getLang(language);
            Map<String, Integer> commonWords = new HashMap<>();
            String cvContent = getTextFromPdf("tmp/" + cvFile, true).getContent();
            AbstractDocument cvDocument = QueryHelper.processQuery(cvContent, pathToLSA, pathToLDA,
                    Lang.getLang(language), usePOSTagging, computeDialogism);
            Map<Word, Integer> cvWords = cvDocument.getWordOccurences();

            QueryResultCvCover queryResult = new QueryResultCvCover();
            ResultCvCover result = new ResultCvCover(null, null);
            ResultCvOrCover resultCv = new ResultCvOrCover(null, null);
            resultCv.setConcepts(ConceptMap.getTopics(
                    QueryHelper.processQuery(cvContent, pathToLSA, pathToLDA, lang, usePOSTagging, computeDialogism),
                    threshold, null));
            resultCv.setSentiments(webService.services.SentimentAnalysis.getSentiment(
                    QueryHelper.processQuery(cvContent, pathToLSA, pathToLDA, lang, usePOSTagging, computeDialogism)));
            result.setCv(resultCv);

            String coverContent = getTextFromPdf("tmp/" + coverFile, true).getContent();
            AbstractDocument coverDocument = QueryHelper.processQuery(coverContent, pathToLSA, pathToLDA, lang,
                    usePOSTagging, computeDialogism);

            ResultCvOrCover resultCover = new ResultCvOrCover(null, null);
            resultCover.setConcepts(ConceptMap.getTopics(
                    QueryHelper.processQuery(coverContent, pathToLSA, pathToLDA, lang, usePOSTagging, computeDialogism),
                    threshold, null));
            resultCover.setSentiments(webService.services.SentimentAnalysis.getSentiment(
                    QueryHelper.processQuery(cvContent, pathToLSA, pathToLDA, lang, usePOSTagging, computeDialogism)));
            result.setCover(resultCover);

            Map<Word, Integer> coverWords = coverDocument.getWordOccurences();

            Iterator<Entry<Word, Integer>> itCvWords = cvWords.entrySet().iterator();
            while (itCvWords.hasNext()) {
                Map.Entry<Word, Integer> cvPair = (Map.Entry<Word, Integer>) itCvWords.next();
                Word cvWord = (Word) cvPair.getKey();
                Integer cvWordOccurences = cvPair.getValue();
                if (coverWords.containsKey(cvWord)) {
                    commonWords.put(cvWord.getLemma(), cvWordOccurences + coverWords.get(cvWord));
                }
            }

            result.setWordOccurences(commonWords);
            // semantic similarity between Cover Letter & CV
            /*
			 * Map<String, Double> similarity = new HashMap<String, Double>();
			 * SemanticCohesion sc = new SemanticCohesion(cvDocument,
			 * coverDocument); similarity.put(sc.getSemanticDistances()[1],
			 * Formatting.formatNumber(sc.getLSASim())); similarity.put("LDA",
			 * Formatting.formatNumber(sc.getLDASim()));
			 * sc.getSemanticDistances()[0]=0; for(int i = 0; i <
			 * sc.getOntologySim().length; i++) similarity.put("LDA",
			 * Formatting.formatNumber(sc.getOntologySim()[i]));
			 * result.setSimilarity(similarity); 1 3 4
             */

            queryResult.setData(result);
            return queryResult.convertToJson();

        });
        Spark.post("/cvProcessing", (request, response) -> {
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());

            response.type("application/json");

            String cvFile = (String) json.get("cvFile");
            String keywords = (String) json.get("keywords");
            String ignore = (String) json.get("ignore");
            String language = (String) json.get("lang");
            String pathToLSA = (String) json.get("lsa");
            String pathToLDA = (String) json.get("lda");
            boolean usePOSTagging = (boolean) json.get("postagging");
            boolean computeDialogism = Boolean.parseBoolean(request.queryParams("dialogism"));
            double threshold = (Double) json.get("threshold");

            Set<String> keywordsList = new HashSet<>(Arrays.asList(keywords.split(",")));
            Set<String> ignoreList = new HashSet<>(Arrays.asList(ignore.split(",")));

            Lang lang = Lang.getLang(language);
            PdfToTextConverter pdfConverter = new PdfToTextConverter();
            String cvContent = pdfConverter.pdftoText("tmp/" + cvFile, true);

            LOGGER.info("Continut cv: " + cvContent);
            AbstractDocument cvDocument = QueryHelper.processQuery(cvContent, pathToLSA, pathToLDA, lang, usePOSTagging,
                    computeDialogism);
            AbstractDocument keywordsDocument = QueryHelper.processQuery(keywords, pathToLSA, pathToLDA, lang,
                    usePOSTagging, computeDialogism);

            QueryResultCv queryResult = new QueryResultCv();
            ResultCv result = CVHelper.process(cvDocument, keywordsDocument, pdfConverter, keywordsList, ignoreList,
                    pathToLSA, pathToLDA, lang, usePOSTagging, computeDialogism, threshold, 5, 1);

            queryResult.setData(result);

            return queryResult.convertToJson();

        });
        // File Upload - send file as multipart form-data to be accepted
        Spark.post("/fileUpload", (request, response) -> {
            MultipartConfigElement multipartConfigElement = new MultipartConfigElement("/tmp");
            request.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);
            Part file = request.raw().getPart("file"); // file is name of the
            // input in the upload
            // form
            return FileProcessor.getInstance().saveFile(file);
        });
        Spark.options("/fileUpload", (request, response) -> {
            return "";
        });
        Spark.get("/fileDownload", (request, response) -> {
            String file = request.queryParams("file");

            int indexOfLastSlash = file.lastIndexOf('/');
            if (indexOfLastSlash != -1) {
                file = file.substring(indexOfLastSlash);
            }
            File f = new File("tmp/" + file);

            HttpServletResponse raw = response.raw();
            if (f.exists() && !f.isDirectory()) {
                byte[] bytes = Files.readAllBytes(Paths.get("tmp/" + file));
                raw.getOutputStream().write(bytes);
            } else {
                raw.getOutputStream().write(null);
            }
            raw.getOutputStream().flush();
            raw.getOutputStream().close();

            return response.raw();
        });
        Spark.post("/folderUpload", (request, response) -> {
            File folder = FileProcessor.getInstance().createFolderForVCoPFiles();
            MultipartConfigElement multipartConfigElement = new MultipartConfigElement(folder.getAbsolutePath());
            request.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);
            List<Part> filesList = (List<Part>) request.raw().getParts();
            for (Part file : filesList) {
                FileProcessor.getInstance().saveFile(file, folder);
            }
            return folder.getName();
        });
        Spark.options("/folderUpload", (request, response) -> {
            return "";
        });
        Spark.post("/vcop", (request, response) -> {
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());

            response.type("application/json");

            StringBuilder communityFolder = new StringBuilder();
            communityFolder.append("resources/in/");
            String community = (String) json.get("community");
            communityFolder.append(community);
            String startDateString = (String) json.get("startDate");
            DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
            Date startDate = format.parse(startDateString);
            String endDateString = (String) json.get("endDate");
            Date endDate = format.parse(endDateString);
            Boolean useTextualComplexity = (Boolean) json.get("useTextualComplexity");
            long monthIncrement = (Long) json.get("monthIncrement");
            long dayIncrement = (Long) json.get("dayIncrement");

            Community communityStartEnd = Community.loadMultipleConversations(communityFolder.toString(), true, startDate, endDate,
                    (int) monthIncrement, (int) dayIncrement);
            communityStartEnd.computeMetrics(useTextualComplexity, true, true);

            List<Community> subCommunities = communityStartEnd.getTimeframeSubCommunities();

            Date startDateAllCommunities = format.parse("01/01/1970");
            Date endDateAllCommunities = format.parse("01/01/2099");

            Community allCommunity = Community.loadMultipleConversations(communityFolder.toString(), true, startDateAllCommunities,
                    endDateAllCommunities, (int) monthIncrement, (int) dayIncrement);
            allCommunity.computeMetrics(useTextualComplexity, true, true);

            List<ResultTopic> participantsInTimeFrame = new ArrayList<>();

            for (Community c : subCommunities) {
                participantsInTimeFrame.add(CommunityInteraction.buildParticipantGraph(c, true));
            }

            QueryResultvCoP queryResult = new QueryResultvCoP();
            ResultvCoP resultVcop = new ResultvCoP(CommunityInteraction.buildParticipantGraph(allCommunity, true),
                    CommunityInteraction.buildParticipantGraph(communityStartEnd, true), participantsInTimeFrame);
            queryResult.setData(resultVcop);

            String result = queryResult.convertToJson();
            LOGGER.info("queryResult" + result);
            return result;

        });
        Spark.post("/sendContactEmail", (request, response) -> {
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());

            response.type("application/json");

            String name = (String) json.get("name");
            String email = (String) json.get("email");
            String subject = (String) json.get("subject");
            String message = (String) json.get("message");

            HashMap<Object, Object> hm = new HashMap<>();
            HashMap<String, String> hmFrom = new HashMap<>();
            hmFrom.put("name", name);
            hmFrom.put("email", email);
            hm.put("from", hmFrom);

            HashMap<String, String> hmSimpleReceiver = new HashMap<>();
            hmSimpleReceiver.put("email", "contact@readerbench.com");
            hm.put("to", hmSimpleReceiver);
            hm.put("subject", subject);
            hm.put("message", message);
            ClientResponse mailGunResponse = SendMail.sendSimpleMessage(hm);
            if (mailGunResponse.getStatus() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + mailGunResponse.getStatus());
            }

            QueryResultMailgun queryResult = new QueryResultMailgun();
            String output = mailGunResponse.getEntity(String.class);

            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(output);
            queryResult.setMailgunResponse(jsonObject);

            String result = queryResult.convertToJson();
            LOGGER.info("queryResult" + result);

            return result;
        });
    }

    private static List<AbstractDocument> setDocuments(String path) {
        if (loadedPath != null && loadedPath.equals(path)) {
            return loadedDocs;
        }

        loadedPath = path;
        loadedDocs = new ArrayList<>();
        try {
            File dir = new File("resources/in/" + path);
            File[] files = dir.listFiles((File dir1, String name) -> name.endsWith(".ser"));

            for (File file : files) {
                Document d = (Document) AbstractDocument.loadSerializedDocument(file.getPath());
                loadedDocs.add(d);
            }
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }

        return loadedDocs;
    }

    public static void initializeDB() {
        LOGGER.info("Initialize words...");
        WordDAO.getInstance().loadAll();
        LOGGER.info("Words initialization finished");

        SentimentWeights.initialize();
        LOGGER.info("Valence map has " + data.sentiment.SentimentValence.getValenceMap().size() + " sentiments after initialization.");
    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO); // changing log level

        ReaderBenchServer.initializeDB();
        ReaderBenchServer server = new ReaderBenchServer();
        server.start();
    }
}
