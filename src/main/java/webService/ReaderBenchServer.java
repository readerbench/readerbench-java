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
import runtime.cv.CVAnalyzer;
import services.commons.Formatting;
import services.converters.PdfToTextConverter;
import services.mail.SendMail;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import spark.Request;
import spark.Spark;
import view.widgets.article.utils.GraphMeasure;
import webService.cv.CVHelper;
import webService.keywords.KeywordsHelper;
import webService.query.QueryHelper;
import webService.queryResult.QueryResult;
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
import webService.services.SentimentAnalysis;
import webService.services.TextualComplexity;
import webService.services.cscl.CSCL;
import webService.services.lak.TwoModeGraphBuilder;
import webService.services.lak.TwoModeGraphFilter;
import webService.services.lak.result.QueryResultGraphMeasures;
import webService.services.lak.result.QueryResultTwoModeGraph;
import webService.services.lak.result.QueryResultTwoModeGraphNodes;
import webService.services.lak.result.TwoModeGraph;
import webService.services.lak.result.TwoModeGraphNode;
import webService.services.utils.FileProcessor;
import webService.services.utils.ParamsValidator;
import webService.services.vCoP.CommunityInteraction;

public class ReaderBenchServer {

    private static final Logger LOGGER = Logger.getLogger(ReaderBenchServer.class);
    public static final int PORT = 8080;

    public static final Color COLOR_TOPIC = new Color(204, 204, 204); // silver
    public static final Color COLOR_INFERRED_CONCEPT = new Color(102, 102, 255); // orchid

    private static List<AbstractDocument> loadedDocs;
    private static String loadedPath;

    public List<ResultCategory> getCategories(String documentContent, Map<String, String> hm) {

        List<ResultCategory> resultCategories = new ArrayList<>();

        hm.put("text", documentContent);
        AbstractDocument queryDoc = QueryHelper.processQuery(hm);
        List<Category> dbCategories = CategoryDAO.getInstance().findAll();

        for (Category cat : dbCategories) {
            List<CategoryPhrase> categoryPhrases = cat.getCategoryPhraseList();
            StringBuilder sb = new StringBuilder();
            for (CategoryPhrase categoryPhrase : categoryPhrases) {
                sb.append(categoryPhrase.getLabel());
                sb.append(" ");
            }

            hm.put("text", sb.toString());
            AbstractDocument queryCategory = QueryHelper.processQuery(hm);
            SemanticCohesion sc = new SemanticCohesion(queryCategory, queryDoc);
            resultCategories.add(new ResultCategory(cat.getLabel(), Formatting.formatNumber(sc.getCohesion()), cat.getType()));
        }

        Collections.sort(resultCategories, ResultCategory.ResultCategoryRelevanceComparator);
        return resultCategories;
    }

    private ResultSemanticAnnotation getSemanticAnnotation(
            AbstractDocument abstractDocument,
            AbstractDocument keywordsDocument,
            AbstractDocument document,
            Set<String> keywordsList,
            Map<String, String> hm) {

        // concepts
        ResultTopic resultTopic = ConceptMap.getTopics(document, Double.parseDouble(hm.get("threshold")), null);
        List<ResultKeyword> resultKeywords = KeywordsHelper.getKeywords(document, keywordsDocument, keywordsList, hm);
        List<ResultCategory> resultCategories = getCategories(document.getText(), hm);

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

    private ResultSelfExplanation getSelfExplanation(String initialText, String selfExplanation, Map<String, String> hm) {
        Lang lang = Lang.getLang(hm.get("lang"));
        List<ISemanticModel> models = new ArrayList<>();
        models.add(LSA.loadLSA(hm.get("lsa"), lang));
        models.add(LDA.loadLDA(hm.get("lda"), lang));
        Document queryInitialText = new Document(
                null,
                AbstractDocumentTemplate.getDocumentModel(initialText),
                models,
                lang,
                Boolean.parseBoolean(hm.get("postagging")));

        Summary s = new Summary(selfExplanation, queryInitialText, true);
        s.computeAll(Boolean.parseBoolean(hm.get("dialogism")));

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

    /**
     * Returns an error result if there are any required parameters in the first
     * set missing in the second set.
     *
     * @param requiredParams a set of required key parameters
     * @param params a set of provided key parameters
     * @return an error message if there are any required parameters in the
     * first set missing in the second set or null otherwise
     */
    private static QueryResult errorIfParamsMissing(Set<String> requiredParams, Set<String> params) {
        Set<String> requiredParamsMissing;
        if (null != (requiredParamsMissing = ParamsValidator.checkRequiredParams(requiredParams, params))) {
            // if not return an error showing the missing parameters
            return new QueryResult(false, ParamsValidator.errorParamsMissing(requiredParamsMissing));
        }
        return null;
    }

    /**
     * Returns a HashMap containing <key, value> for parameters.
     *
     * @param request the request sent to the server
     * @return the HashMap if there are any parameters or null otherwise
     */
    private static Map<String, String> hmParams(Request request) {
        Map<String, String> hm = new HashMap<>();
        for (String paramKey : request.queryParams()) {
            hm.put(paramKey, request.queryParams(paramKey));
        }
        return hm;
    }

    /**
     * Returns a HashMap containing <key, value> for parameters.
     *
     * @param request the request sent to the server
     * @return the HashMap if there are any parameters or null otherwise
     */
    private static Map<String, String> hmParams(JSONObject json) {
        Map<String, String> hm = new HashMap<>();
        for (String paramKey : (Set<String>) json.keySet()) {
            hm.put(paramKey, json.get(paramKey).toString());
        }
        return hm;
    }

    /**
     * Returns a Set of initial required parameters.
     *
     * @return the set of initial required parameters
     */
    public static Set<String> setInitialRequiredParams() {
        Set<String> requiredParams = new HashSet<>();
        requiredParams.add("lang");
        requiredParams.add("lsa");
        requiredParams.add("lda");
        requiredParams.add("postagging");
        requiredParams.add("dialogism");
        return requiredParams;
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
            Set<String> requiredParams = setInitialRequiredParams();
            // additional required parameters
            requiredParams.add("text");
            requiredParams.add("threshold");
            // check whether all the required parameters are available
            errorIfParamsMissing(requiredParams, request.queryParams());

            Map<String, String> hm = hmParams(request);
            QueryResultTopic queryResult = new QueryResultTopic();
            queryResult.setData(
                    ConceptMap.getTopics(
                            QueryHelper.processQuery(hm),
                            Double.parseDouble(hm.get("threshold")),
                            null)
            );

            response.type("application/json");
            return queryResult.convertToJson();
        });
        Spark.get("/getSentiment", (request, response) -> {
            Set<String> requiredParams = setInitialRequiredParams();
            // additional required parameters
            requiredParams.add("text");
            // check whether all the required parameters are available
            errorIfParamsMissing(requiredParams, request.queryParams());

            Map<String, String> hm = hmParams(request);
            QueryResultSentiment queryResult = new QueryResultSentiment();
            try {
                queryResult.setData(
                        SentimentAnalysis.getSentiment(
                                QueryHelper.processQuery(hm)
                        )
                );
            } catch (Exception e) {
                LOGGER.error("Exception: " + e.getMessage());
            }

            response.type("application/json");
            return queryResult.convertToJson();
        });
        Spark.get("/getComplexity", (request, response) -> {
            Set<String> requiredParams = setInitialRequiredParams();
            // additional required parameters
            requiredParams.add("text");
            // check whether all the required parameters are available
            errorIfParamsMissing(requiredParams, request.queryParams());

            Map<String, String> hm = hmParams(request);
            QueryResultTextualComplexity queryResult = new QueryResultTextualComplexity();
            TextualComplexity textualComplexity = new TextualComplexity(
                    QueryHelper.processQuery(hm),
                    Lang.getLang(hm.get("lang")),
                    Boolean.parseBoolean(hm.get("postagging")),
                    Boolean.parseBoolean(hm.get("dialogism"))
            );
            queryResult.setData(textualComplexity.getComplexityIndices());

            response.type("application/json");
            return queryResult.convertToJson();
        });
        Spark.get("/search", (request, response) -> {
            Set<String> requiredParams = setInitialRequiredParams();
            // TODO: refactor here similar to other endpoints
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
            Set<String> requiredParams = setInitialRequiredParams();
            // additional required parameters
            requiredParams.add("uri");
            requiredParams.add("threshold");
            // check whether all the required parameters are available
            errorIfParamsMissing(requiredParams, request.queryParams());

            Map<String, String> hm = hmParams(request);
            LOGGER.info("URI primit: " + hm.get("uri"));
            hm.put("text", getTextFromPdf(hm.get("contents"), true).getContent());
            hm.remove("uri");

            QueryResultTopic queryResult = new QueryResultTopic();
            queryResult.setData(
                    ConceptMap.getTopics(
                            QueryHelper.processQuery(hm),
                            Double.parseDouble(hm.get("threshold")),
                            null)
            );

            response.type("application/json");
            return queryResult.convertToJson();

        });
        Spark.post("/semanticProcessUri", (request, response) -> {
            Set<String> requiredParams = setInitialRequiredParams();
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());
            // additional required parameters
            requiredParams.add("uri");
            requiredParams.add("threshold");
            // check whether all the required parameters are available
            errorIfParamsMissing(requiredParams, json.keySet());

            Map<String, String> hm = hmParams(json);
            String documentContent;
            if (hm.get("uri").contains("http") || hm.get("uri").contains("https") || hm.get("uri").contains("ftp")) {
                documentContent = getTextFromPdf(hm.get("uri"), false).getContent();
            } else {
                documentContent = getTextFromPdf(hm.get("uri"), true).getContent();
            }

            Set<String> keywordsList = new HashSet<>(Arrays.asList(((String) json.get("keywords")).split(",")));

            hm.put("text", documentContent);
            AbstractDocument document = QueryHelper.processQuery(hm);

            hm.put("text", hm.get("keywords"));
            AbstractDocument keywordsDocument = QueryHelper.processQuery(hm);

            hm.put("text", hm.get("abstract"));
            AbstractDocument abstractDocument = QueryHelper.processQuery(hm);

            QueryResultSemanticAnnotation queryResult = new QueryResultSemanticAnnotation();
            queryResult.setData(getSemanticAnnotation(abstractDocument, keywordsDocument, document, keywordsList, hm));

            response.type("application/json");
            return queryResult.convertToJson();

        });
        Spark.post("/semanticProcess", (request, response) -> {
            Set<String> requiredParams = setInitialRequiredParams();
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());
            // additional required parameters
            requiredParams.add("file");
            requiredParams.add("threshold");
            // check whether all the required parameters are available
            errorIfParamsMissing(requiredParams, json.keySet());

            Map<String, String> hm = hmParams(json);
            String documentContent = getTextFromPdf("tmp/" + hm.get("file"), true).getContent();

            Set<String> keywordsList = new HashSet<>(Arrays.asList(((String) json.get("keywords")).split(",")));

            hm.put("text", documentContent);
            AbstractDocument document = QueryHelper.processQuery(hm);

            hm.put("text", hm.get("keywords"));
            AbstractDocument keywordsDocument = QueryHelper.processQuery(hm);

            hm.put("text", hm.get("abstract"));
            AbstractDocument abstractDocument = QueryHelper.processQuery(hm);

            QueryResultSemanticAnnotation queryResult = new QueryResultSemanticAnnotation();
            queryResult.setData(getSemanticAnnotation(abstractDocument, keywordsDocument, document, keywordsList, hm));

            response.type("application/json");
            return queryResult.convertToJson();

        });
        Spark.post("/selfExplanation", (request, response) -> {
            Set<String> requiredParams = setInitialRequiredParams();
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());
            // additional required parameters
            requiredParams.add("text");
            requiredParams.add("explanation");
            // check whether all the required parameters are available
            errorIfParamsMissing(requiredParams, json.keySet());

            Map<String, String> hm = hmParams(json);

            QueryResultSelfExplanation queryResult = new QueryResultSelfExplanation();
            queryResult.setData(getSelfExplanation(hm.get("text"), hm.get("explanation"), hm));

            response.type("application/json");
            return queryResult.convertToJson();

        });
        Spark.post("/csclProcessing", (request, response) -> {
            Set<String> requiredParams = setInitialRequiredParams();
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());
            // additional required parameters
            requiredParams.add("csclFile");
            requiredParams.add("threshold");
            // check whether all the required parameters are available
            errorIfParamsMissing(requiredParams, json.keySet());

            Map<String, String> hm = hmParams(json);

            Lang lang = Lang.getLang(hm.get("lang"));
            List<ISemanticModel> models = new ArrayList<>();
            models.add(LSA.loadLSA(hm.get("lsa"), lang));
            models.add(LDA.loadLDA(hm.get("lda"), lang));

            Conversation conversation = Conversation.load(
                    new File("tmp/" + json.get("csclFile")),
                    models,
                    lang,
                    Boolean.parseBoolean(hm.get("postagging")));
            conversation.computeAll(Boolean.parseBoolean(hm.get("dialogism")));
            hm.put("text", conversation.getText());
            AbstractDocument conversationDocument = QueryHelper.processQuery(hm);

            QueryResultCscl queryResult = new QueryResultCscl();
            queryResult.setData(CSCL.getAll(conversationDocument, conversation, Double.parseDouble(hm.get("threshold"))));

            response.type("application/json");
            return queryResult.convertToJson();

        });
        Spark.post("/textCategorization", (request, response) -> {
            Set<String> requiredParams = setInitialRequiredParams();
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());
            // additional required parameters
            requiredParams.add("uri");
            requiredParams.add("threshold");
            // check whether all the required parameters are available
            errorIfParamsMissing(requiredParams, json.keySet());

            Map<String, String> hm = hmParams(json);

            String documentContent;
            if (hm.get("uri").contains("http") || hm.get("uri").contains("https") || hm.get("uri").contains("ftp")) {
                documentContent = getTextFromPdf(hm.get("uri"), false).getContent();
            } else {
                documentContent = getTextFromPdf(hm.get("uri"), true).getContent();
            }

            hm.put("text", documentContent);
            ResultTopic resultTopic = ConceptMap.getTopics(QueryHelper.processQuery(hm), Double.parseDouble(hm.get("threshold")), null);
            List<ResultCategory> resultCategories = getCategories(documentContent, hm);

            QueryResultTextCategorization queryResult = new QueryResultTextCategorization();
            queryResult.setData(new ResultTextCategorization(resultTopic, resultCategories));

            response.type("application/json");
            return queryResult.convertToJson();

        });
        Spark.post("/cvCoverProcessing", (request, response) -> {
            Set<String> requiredParams = setInitialRequiredParams();
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());
            // additional required parameters
            requiredParams.add("cvFile");
            requiredParams.add("coverFile");
            requiredParams.add("threshold");
            // check whether all the required parameters are available
            errorIfParamsMissing(requiredParams, json.keySet());

            Map<String, String> hm = hmParams(json);

            Map<String, Integer> commonWords = new HashMap<>();
            String cvContent = getTextFromPdf("tmp/" + hm.get("cvFile"), true).getContent();
            hm.put("text", cvContent);
            AbstractDocument cvDocument = QueryHelper.processQuery(hm);
            Map<Word, Integer> cvWords = cvDocument.getWordOccurences();

            QueryResultCvCover queryResult = new QueryResultCvCover();
            ResultCvCover result = new ResultCvCover(null, null);
            ResultCvOrCover resultCv = new ResultCvOrCover(null, null);
            resultCv.setConcepts(ConceptMap.getTopics(cvDocument, Double.parseDouble(hm.get("threshold")), null));
            resultCv.setSentiments(webService.services.SentimentAnalysis.getSentiment(cvDocument));
            result.setCv(resultCv);

            String coverContent = getTextFromPdf("tmp/" + hm.get("coverFile"), true).getContent();
            hm.put("text", coverContent);
            AbstractDocument coverDocument = QueryHelper.processQuery(hm);

            ResultCvOrCover resultCover = new ResultCvOrCover(null, null);
            resultCover.setConcepts(ConceptMap.getTopics(coverDocument, Double.parseDouble(hm.get("threshold")), null));
            resultCover.setSentiments(webService.services.SentimentAnalysis.getSentiment(coverDocument));
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
            queryResult.setData(result);

            response.type("application/json");
            return queryResult.convertToJson();

        });
        Spark.post("/cvProcessing", (request, response) -> {
            Set<String> requiredParams = setInitialRequiredParams();
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());
            // additional required parameters
            requiredParams.add("cvFile");
            requiredParams.add("threshold");
            // check whether all the required parameters are available
            errorIfParamsMissing(requiredParams, json.keySet());

            Map<String, String> hm = hmParams(json);

            Set<String> keywordsList = new HashSet<>(Arrays.asList(hm.get("keywords").split(",")));
            Set<String> ignoreList = new HashSet<>(Arrays.asList(hm.get("ignore").split(",")));

            PdfToTextConverter pdfConverter = new PdfToTextConverter();
            String cvContent = pdfConverter.pdftoText("tmp/" + hm.get("cvFile"), true);

            LOGGER.info("Continut cv: " + cvContent);
            hm.put("text", cvContent);
            AbstractDocument cvDocument = QueryHelper.processQuery(hm);
            hm.put("text", hm.get("keywords"));
            AbstractDocument keywordsDocument = QueryHelper.processQuery(hm);

            QueryResultCv queryResult = new QueryResultCv();
            ResultCv result = CVHelper.process(cvDocument, keywordsDocument, pdfConverter, keywordsList, ignoreList,
                    hm, CVAnalyzer.FAN_DELTA);

            queryResult.setData(result);

            response.type("application/json");
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
                    CommunityInteraction.buildParticipantGraph(communityStartEnd, true), participantsInTimeFrame, null);
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

        Spark.get("/lak/nodes", (request, response) -> {
            response.type("application/json");
            TwoModeGraphBuilder graphBuilder = TwoModeGraphBuilder.getLakCorpusTwoModeGraphBuilder();
            List<TwoModeGraphNode> authorNodes = graphBuilder.getNodes();
            QueryResultTwoModeGraphNodes qResult = new QueryResultTwoModeGraphNodes(authorNodes);
            return qResult.convertToJson();
        });

        Spark.post("/lak/graph", (request, response) -> {
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());
            response.type("application/json");
            String centerUri = (String) json.get("centerUri");
            String searchText = (String) json.get("searchText");
            int noAuthors = TwoModeGraphFilter.MaxNoAuthors;
            int noArticles = TwoModeGraphFilter.MaxNoArticles;
            boolean showAuthors = true, showArticles = true;
            try {
                noAuthors = ((Long) json.get("noAuthors")).intValue();
                noArticles = ((Long) json.get("noArticles")).intValue();
                showAuthors = (boolean) json.get("showAuthors");
                showArticles = (boolean) json.get("showArticles");
            } catch (Exception e) {
            }

            TwoModeGraphBuilder graphBuilder = TwoModeGraphBuilder.getLakCorpusTwoModeGraphBuilder();
            TwoModeGraph graph = graphBuilder.getGraph(centerUri, searchText);
            TwoModeGraphFilter graphFilter = new TwoModeGraphFilter();
            LOGGER.info("[Before filter] nodes = " + graph.nodeList.size() + " edges = " + graph.edgeList.size());
            graph = graphFilter.filterGraph(graph, centerUri, noAuthors, noArticles, showAuthors, showArticles);
            LOGGER.info("[After filter] nodes = " + graph.nodeList.size() + " edges = " + graph.edgeList.size());
            QueryResultTwoModeGraph queryResult = new QueryResultTwoModeGraph(graph);
            String result = queryResult.convertToJson();
            return result;
        });

        Spark.get("/lak/measures", (request, response) -> {
            response.type("application/json");
            List<GraphMeasure> measures = GraphMeasure.readGraphMeasures();
            QueryResultGraphMeasures qResult = new QueryResultGraphMeasures(measures);
            return qResult.convertToJson();
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
