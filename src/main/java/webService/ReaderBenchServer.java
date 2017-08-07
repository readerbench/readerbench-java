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

import dao.CategoryDAO;
import dao.WordDAO;
import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.Block;
import data.Lang;
import data.SemanticCorpora;
import data.Word;
import data.article.ResearchArticle;
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.servlet.MultipartConfigElement;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openide.util.Exceptions;
import runtime.cv.CVConstants;
import runtime.cv.CVFeedback;
import runtime.cv.CVValidation;
import services.converters.PdfToTxtConverter;
import services.elasticsearch.ElasticsearchService;
import services.semanticModels.ISemanticModel;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.SimilarityType;
import services.semanticModels.TextSimilarity;
import services.semanticModels.word2vec.Word2VecModel;
import spark.Request;
import spark.Response;
import spark.Spark;
import view.widgets.article.utils.GraphMeasure;
import webService.cv.CVHelper;
import webService.cv.JobQuestHelper;
import webService.keywords.KeywordsHelper;
import webService.query.QueryHelper;
import webService.queryResult.*;
import webService.result.ResultAnswerMatching;
import webService.result.ResultCategory;
import webService.result.ResultCv;
import webService.result.ResultCvCover;
import webService.result.ResultCvOrCover;
import webService.result.ResultFile;
import webService.result.ResultJobQuest;
import webService.result.ResultKeyword;
import webService.result.ResultPdfToText;
import webService.result.ResultReadingStrategy;
import webService.result.ResultSelfExplanation;
import webService.result.ResultSemanticAnnotation;
import webService.result.ResultSimilarConcepts;
import webService.result.ResultTextCategorization;
import webService.result.ResultTextSimilarities;
import webService.result.ResultTextSimilarity;
import webService.result.ResultTopic;
import webService.result.ResultvCoP;
import webService.semanticSearch.SearchClient;
import webService.services.ConceptMap;
import webService.services.SentimentAnalysis;
import webService.services.TextualComplexity;
import webService.services.cimodel.ComprehensionModelService;
import webService.services.cimodel.result.CMResult;
import webService.services.cimodel.result.QueryResultCM;
import webService.services.cscl.CSCL;
import webService.services.cscl.result.QueryResultAllCommunities;
import webService.services.cscl.result.QueryResultParticipants;
import webService.services.cscl.result.QueryResultParticipantsInteraction;
import webService.services.lak.TopicEvolutionBuilder;
import webService.services.lak.TwoModeGraphBuilder;
import webService.services.lak.TwoModeGraphFilter;
import webService.services.lak.result.QueryResultDocumentYears;
import webService.services.lak.result.QueryResultGraphMeasures;
import webService.services.lak.result.QueryResultTopicEvolution;
import webService.services.lak.result.QueryResultTwoModeGraph;
import webService.services.lak.result.QueryResultTwoModeGraphNodes;
import webService.services.lak.result.TopicEvolution;
import webService.services.lak.result.TwoModeGraph;
import webService.services.lak.result.TwoModeGraphNode;
import webService.services.utils.FileProcessor;
import webService.services.utils.ParamsValidator;
import webService.services.vCoP.CommunityInteraction;

public class ReaderBenchServer {

    private static final Logger LOGGER = Logger.getLogger("");
    public static final int PORT = 8080;

    public static final Color COLOR_TOPIC = new Color(204, 204, 204); // silver
    public static final Color COLOR_INFERRED_CONCEPT = new Color(102, 102, 255); // orchid

    private static List<AbstractDocument> loadedDocs;
    private static String loadedPath;
    private ElasticsearchService elasticsearchService = new ElasticsearchService();

    private static QueryResult errorEmptyBody() {
        return new QueryResult(false, ParamsValidator.errorNoParams());
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

    private static QueryResult errorIfParamsEmpty(Set<String> params) {
        Set<String> emptyParams;
        if (null != (emptyParams = ParamsValidator.checkEmptyParams(params))) {
            // if not return an error showing the missing parameters
            return new QueryResult(false, ParamsValidator.errorParamsMissing(emptyParams));
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
     * @param json the request sent to the server
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
        requiredParams.add("language");
        requiredParams.add("lsa");
        requiredParams.add("lda");
        requiredParams.add("pos-tagging");
        requiredParams.add("dialogism");
        return requiredParams;
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
        } catch (IOException | ClassNotFoundException e) {
            Exceptions.printStackTrace(e);
        }

        return loadedDocs;
    }

    public static void initializeDB() {
        LOGGER.setLevel(Level.INFO); // changing log level
        org.apache.log4j.BasicConfigurator.configure();
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.INFO);
        FileHandler fh;
        try {
            fh = new FileHandler("ReaderBenchServer.log");
            LOGGER.addHandler(fh);
        } catch (IOException | SecurityException ex) {
            Exceptions.printStackTrace(ex);
        }

        LOGGER.info("Initialize words...");
        WordDAO.getInstance().loadAll();
        LOGGER.info("Words initialization finished");

        SentimentWeights.initialize();
        LOGGER.log(Level.INFO, "Valence map has {0} sentiments after initialization.", data.sentiment.SentimentValence.getValenceMap().size());
    }

    public static void main(String[] args) {
        ReaderBenchServer.initializeDB();
        ReaderBenchServer server = new ReaderBenchServer();
        server.start();
    }

    public List<ResultCategory> generateCategories(AbstractDocument document, Lang lang, List<ISemanticModel> models, Boolean usePosTagging, Boolean computeDialogism, Boolean useBigrams) throws Exception {
        List<ResultCategory> resultCategories = new ArrayList<>();
        List<Category> dbCategories = CategoryDAO.getInstance().findAll();
        for (Category cat : dbCategories) {
            List<CategoryPhrase> categoryPhrases = cat.getCategoryPhraseList();
            StringBuilder sb = new StringBuilder();
            for (CategoryPhrase categoryPhrase : categoryPhrases) {
                sb.append(categoryPhrase.getLabel()).append(' ');
            }
            String text = sb.toString();
            AbstractDocument queryCategory = QueryHelper.generateDocument(text, lang, models, usePosTagging, computeDialogism, useBigrams);
            SemanticCohesion sc = new SemanticCohesion(queryCategory, document);
            resultCategories.add(new ResultCategory(cat.getLabel(), sc.getCohesion(), cat.getType()));
        }
        Collections.sort(resultCategories, ResultCategory.ResultCategoryRelevanceComparator);
        return resultCategories;
    }

    private ResultSemanticAnnotation getSemanticAnnotation(
            AbstractDocument abstractDocument, AbstractDocument keywordsDocument, AbstractDocument document,
            Set<String> keywordsList, Lang lang, List<ISemanticModel> models, Boolean usePosTagging, Boolean computeDialogism, Boolean useBigrams, Double minThreshold) throws Exception {
        ResultTopic resultTopic = ConceptMap.getKeywords(document, minThreshold, null);
        List<ResultKeyword> resultKeywords = KeywordsHelper.getKeywords(document, keywordsDocument, keywordsList, lang, models, usePosTagging, computeDialogism, useBigrams, minThreshold);
        List<ResultCategory> resultCategories = generateCategories(document, lang, models, usePosTagging, computeDialogism, useBigrams);
        SemanticCohesion scAbstractDocument = new SemanticCohesion(abstractDocument, document);
        SemanticCohesion scKeywordsAbstract = new SemanticCohesion(abstractDocument, keywordsDocument);
        SemanticCohesion scKeywordsDocument = new SemanticCohesion(keywordsDocument, document);
        ResultSemanticAnnotation rsa = new ResultSemanticAnnotation(resultTopic,
                scAbstractDocument.getCohesion(),
                scKeywordsAbstract.getCohesion(),
                scKeywordsDocument.getCohesion(), resultKeywords, resultCategories);
        return rsa;
    }

    private ResultSelfExplanation generateSelfExplanation(AbstractDocument document, String selfExplanation, boolean computeDialogism) {
        Summary s = new Summary(selfExplanation, (Document) document, true);
        s.computeAll(computeDialogism, false);

        List<ResultReadingStrategy> readingStrategies = new ArrayList<>();
        for (ReadingStrategyType rs : ReadingStrategyType.values()) {
            readingStrategies.add(new ResultReadingStrategy(rs.getName(), s.getAutomatedRS().get(0).get(rs)));
        }

        StringBuilder summary = new StringBuilder();
        for (Block b : s.getBlocks()) {
            LOGGER.log(Level.INFO, "Block alternate text: {0}", b.getAlternateText());
            summary.append(b.getAlternateText());
            summary.append("<br/>");
        }

        summary.append(s.getAlternateText());

        return new ResultSelfExplanation(summary.toString(), readingStrategies);
    }

    /**
     * Computes text similarity between two strings
     *
     * @param text1 First text
     * @param text2 Second text
     * @param lang Language to be used
     * @param model Model to be used
     * @param corpus Corpus to be used
     * @return
     */
    private ResultTextSimilarity generateTextSimilarity(String text1, String text2, Lang lang, String model, String corpus) {
        if (lang == null || model == null || model.isEmpty() || corpus == null || corpus.isEmpty()) {
            return null;
        }
        ISemanticModel semanticModel = null;
        List<ISemanticModel> models = new ArrayList<>();
        if (model.toLowerCase().compareTo("lsa") == 0) {
            semanticModel = LSA.loadLSA(SemanticCorpora.getSemanticCorpora(corpus, lang, SimilarityType.LSA).getFullPath(), lang);
        } else if (model.toLowerCase().compareTo("lda") == 0) {
            semanticModel = LDA.loadLDA(SemanticCorpora.getSemanticCorpora(corpus, lang, SimilarityType.LSA).getFullPath(), lang);
        }
        if (semanticModel == null) {
            return null;
        }
        models.add(semanticModel);
        Document docText1 = new Document(null, AbstractDocumentTemplate.getDocumentModel(text1), models, lang, true);
        Document docText2 = new Document(null, AbstractDocumentTemplate.getDocumentModel(text2), models, lang, true);
        return new ResultTextSimilarity(semanticModel.getSimilarity(docText1, docText2));
    }

    /**
     * Retrieves similar concepts for a given concepts
     *
     * @param seed The word
     * @param lang Language of the word
     * @param model Semantic model to be used
     * @param corpus Corpus to be used for semantic model
     * @param minThreshold Threshold to be used for similar concepts
     * @return
     */
    private ResultSimilarConcepts generateSimilarConcepts(String seed, Lang lang, String model, String corpus, double minThreshold) {
        if (seed == null || seed.isEmpty() || lang == null || model == null || model.isEmpty() || corpus == null || corpus.isEmpty()) {
            return null;
        }
        ISemanticModel semanticModel = null;
        List<ISemanticModel> models = new ArrayList<>();
        if (model.toLowerCase().compareTo("lsa") == 0) {
            semanticModel = LSA.loadLSA(SemanticCorpora.getSemanticCorpora(corpus, lang, SimilarityType.LSA).getFullPath(), lang);
        } else if (model.toLowerCase().compareTo("lda") == 0) {
            semanticModel = LDA.loadLDA(SemanticCorpora.getSemanticCorpora(corpus, lang, SimilarityType.LSA).getFullPath(), lang);
        } else if (model.toLowerCase().compareTo("w2v") == 0) {
            semanticModel = Word2VecModel.loadWord2Vec(corpus, lang);
        }
        if (semanticModel == null) {
            return null;
        }
        models.add(semanticModel);
        Document seedDocument = new Document(null, AbstractDocumentTemplate.getDocumentModel(seed), models, lang, true);
        return new ResultSimilarConcepts(semanticModel.getSimilarConcepts(seedDocument, minThreshold));
    }

    /**
     *
     * @param text1 First text
     * @param text2 Second text
     * @param lang The language of the models
     * @param models The models
     * @param usePOSTagging use or not POS tagging
     * @return
     */
    private ResultTextSimilarities textSimilarities(String text1, String text2, Lang lang, List<ISemanticModel> models, boolean usePOSTagging) {
        Map<SimilarityType, Double> similarityScores = TextSimilarity.textSimilarities(text1, text2, lang, models, usePOSTagging);
        Map<String, Double> similarityScoresToString = new HashMap<>();
        for (Map.Entry<SimilarityType, Double> entry : similarityScores.entrySet()) {
            similarityScoresToString.put(entry.getKey().getAcronym(), entry.getValue());
        }
        return new ResultTextSimilarities(similarityScoresToString);
    }

    private ResultAnswerMatching computeScoresPerAnswer(AbstractDocument userInputDocument, List<AbstractDocument> predefinedAnswerDocuments) {
        Map<Integer, Double> scoresPerAnswer = new HashMap<>();
        for (AbstractDocument predefinedAnswerDocument : predefinedAnswerDocuments) {
            SemanticCohesion sc = new SemanticCohesion(userInputDocument, predefinedAnswerDocument);
            scoresPerAnswer.put(predefinedAnswerDocuments.indexOf(predefinedAnswerDocument), sc.getCohesion());
        }
        return new ResultAnswerMatching(scoresPerAnswer);
    }

    private ResultPdfToText getTextFromPdf(String uri, boolean localFile) {
        PdfToTxtConverter pdfToTxtConverter;
        if (localFile) {
            pdfToTxtConverter = new PdfToTxtConverter(uri, true);
            pdfToTxtConverter.process();
            return new ResultPdfToText(pdfToTxtConverter.getParsedText());
        } else {
            pdfToTxtConverter = new PdfToTxtConverter(uri, false);
            pdfToTxtConverter.process();
            return new ResultPdfToText(pdfToTxtConverter.getParsedText());
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
        Spark.post("/keywords", (request, response) -> {
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());
            Set<String> requiredParams = setInitialRequiredParams();
            requiredParams.add("text");
            requiredParams.add("threshold");
            requiredParams.add("bigrams");
            QueryResult error = errorIfParamsMissing(requiredParams, json.keySet());
            if (error != null) {
                return error.convertToJson();
            }

            Map<String, String> hm = hmParams(json);
            Lang lang = Lang.getLang(hm.get("language"));
            Boolean usePosTagging = Boolean.parseBoolean(hm.get("pos-tagging"));
            Boolean computeDialogism = Boolean.parseBoolean(hm.get("dialogism"));
            String text = hm.get("text");
            double minThreshold;
            try {
                minThreshold = Double.parseDouble(hm.get("threshold"));
            } catch (NullPointerException e) {
                minThreshold = 0.3;
            }
            Boolean useBigrams = Boolean.parseBoolean(hm.get("bigrams"));
            String lsaCorpora = "", ldaCorpora = "", w2vCorpora = "";
            if (hm.get("lsa") != null && (hm.get("lsa").compareTo("") != 0)) {
                lsaCorpora = hm.get("lsa");
            }
            if (hm.get("lda") != null && (hm.get("lda").compareTo("") != 0)) {
                ldaCorpora = hm.get("lda");
            }
            if (hm.get("w2v") != null && (hm.get("w2v").compareTo("") != 0)) {
                w2vCorpora = hm.get("w2v");
            }
            List<ISemanticModel> models = QueryHelper.loadSemanticModels(lang, lsaCorpora, ldaCorpora, w2vCorpora);
            AbstractDocument document = QueryHelper.generateDocument(text, lang, models, usePosTagging, computeDialogism, useBigrams);
            ResultTopic resultTopic = ConceptMap.getKeywords(document, minThreshold, null);
            QueryResultTopicAdvanced queryResult = new QueryResultTopicAdvanced();
            queryResult.setData(resultTopic);
            response.type("application/json");
            return queryResult.convertToJson();
        });
        Spark.post("/sentiment-analysis", (request, response) -> {
            QueryResult error;
            JSONObject json = null;
            if (request.body().isEmpty()) {
                error = errorEmptyBody();
            } else {
                json = (JSONObject) new JSONParser().parse(request.body());
                Set<String> requiredParams = setInitialRequiredParams();
                requiredParams.add("text");
                error = errorIfParamsMissing(requiredParams, json.keySet());
            }
            if (error != null) {
                return error.convertToJson();
            }

            Map<String, String> hm = hmParams(json);
            Lang lang = Lang.getLang(hm.get("language"));
            Boolean usePosTagging = Boolean.parseBoolean(hm.get("pos-tagging"));
            Boolean computeDialogism = Boolean.parseBoolean(hm.get("dialogism"));
            Boolean useBigrams = false;
            String lsaCorpora = "", ldaCorpora = "", w2vCorpora = "";
            if (hm.get("lsa") != null && (hm.get("lsa").compareTo("") != 0)) {
                lsaCorpora = hm.get("lsa");
            }
            if (hm.get("lda") != null && (hm.get("lda").compareTo("") != 0)) {
                ldaCorpora = hm.get("lda");
            }
            if (hm.get("w2v") != null && (hm.get("w2v").compareTo("") != 0)) {
                w2vCorpora = hm.get("w2v");
            }
            List<ISemanticModel> models = QueryHelper.loadSemanticModels(lang, lsaCorpora, ldaCorpora, w2vCorpora);
            String text = hm.get("text");
            Integer granularity = null;
            if (hm.get("granularity") != null && (hm.get("granularity").compareTo("") != 0)) {
                granularity = Integer.parseInt(hm.get("granularity"));
            }
            AbstractDocument document = QueryHelper.generateDocument(text, lang, models, usePosTagging, computeDialogism, useBigrams);
            QueryResultSentiment queryResult = new QueryResultSentiment();
            try {
                queryResult.setData(SentimentAnalysis.computeSentiments(document, granularity));
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Exception: {0}", e.getMessage());
            }
            response.type("application/json");
            return queryResult.convertToJson();
        });
        Spark.post("/textual-complexity", (request, response) -> {
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());
            Set<String> requiredParams = setInitialRequiredParams();
            requiredParams.add("text");
            QueryResult error = errorIfParamsMissing(requiredParams, json.keySet());
            if (error != null) {
                return error.convertToJson();
            }

            Map<String, String> hm = hmParams(json);
            Lang lang = Lang.getLang(hm.get("language"));
            Boolean usePosTagging = Boolean.parseBoolean(hm.get("pos-tagging"));
            Boolean computeDialogism = Boolean.parseBoolean(hm.get("dialogism"));
            Boolean useBigrams = false;
            String lsaCorpora = "", ldaCorpora = "", w2vCorpora = "";
            if (hm.get("lsa") != null && (hm.get("lsa").compareTo("") != 0)) {
                lsaCorpora = hm.get("lsa");
            }
            if (hm.get("lda") != null && (hm.get("lda").compareTo("") != 0)) {
                ldaCorpora = hm.get("lda");
            }
            if (hm.get("w2v") != null && (hm.get("w2v").compareTo("") != 0)) {
                w2vCorpora = hm.get("w2v");
            }
            List<ISemanticModel> models = QueryHelper.loadSemanticModels(lang, lsaCorpora, ldaCorpora, w2vCorpora);
            String text = hm.get("text");
            AbstractDocument document = QueryHelper.generateDocument(text, lang, models, usePosTagging, computeDialogism, useBigrams);
            QueryResultTextualComplexity queryResult = new QueryResultTextualComplexity();
            try {
                TextualComplexity textualComplexity = new TextualComplexity(document, lang, usePosTagging, computeDialogism);
                queryResult.setData(textualComplexity.getComplexityIndices());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Exception: {0}", e.getMessage());
            }
            response.type("application/json");
            return queryResult.convertToJson();
        });
        Spark.post("/semantic-search", (request, response) -> {
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());
            Set<String> requiredParams = new HashSet<>();
            requiredParams.add("text");
            requiredParams.add("path");
            QueryResult error = errorIfParamsMissing(requiredParams, json.keySet());
            if (error != null) {
                return error.convertToJson();
            }

            Map<String, String> hm = hmParams(json);
            int maxContentSize = Integer.MAX_VALUE;
            String maxContentSizeStr = hm.get("mcs");
            if (maxContentSizeStr != null) {
                maxContentSize = Integer.parseInt(maxContentSizeStr);
            }
            QueryResultSearch queryResult = new QueryResultSearch();
            queryResult.setData(SearchClient.search(hm.get("text"), setDocuments(hm.get("path")), maxContentSize));
            response.type("application/json");
            return queryResult.convertToJson();
        });
        Spark.get("/getTopicsFromPdf", (request, response) -> {
            Set<String> requiredParams = setInitialRequiredParams();
            requiredParams.add("uri");
            requiredParams.add("threshold");
            QueryResult error = errorIfParamsMissing(requiredParams, request.queryParams());
            if (error != null) {
                return error.convertToJson();
            }

            Map<String, String> hm = hmParams(request);
            Lang lang = Lang.getLang(hm.get("language"));
            Boolean usePosTagging = Boolean.parseBoolean(hm.get("pos-tagging"));
            Boolean computeDialogism = Boolean.parseBoolean(hm.get("dialogism"));
            Boolean useBigrams = false;
            String lsaCorpora = "", ldaCorpora = "", w2vCorpora = "";
            if (hm.get("lsa") != null && (hm.get("lsa").compareTo("") != 0)) {
                lsaCorpora = hm.get("lsa");
            }
            if (hm.get("lda") != null && (hm.get("lda").compareTo("") != 0)) {
                ldaCorpora = hm.get("lda");
            }
            if (hm.get("w2v") != null && (hm.get("w2v").compareTo("") != 0)) {
                w2vCorpora = hm.get("w2v");
            }
            List<ISemanticModel> models = QueryHelper.loadSemanticModels(lang, lsaCorpora, ldaCorpora, w2vCorpora);
            LOGGER.log(Level.INFO, "URI primit: {0}", hm.get("uri"));
            String text = getTextFromPdf(hm.get("uri"), true).getContent();
            double minThreshold;
            try {
                minThreshold = Double.parseDouble(hm.get("threshold"));
            } catch (NullPointerException e) {
                minThreshold = 0.3;
            }
            AbstractDocument document = QueryHelper.generateDocument(text, lang, models, usePosTagging, computeDialogism, useBigrams);
            QueryResultTopic queryResult = new QueryResultTopic();
            queryResult.setData(ConceptMap.getKeywords(document, minThreshold, null));
            response.type("application/json");
            return queryResult.convertToJson();
        });
        Spark.post("/semantic-annotation-uri", (request, response) -> {
            Set<String> requiredParams = setInitialRequiredParams();
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());
            requiredParams.add("uri");
            requiredParams.add("threshold");
            QueryResult error = errorIfParamsMissing(requiredParams, json.keySet());
            if (error != null) {
                return error.convertToJson();
            }

            Map<String, String> hm = hmParams(json);
            Lang lang = Lang.getLang(hm.get("language"));
            Boolean usePosTagging = Boolean.parseBoolean(hm.get("pos-tagging"));
            Boolean computeDialogism = Boolean.parseBoolean(hm.get("dialogism"));
            Boolean useBigrams = false;
            String lsaCorpora = "", ldaCorpora = "", w2vCorpora = "";
            if (hm.get("lsa") != null && (hm.get("lsa").compareTo("") != 0)) {
                lsaCorpora = hm.get("lsa");
            }
            if (hm.get("lda") != null && (hm.get("lda").compareTo("") != 0)) {
                ldaCorpora = hm.get("lda");
            }
            if (hm.get("w2v") != null && (hm.get("w2v").compareTo("") != 0)) {
                w2vCorpora = hm.get("w2v");
            }
            List<ISemanticModel> models = QueryHelper.loadSemanticModels(lang, lsaCorpora, ldaCorpora, w2vCorpora);
            String documentContent;
            if (hm.get("uri").contains("http") || hm.get("uri").contains("https") || hm.get("uri").contains("ftp")) {
                documentContent = getTextFromPdf(hm.get("uri"), false).getContent();
            } else {
                documentContent = getTextFromPdf(hm.get("uri"), true).getContent();
            }
            double minThreshold;
            try {
                minThreshold = Double.parseDouble(hm.get("threshold"));
            } catch (NullPointerException e) {
                minThreshold = 0.3;
            }
            Set<String> keywordsList = new HashSet<>(Arrays.asList(((String) json.get("keywords")).split(",")));
            AbstractDocument document = QueryHelper.generateDocument(documentContent, lang, models, usePosTagging, computeDialogism, useBigrams);
            String keywordsText = hm.get("keywords");
            AbstractDocument keywordsDocument = QueryHelper.generateDocument(keywordsText, lang, models, usePosTagging, computeDialogism, useBigrams);
            String abstractText = hm.get("abstract");
            AbstractDocument abstractDocument = QueryHelper.generateDocument(abstractText, lang, models, usePosTagging, computeDialogism, useBigrams);
            QueryResultSemanticAnnotation queryResult = new QueryResultSemanticAnnotation();
            queryResult.setData(getSemanticAnnotation(abstractDocument, keywordsDocument, document, keywordsList, lang, models, usePosTagging, computeDialogism, useBigrams, minThreshold));
            response.type("application/json");
            return queryResult.convertToJson();
        });
        Spark.post("/semantic-annotation", (request, response) -> {
            Set<String> requiredParams = setInitialRequiredParams();
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());
            requiredParams.add("file");
            requiredParams.add("threshold");
            QueryResult error = errorIfParamsMissing(requiredParams, json.keySet());
            if (error != null) {
                return error.convertToJson();
            }

            Map<String, String> hm = hmParams(json);
            Lang lang = Lang.getLang(hm.get("language"));
            Boolean usePosTagging = Boolean.parseBoolean(hm.get("pos-tagging"));
            Boolean computeDialogism = Boolean.parseBoolean(hm.get("dialogism"));
            Boolean useBigrams = false;
            String lsaCorpora = "", ldaCorpora = "", w2vCorpora = "";
            if (hm.get("lsa") != null && (hm.get("lsa").compareTo("") != 0)) {
                lsaCorpora = hm.get("lsa");
            }
            if (hm.get("lda") != null && (hm.get("lda").compareTo("") != 0)) {
                ldaCorpora = hm.get("lda");
            }
            if (hm.get("w2v") != null && (hm.get("w2v").compareTo("") != 0)) {
                w2vCorpora = hm.get("w2v");
            }
            List<ISemanticModel> models = QueryHelper.loadSemanticModels(lang, lsaCorpora, ldaCorpora, w2vCorpora);
            String documentContent = getTextFromPdf("tmp/" + hm.get("file"), true).getContent();
            double minThreshold;
            try {
                minThreshold = Double.parseDouble(hm.get("threshold"));
            } catch (NullPointerException e) {
                minThreshold = 0.3;
            }
            Set<String> keywordsList = new HashSet<>(Arrays.asList(((String) json.get("keywords")).split(",")));
            try {
                AbstractDocument document = QueryHelper.generateDocument(documentContent, lang, models, usePosTagging, computeDialogism, useBigrams);
                String keywordsText = hm.get("keywords");
                AbstractDocument keywordsDocument = QueryHelper.generateDocument(keywordsText, lang, models, usePosTagging, computeDialogism, useBigrams);
                String abstractText = hm.get("abstract");
                AbstractDocument abstractDocument = QueryHelper.generateDocument(abstractText, lang, models, usePosTagging, computeDialogism, useBigrams);
                QueryResultSemanticAnnotation queryResult = new QueryResultSemanticAnnotation();
                queryResult.setData(getSemanticAnnotation(abstractDocument, keywordsDocument, document, keywordsList, lang, models, usePosTagging, computeDialogism, useBigrams, minThreshold));
                response.type("application/json");
                return queryResult.convertToJson();
            } catch (Exception e) {
                error = new QueryResult(false, e.getMessage());
                return error.convertToJson();
            }
        });
        Spark.post("/self-explanation", (request, response) -> {
            Set<String> requiredParams = setInitialRequiredParams();
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());
            requiredParams.add("text");
            requiredParams.add("explanation");
            QueryResult error = errorIfParamsMissing(requiredParams, json.keySet());
            if (error != null) {
                return error.convertToJson();
            }

            Map<String, String> hm = hmParams(json);
            Lang lang = Lang.getLang(hm.get("language"));
            Boolean usePosTagging = Boolean.parseBoolean(hm.get("pos-tagging"));
            Boolean computeDialogism = Boolean.parseBoolean(hm.get("dialogism"));
            Boolean useBigrams = false;
            String lsaCorpora = "", ldaCorpora = "", w2vCorpora = "";
            if (hm.get("lsa") != null && (hm.get("lsa").compareTo("") != 0)) {
                lsaCorpora = hm.get("lsa");
            }
            if (hm.get("lda") != null && (hm.get("lda").compareTo("") != 0)) {
                ldaCorpora = hm.get("lda");
            }
            if (hm.get("w2v") != null && (hm.get("w2v").compareTo("") != 0)) {
                w2vCorpora = hm.get("w2v");
            }
            List<ISemanticModel> models = QueryHelper.loadSemanticModels(lang, lsaCorpora, ldaCorpora, w2vCorpora);
            String text = hm.get("text");
            AbstractDocument document = QueryHelper.generateDocument(text, lang, models, usePosTagging, computeDialogism, useBigrams);
            QueryResultSelfExplanation queryResult = new QueryResultSelfExplanation();
            String explanation = hm.get("explanation");
            queryResult.setData(generateSelfExplanation(document, explanation, usePosTagging));
            response.type("application/json");
            return queryResult.convertToJson();
        });
        Spark.post("/cscl-processing", (request, response) -> {
            Set<String> requiredParams = setInitialRequiredParams();
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());
            requiredParams.add("cscl-file");
            requiredParams.add("threshold");
            QueryResult error = errorIfParamsMissing(requiredParams, json.keySet());
            if (error != null) {
                response.type("application/json");
                return error.convertToJson();
            }

            Map<String, String> hm = hmParams(json);
            Lang lang = Lang.getLang(hm.get("language"));
            Boolean usePosTagging = Boolean.parseBoolean(hm.get("pos-tagging"));
            Boolean computeDialogism = Boolean.parseBoolean(hm.get("dialogism"));
            Boolean useBigrams = false;
            String lsaCorpora = "", ldaCorpora = "", w2vCorpora = "";
            if (hm.get("lsa") != null && (hm.get("lsa").compareTo("") != 0)) {
                lsaCorpora = hm.get("lsa");
            }
            if (hm.get("lda") != null && (hm.get("lda").compareTo("") != 0)) {
                ldaCorpora = hm.get("lda");
            }
            if (hm.get("w2v") != null && (hm.get("w2v").compareTo("") != 0)) {
                w2vCorpora = hm.get("w2v");
            }
            List<ISemanticModel> models = QueryHelper.loadSemanticModels(lang, lsaCorpora, ldaCorpora, w2vCorpora);
            double minThreshold;
            try {
                minThreshold = Double.parseDouble(hm.get("threshold"));
            } catch (NullPointerException e) {
                minThreshold = 0.3;
            }
            String csclFile = json.get("cscl-file").toString();
            Conversation conversation = Conversation.load(new File("tmp/" + csclFile), models, lang, usePosTagging);
            conversation.computeAll(computeDialogism, useBigrams);
            AbstractDocument conversationDocument = QueryHelper.generateDocument(conversation.getText(), lang, models, usePosTagging, computeDialogism, useBigrams);
            QueryResultCscl queryResult = new QueryResultCscl();
            queryResult.setData(CSCL.getAll(conversationDocument, conversation, minThreshold));
            response.type("application/json");
            return queryResult.convertToJson();
        });
        Spark.post("/textCategorization", (request, response) -> {
            Set<String> requiredParams = setInitialRequiredParams();
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());
            requiredParams.add("uri");
            requiredParams.add("threshold");
            QueryResult error = errorIfParamsMissing(requiredParams, json.keySet());
            if (error != null) {
                return error.convertToJson();
            }

            Map<String, String> hm = hmParams(json);
            Lang lang = Lang.getLang(hm.get("language"));
            Boolean usePosTagging = Boolean.parseBoolean(hm.get("pos-tagging"));
            Boolean computeDialogism = Boolean.parseBoolean(hm.get("dialogism"));
            Boolean useBigrams = false;
            String lsaCorpora = "", ldaCorpora = "", w2vCorpora = "";
            if (hm.get("lsa") != null && (hm.get("lsa").compareTo("") != 0)) {
                lsaCorpora = hm.get("lsa");
            }
            if (hm.get("lda") != null && (hm.get("lda").compareTo("") != 0)) {
                ldaCorpora = hm.get("lda");
            }
            if (hm.get("w2v") != null && (hm.get("w2v").compareTo("") != 0)) {
                w2vCorpora = hm.get("w2v");
            }
            List<ISemanticModel> models = QueryHelper.loadSemanticModels(lang, lsaCorpora, ldaCorpora, w2vCorpora);
            double minThreshold;
            try {
                minThreshold = Double.parseDouble(hm.get("threshold"));
            } catch (NullPointerException e) {
                minThreshold = 0.3;
            }
            String documentContent;
            if (hm.get("uri").contains("http") || hm.get("uri").contains("https") || hm.get("uri").contains("ftp")) {
                documentContent = getTextFromPdf(hm.get("uri"), false).getContent();
            } else {
                documentContent = getTextFromPdf(hm.get("uri"), true).getContent();
            }
            AbstractDocument document = QueryHelper.generateDocument(documentContent, lang, models, usePosTagging, computeDialogism, useBigrams);
            ResultTopic resultTopic = ConceptMap.getKeywords(document, minThreshold, null);
            List<ResultCategory> resultCategories = generateCategories(document, lang, models, usePosTagging, computeDialogism, useBigrams);

            QueryResultTextCategorization queryResult = new QueryResultTextCategorization();
            queryResult.setData(new ResultTextCategorization(resultTopic, resultCategories));
            response.type("application/json");
            return queryResult.convertToJson();
        });
        Spark.post("/cv-cover-processing", (request, response) -> {
            Set<String> requiredParams = setInitialRequiredParams();
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());
            requiredParams.add("cv-file");
            requiredParams.add("cover-file");
            requiredParams.add("threshold");
            QueryResult error = errorIfParamsMissing(requiredParams, json.keySet());
            if (error != null) {
                return error.convertToJson();
            }

            Map<String, String> hm = hmParams(json);
            Lang lang = Lang.getLang(hm.get("language"));
            Boolean usePosTagging = Boolean.parseBoolean(hm.get("pos-tagging"));
            Boolean computeDialogism = Boolean.parseBoolean(hm.get("dialogism"));
            Boolean useBigrams = false;
            String lsaCorpora = "", ldaCorpora = "", w2vCorpora = "";
            if (hm.get("lsa") != null && (hm.get("lsa").compareTo("") != 0)) {
                lsaCorpora = hm.get("lsa");
            }
            if (hm.get("lda") != null && (hm.get("lda").compareTo("") != 0)) {
                ldaCorpora = hm.get("lda");
            }
            if (hm.get("w2v") != null && (hm.get("w2v").compareTo("") != 0)) {
                w2vCorpora = hm.get("w2v");
            }
            List<ISemanticModel> models = QueryHelper.loadSemanticModels(lang, lsaCorpora, ldaCorpora, w2vCorpora);
            double minThreshold;
            try {
                minThreshold = Double.parseDouble(hm.get("threshold"));
            } catch (NullPointerException e) {
                minThreshold = 0.3;
            }
            Map<String, Integer> commonWords = new HashMap<>();
            String cvContent = getTextFromPdf("tmp/" + hm.get("cv-file"), true).getContent();
            AbstractDocument cvDocument = QueryHelper.generateDocument(cvContent, lang, models, usePosTagging, computeDialogism, useBigrams);
            Map<Word, Integer> cvWords = cvDocument.getWordOccurences();
            ResultCvCover result = new ResultCvCover(null, null);
            ResultCvOrCover resultCv = new ResultCvOrCover(null, null);
            resultCv.setConcepts(ConceptMap.getKeywords(cvDocument, minThreshold, null));
            resultCv.setSentiments(webService.services.SentimentAnalysis.computeSentiments(cvDocument, SentimentAnalysis.GRANULARITY_DOCUMENT));
            result.setCv(resultCv);
            String coverContent = getTextFromPdf("tmp/" + hm.get("cover-file"), true).getContent();
            AbstractDocument coverDocument = QueryHelper.generateDocument(coverContent, lang, models, usePosTagging, computeDialogism, useBigrams);
            ResultCvOrCover resultCover = new ResultCvOrCover(null, null);
            resultCover.setConcepts(ConceptMap.getKeywords(coverDocument, Double.parseDouble(hm.get("threshold")), null));
            resultCover.setSentiments(webService.services.SentimentAnalysis.computeSentiments(coverDocument,  SentimentAnalysis.GRANULARITY_DOCUMENT));
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
            QueryResultCvCover queryResult = new QueryResultCvCover();
            queryResult.setData(result);
            response.type("application/json");
            return queryResult.convertToJson();
        });
        Spark.post("/cv-processing", (request, response) -> {
            Set<String> socialNetworksLinks = new HashSet<>();
            socialNetworksLinks.add("LinkedIn");
            socialNetworksLinks.add("Viadeo");

            Set<String> requiredParams = setInitialRequiredParams();
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());
            // additional required parameters
            requiredParams.add("cv-file");
            requiredParams.add("threshold");
            // check whether all the required parameters are available
            QueryResult error = errorIfParamsMissing(requiredParams, json.keySet());
            if (error != null) {
                return error.convertToJson();
            }

            Map<String, String> hm = hmParams(json);
            Lang lang = Lang.getLang(hm.get("language"));
            Boolean usePosTagging = Boolean.parseBoolean(hm.get("pos-tagging"));
            Boolean computeDialogism = Boolean.parseBoolean(hm.get("dialogism"));
            Boolean useBigrams = false;
            String lsaCorpora = "", ldaCorpora = "", w2vCorpora = "";
            if (hm.get("lsa") != null && (hm.get("lsa").compareTo("") != 0)) {
                lsaCorpora = hm.get("lsa");
            }
            if (hm.get("lda") != null && (hm.get("lda").compareTo("") != 0)) {
                ldaCorpora = hm.get("lda");
            }
            if (hm.get("w2v") != null && (hm.get("w2v").compareTo("") != 0)) {
                w2vCorpora = hm.get("w2v");
            }
            List<ISemanticModel> models = QueryHelper.loadSemanticModels(lang, lsaCorpora, ldaCorpora, w2vCorpora);
            Double minThreshold;
            try {
                minThreshold = Double.parseDouble(hm.get("threshold"));
            } catch (NullPointerException e) {
                minThreshold = 0.3;
            }
            Set<String> keywordsList = new HashSet<>(Arrays.asList(hm.get("keywords").split(",")));
            Set<String> ignoreList = new HashSet<>(Arrays.asList(hm.get("ignore").split(",")));
            Set<String> ignoreLines = new HashSet<>(Arrays.asList(CVConstants.IGNORE_LINES.split(",")));

            String keywordsText = hm.get("keywords");
            AbstractDocument keywordsDocument = QueryHelper.generateDocument(keywordsText, lang, models, usePosTagging, computeDialogism, useBigrams);

            PdfToTxtConverter pdfToTxtConverter = new PdfToTxtConverter("tmp/" + hm.get("cv-file"), true);
            pdfToTxtConverter.process();
            // ignore lines containing at least one of the words in the ignoreList list
            pdfToTxtConverter.removeLines(ignoreLines);
            pdfToTxtConverter.extractSocialLinks(socialNetworksLinks);

            AbstractDocument cvDocument = QueryHelper.generateDocument(pdfToTxtConverter.getCleanedText(), lang, models, usePosTagging, computeDialogism, useBigrams);

            QueryResultCv queryResult = new QueryResultCv();
            ResultCv result = CVHelper.process(cvDocument, keywordsDocument, pdfToTxtConverter, keywordsList, ignoreList, lang, models, usePosTagging, computeDialogism, useBigrams, minThreshold, CVConstants.FAN_DELTA);
            result.setText(cvDocument.getText());
            result.setProcessedText(cvDocument.getProcessedText());
            result.setSocialNetworksLinksFound(pdfToTxtConverter.getSocialNetworkLinks());

            StringBuilder sb = new StringBuilder();
            boolean keywordWarning = false;
            sb.append(ResourceBundle.getBundle("utils.localization.cv_errors").getString("keyword_recommendation"));
            for (String keyword : keywordsList) {
                boolean found = false;
                for (ResultKeyword resultKeyword : result.getKeywords()) {
                    if (keyword.equals(resultKeyword.getName())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    if (!keywordWarning) {
                        keywordWarning = true;
                    }
                    sb.append(keyword).append(", ");
                }
            }
            if (keywordWarning) {
                result.getWarnings().add(sb.toString());
            }

            if (result.getPages() > 2) {
                result.getWarnings().add(ResourceBundle.getBundle("utils.localization.cv_errors").getString("too_many_pages"));
            }

            if (pdfToTxtConverter.getSocialNetworkLinks().get("LinkedIn") == null) {
                result.getWarnings().add(ResourceBundle.getBundle("utils.localization.cv_errors").getString("social_network_linkedin_not_found"));
            }
            if (pdfToTxtConverter.getSocialNetworkLinks().get("Viadeo") == null) {
                result.getWarnings().add(ResourceBundle.getBundle("utils.localization.cv_errors").getString("social_network_viadeo_not_found"));
            }

            queryResult.setData(result);

            response.type("application/json");
            return queryResult.convertToJson();
        });
        Spark.post("/job-quest", (request, response) -> {
            Double notEnoughWords = .5;
            Double tooManyWords = 1.5;

            Integer juniorAvgWords = 320;
            Integer seniorAvgWords = 376;
            Set<String> socialNetworksLinks = new HashSet<>();
            socialNetworksLinks.add("LinkedIn");
            socialNetworksLinks.add("Viadeo");

            Set<String> competencesSectionTitles = new HashSet<>();
            competencesSectionTitles.add("Compétences");
            competencesSectionTitles.add("Competences");
            competencesSectionTitles.add("Compétence");
            competencesSectionTitles.add("Competence");
            competencesSectionTitles.add("Skills");

            Set<String> requiredParams = setInitialRequiredParams();
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());
            requiredParams.add("experience"); // junior or senior
            requiredParams.add("cv-file");
            requiredParams.add("threshold");
            QueryResult error = errorIfParamsMissing(requiredParams, json.keySet());
            if (error != null) {
                return error.convertToJson();
            }

            Map<String, String> hm = hmParams(json);
            Lang lang = Lang.getLang(hm.get("language"));
            Boolean usePosTagging = Boolean.parseBoolean(hm.get("pos-tagging"));
            Boolean computeDialogism = Boolean.parseBoolean(hm.get("dialogism"));
            Boolean useBigrams = false;
            String lsaCorpora = "", ldaCorpora = "", w2vCorpora = "";
            if (hm.get("lsa") != null && (hm.get("lsa").compareTo("") != 0)) {
                lsaCorpora = hm.get("lsa");
            }
            if (hm.get("lda") != null && (hm.get("lda").compareTo("") != 0)) {
                ldaCorpora = hm.get("lda");
            }
            if (hm.get("w2v") != null && (hm.get("w2v").compareTo("") != 0)) {
                w2vCorpora = hm.get("w2v");
            }
            List<ISemanticModel> models = QueryHelper.loadSemanticModels(lang, lsaCorpora, ldaCorpora, w2vCorpora);
            Integer experience = Integer.parseInt(hm.get("experience"));
            Double minThreshold;
            try {
                minThreshold = Double.parseDouble(hm.get("threshold"));
            } catch (NullPointerException e) {
                minThreshold = 0.3;
            }
            Set<String> keywordsList = new HashSet<>(Arrays.asList(hm.get("keywords").split(",")));
            Set<String> ignoreList = new HashSet<>(Arrays.asList(hm.get("ignore").split(",")));
            Set<String> ignoreLines = new HashSet<>(Arrays.asList(CVConstants.IGNORE_LINES.split(",")));

            String keywordsText = hm.get("keywords");
            AbstractDocument keywordsDocument = QueryHelper.generateDocument(keywordsText, lang, models, usePosTagging, computeDialogism, useBigrams);

            PdfToTxtConverter pdfToTxtConverter = new PdfToTxtConverter("tmp/" + hm.get("cv-file"), true);
            pdfToTxtConverter.process();
            // ignore lines containing at least one of the words in the ignoreList list
            pdfToTxtConverter.removeLines(ignoreLines);
            pdfToTxtConverter.extractSocialLinks(socialNetworksLinks);

            AbstractDocument cvDocument = QueryHelper.generateDocument(pdfToTxtConverter.getCleanedText(), lang, models, usePosTagging, computeDialogism, useBigrams);

            QueryResultJobQuest queryResult = new QueryResultJobQuest();
            ResultJobQuest result = JobQuestHelper.process(cvDocument, keywordsDocument, pdfToTxtConverter, keywordsList, ignoreList, lang, models, usePosTagging, computeDialogism, useBigrams, minThreshold, CVConstants.FAN_DELTA, CVConstants.FAN_DELTA_VERY);
            result.setSocialNetworksLinksFound(pdfToTxtConverter.getSocialNetworkLinks());

            StringBuilder sb = new StringBuilder();
            boolean keywordWarning = false;
            sb.append(ResourceBundle.getBundle("utils.localization.cv_errors").getString("keyword_recommendation"));
            for (String keyword : keywordsList) {
                boolean found = false;
                for (ResultKeyword resultKeyword : result.getKeywords()) {
                    if (keyword.equals(resultKeyword.getName())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    if (!keywordWarning) {
                        keywordWarning = true;
                    }
                    sb.append(keyword).append(", ");
                }
            }
            if (keywordWarning) {
                result.getWarnings().add(sb.toString());
            }

            if (pdfToTxtConverter.getSocialNetworkLinks().get("LinkedIn") == null) {
                result.getWarnings().add(ResourceBundle.getBundle("utils.localization.cv_errors").getString("social_network_linkedin_not_found"));
            }
            if (pdfToTxtConverter.getSocialNetworkLinks().get("Viadeo") == null) {
                result.getWarnings().add(ResourceBundle.getBundle("utils.localization.cv_errors").getString("social_network_viadeo_not_found"));
            }

            if (experience == 0) {
                if (result.getPages() > 1) {
                    result.getWarnings().add(ResourceBundle.getBundle("utils.localization.cv_errors").getString("junior_too_many_pages"));
                }
            } else if (experience == 1) {
                if (result.getPages() == 1) {
                    result.getWarnings().add(ResourceBundle.getBundle("utils.localization.cv_errors").getString("senior_too_few_pages"));
                } else if (result.getPages() > 3) {
                    result.getWarnings().add(ResourceBundle.getBundle("utils.localization.cv_errors").getString("senior_too_many_pages"));
                }
            }

            if (!pdfToTxtConverter.sectionExists(competencesSectionTitles)) {
                result.getWarnings().add(ResourceBundle.getBundle("utils.localization.cv_errors").getString("competences_not_found"));
            }

            if (experience == 0) {
                if (result.getWords() < notEnoughWords * juniorAvgWords) {
                    result.getWarnings().add(ResourceBundle.getBundle("utils.localization.cv_errors").getString("junior_too_few_words"));
                } else if (result.getWords() > tooManyWords * juniorAvgWords) {
                    result.getWarnings().add(ResourceBundle.getBundle("utils.localization.cv_errors").getString("junior_too_many_words"));
                }
            } else if (experience == 1) {
                if (result.getWords() < notEnoughWords * seniorAvgWords) {
                    result.getWarnings().add(ResourceBundle.getBundle("utils.localization.cv_errors").getString("senior_too_few_words"));
                } else if (result.getWords() > tooManyWords * seniorAvgWords) {
                    result.getWarnings().add(ResourceBundle.getBundle("utils.localization.cv_errors").getString("senior_too_many_words"));
                }
            }

            if (result.getColors() > 3) {
                result.getWarnings().add(ResourceBundle.getBundle("utils.localization.cv_errors").getString("too_many_colors"));
            }

            if (result.getImages() > 10) {
                result.getWarnings().add(ResourceBundle.getBundle("utils.localization.cv_errors").getString("too_many_images"));
            }

            queryResult.setData(result);
            response.type("application/json");
            return queryResult.convertToJson();
        });
        // File Upload - send file as multipart form-data to be accepted
        Spark.post("/file-upload", (request, response) -> {
            MultipartConfigElement multipartConfigElement = new MultipartConfigElement("/tmp");
            request.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);
            Part file = request.raw().getPart("file"); // file is name of the
            // input in the upload formx
            QueryResultFile queryResult = new QueryResultFile();
            ResultFile result = FileProcessor.getInstance().saveFile(file);
            List<CVFeedback> cvFeedback = CVValidation.validateFileSize(result.getSize());
            for (CVFeedback feedback : cvFeedback) {
                if (feedback.getFatal() == true) {
                    result.getErrors().add(feedback.getFeedback());
                    queryResult.setErrorMsg(ResourceBundle.getBundle("utils.localization.cv_errors").getString("error_general"));
                    queryResult.setSuccess(false);
                } else {
                    result.getWarnings().add(feedback.getFeedback());
                }
            }
            queryResult.setData(result);
            response.type("application/json");
            return queryResult.convertToJson();
        });
        Spark.options("/file-upload", (request, response) -> {
            return "";
        });
        Spark.get("/file-download", (request, response) -> {
            QueryResult error;
            if (request.queryParams().isEmpty()) {
                error = errorEmptyBody();
            } else {
                Set<String> requiredParams = new HashSet<>();
                requiredParams.add("file");
                error = errorIfParamsMissing(requiredParams, request.queryParams());
            }
            if (error != null) {
                return error.convertToJson();
            }

            Set<String> notEmptyParams = new HashSet<>();
            notEmptyParams.add("file");
            error = errorIfParamsEmpty(notEmptyParams);
            if (error != null) {
                return error.convertToJson();
            }
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
        Spark.post("/vcop", (Request request, Response response) -> {
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

            Community communityStartEnd = Community.loadMultipleConversations(communityFolder.toString(), Lang.en, true, startDate, endDate,
                    (int) monthIncrement, (int) dayIncrement);
            communityStartEnd.computeMetrics(useTextualComplexity, true, true);

            List<Community> subCommunities = communityStartEnd.getTimeframeSubCommunities();

            Date startDateAllCommunities = format.parse("01/01/1970");
            Date endDateAllCommunities = format.parse("01/01/2099");

            Community allCommunity = Community.loadMultipleConversations(communityFolder.toString(), Lang.en, true, startDateAllCommunities,
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
            return result;
        });
        Spark.post("/vcopD3", (request, response) -> {
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());

            response.type("application/json");

            StringBuilder communityFolder = new StringBuilder();
            communityFolder.append("resources/in/Reddit/");
            String communityName = (String) json.get("community");
            communityFolder.append(communityName);
            Boolean useTextualComplexity = (Boolean) json.get("useTextualComplexity");

            Community community = Community.loadMultipleConversations(communityFolder.toString(), Lang.en, true, null, null,
                    0, 7);
            community.computeMetrics(useTextualComplexity, true, true);

            JSONArray participantsCommunities = community.generateParticipantViewSubCommunities(communityFolder.toString());

            QueryResultCommunityParticipants queryResult = new QueryResultCommunityParticipants();
            queryResult.setParticipants(participantsCommunities);
            response.type("application/json");
            return queryResult.convertToJson();
        });
        Spark.post("/vcopD3Week", (request, response) -> {
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());

            response.type("application/json");

            StringBuilder communityFolder = new StringBuilder();
            communityFolder.append("resources/in/Reddit/");
            String communityName = (String) json.get("community");
            Integer weekNumber = Integer.valueOf((String) json.get("week"));

            JSONArray participantsCommunities = new JSONArray();
            JSONParser parser = new JSONParser();
            try {
                String fileName = communityFolder + communityName + "/" + communityName + "_d3_" + weekNumber + ".json";
                LOGGER.log(Level.INFO, "Get participants for week {0} from file {1}", new Object[]{weekNumber, fileName});
                Object obj = parser.parse(new FileReader(fileName));
                JSONObject participantSubCommunity = (JSONObject) obj;
                JSONObject subCommunityJson = new JSONObject();
                subCommunityJson.put("week", weekNumber);
                subCommunityJson.put("participants", participantSubCommunity);
                participantsCommunities.add(subCommunityJson);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            QueryResultCommunityParticipants queryResult = new QueryResultCommunityParticipants();
            queryResult.setParticipants(participantsCommunities);
            response.type("application/json");
            return queryResult.convertToJson();
        });
        Spark.post("/text-similarity", (request, response) -> {
            Set<String> requiredParams = new HashSet<>();
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());
            requiredParams.add("text1");
            requiredParams.add("text2");
            requiredParams.add("language");
            requiredParams.add("model");
            requiredParams.add("corpus");
            // check whether all the required parameters are available
            errorIfParamsMissing(requiredParams, json.keySet());

            Map<String, String> hm = hmParams(json);
            String text1 = hm.get("text1");
            String text2 = hm.get("text2");
            Lang lang = Lang.getLang(hm.get("language"));

            QueryResultTextSimilarity queryResult = new QueryResultTextSimilarity();
            queryResult.setData(generateTextSimilarity(text1, text2, lang, hm.get("model"), hm.get("corpus")));

            response.type("application/json");
            return queryResult.convertToJson();
        });
        Spark.post("/similar-concepts", (request, response) -> {
            Set<String> requiredParams = new HashSet<>();
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());
            requiredParams.add("seed");
            requiredParams.add("language");
            requiredParams.add("model");
            requiredParams.add("corpus");
            errorIfParamsMissing(requiredParams, json.keySet());

            Map<String, String> hm = hmParams(json);
            String seed = hm.get("seed");
            Lang lang = Lang.getLang(hm.get("language"));
            String semanticCorpora = "";
            if (hm.get("model") != null && (hm.get("model").compareTo("") != 0)) {
                semanticCorpora = hm.get("model");
            }
            String corpus = hm.get("corpus");
            double minThreshold;
            try {
                minThreshold = Double.parseDouble(hm.get("threshold"));
            } catch (NullPointerException e) {
                minThreshold = 0.3;
            }
            QueryResultSimilarConcepts queryResult = new QueryResultSimilarConcepts();
            queryResult.setData(generateSimilarConcepts(seed, lang, semanticCorpora, corpus, minThreshold));
            response.type("application/json");
            return queryResult.convertToJson();
        });
        // In contrast to textSimilarity, this endpoint returns similarity
        // scores of two texts using every similarity method available
        Spark.post("/text-similarities", (request, response) -> {
            Set<String> requiredParams = new HashSet<>();
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());
            requiredParams.add("text1");
            requiredParams.add("text2");
            requiredParams.add("language");
            requiredParams.add("pos-tagging");
            requiredParams.add("lsa");
            requiredParams.add("lda");
            requiredParams.add("word2vec");
            // check whether all the required parameters are available
            QueryResult error = errorIfParamsMissing(requiredParams, json.keySet());
            if (error != null) {
                return error.convertToJson();
            }

            Map<String, String> hm = hmParams(json);
            String text1 = hm.get("text1");
            String text2 = hm.get("text2");
            Lang lang = Lang.getLang(hm.get("language"));
            Boolean usePosTagging = Boolean.parseBoolean(hm.get("pos-tagging"));
            Boolean computeDialogism = Boolean.parseBoolean(hm.get("dialogism"));
            String lsaCorpora = "", ldaCorpora = "", w2vCorpora = "";
            if (hm.get("lsa") != null && (hm.get("lsa").compareTo("") != 0)) {
                lsaCorpora = hm.get("lsa");
            }
            if (hm.get("lda") != null && (hm.get("lda").compareTo("") != 0)) {
                ldaCorpora = hm.get("lda");
            }
            if (hm.get("w2v") != null && (hm.get("w2v").compareTo("") != 0)) {
                w2vCorpora = hm.get("w2v");
            }
            List<ISemanticModel> models = QueryHelper.loadSemanticModels(lang, lsaCorpora, ldaCorpora, w2vCorpora);
            QueryResultTextSimilarities queryResult = new QueryResultTextSimilarities();
            queryResult.setData(textSimilarities(text1, text2, lang, models, usePosTagging));
            response.type("application/json");
            return queryResult.convertToJson();
        });

        Spark.post("/answer-matching", (request, response) -> {
            Set<String> requiredParams = new HashSet<>();
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());
            requiredParams.add("language");
            requiredParams.add("pos-tagging");
            requiredParams.add("dialogism");
            requiredParams.add("lsa");
            requiredParams.add("lda");
            requiredParams.add("w2v");
            requiredParams.add("user-answer");
            requiredParams.add("predefined-answers");
            QueryResult error = errorIfParamsMissing(requiredParams, json.keySet());
            if (error != null) {
                return error.convertToJson();
            }

            Map<String, String> hm = hmParams(json);
            Lang lang = Lang.getLang(hm.get("language"));
            Boolean usePosTagging = Boolean.parseBoolean(hm.get("pos-tagging"));
            Boolean computeDialogism = Boolean.parseBoolean(hm.get("dialogism"));
            Boolean useBigrams = false;
            String lsaCorpora = "", ldaCorpora = "", w2vCorpora = "";
            if (hm.get("lsa") != null && (hm.get("lsa").compareTo("") != 0)) {
                lsaCorpora = hm.get("lsa");
            }
            if (hm.get("lda") != null && (hm.get("lda").compareTo("") != 0)) {
                ldaCorpora = hm.get("lda");
            }
            if (hm.get("w2v") != null && (hm.get("w2v").compareTo("") != 0)) {
                w2vCorpora = hm.get("w2v");
            }
            List<ISemanticModel> models = QueryHelper.loadSemanticModels(lang, lsaCorpora, ldaCorpora, w2vCorpora);
            String userAnswer = hm.get("user-answer");
            List<String> predefinedAnswers = (ArrayList<String>) json.get("predefined-answers");
            List<AbstractDocument> predefinedAnswerDocuments = new ArrayList<>();
            for (String answer : predefinedAnswers) {
                predefinedAnswerDocuments.add(QueryHelper.generateDocument(answer, lang, models, usePosTagging, computeDialogism, useBigrams));
            }
            AbstractDocument userAnswerDocument = QueryHelper.generateDocument(userAnswer, lang, models, usePosTagging, computeDialogism, useBigrams);
            QueryResultAnswerMatching queryResult = new QueryResultAnswerMatching();
            queryResult.setData(computeScoresPerAnswer(userAnswerDocument, predefinedAnswerDocuments));
            response.type("application/json");
            return queryResult.convertToJson();
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
            LOGGER.log(Level.INFO, "[Before filter] nodes = {0} edges = {1}", new Object[]{graph.nodeList.size(), graph.edgeList.size()});
            graph = graphFilter.filterGraph(graph, centerUri, noAuthors, noArticles, showAuthors, showArticles);
            LOGGER.log(Level.INFO, "[After filter] nodes = {0} edges = {1}", new Object[]{graph.nodeList.size(), graph.edgeList.size()});
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
        Spark.get("/lak/years", (request, response) -> {
            TwoModeGraphBuilder graphBuilder = TwoModeGraphBuilder.getLakCorpusTwoModeGraphBuilder();
            List<ResearchArticle> articles = graphBuilder.getArticles();
            Set<Integer> yearSet = new HashSet();
            articles.forEach((article) -> {
                if (article.getDate() != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(article.getDate());
                    yearSet.add(cal.get(Calendar.YEAR));
                }
            });
            List<Integer> yearList = new ArrayList();
            yearList.addAll(yearSet);
            Collections.sort(yearList);
            QueryResultDocumentYears queryResult = new QueryResultDocumentYears(yearList);
            response.type("application/json");
            return queryResult.convertToJson();
        });
        Spark.get("/lak/topicEvolution", (request, response) -> {
            TwoModeGraphBuilder graphBuilder = TwoModeGraphBuilder.getLakCorpusTwoModeGraphBuilder();
            List<ResearchArticle> articles = graphBuilder.getArticles();
            TopicEvolutionBuilder builder = new TopicEvolutionBuilder(articles);
            TopicEvolution topicEvolution = builder.build();
            QueryResultTopicEvolution queryResult = new QueryResultTopicEvolution(topicEvolution);
            response.type("application/json");
            return queryResult.convertToJson();
        });

        Spark.post("/lak/topics", (request, response) -> {
            TwoModeGraphBuilder graphBuilder = TwoModeGraphBuilder.getLakCorpusTwoModeGraphBuilder();
            List<ResearchArticle> articles = graphBuilder.getArticles();
            final List<ResearchArticle> filteredArticles = new ArrayList();
            double threshold = 0.4;
            try {
                JSONObject json = (JSONObject) new JSONParser().parse(request.body());
                int year = ((Long) json.get("year")).intValue();
                articles.forEach((article) -> {
                    if (article.getDate() != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(article.getDate());
                        if (cal.get(Calendar.YEAR) == year) {
                            filteredArticles.add(article);
                        }
                    }
                });
                threshold = (Double) json.get("threshold");
            } catch (Exception e) {
                filteredArticles.addAll(articles);
            }
            QueryResultTopic queryResult = new QueryResultTopic();
            ResultTopic resultTopic = ConceptMap.getKeywords(filteredArticles, threshold, 25);
            queryResult.setData(resultTopic);
            response.type("application/json");
            return queryResult.convertToJson();
        });

        Spark.post("/ciModel", (request, response) -> {
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());
            Map<String, String> hm = hmParams(json);

            double minActivationThreshold;
            try {
                minActivationThreshold = Double.parseDouble(hm.get("minActivationThreshold"));
            } catch (NullPointerException e) {
                minActivationThreshold = 0.3;
            }

            int maxSemanticExpand;
            try {
                maxSemanticExpand = Integer.parseInt(hm.get("maxSemanticExpand"));
            } catch (Exception e) {
                maxSemanticExpand = 5;
            }

            ComprehensionModelService cmService = new ComprehensionModelService(minActivationThreshold, maxSemanticExpand);
            CMResult result = cmService.run(hm.get("text"));
            QueryResultCM queryResult = new QueryResultCM(result);
            String resultStr = queryResult.convertToJson();
            return resultStr;
        });
        Spark.get("/community/communities", (request, response) -> {
//            List<com.readerbench.solr.entities.cscl.Community> communities = SOLR_SERVICE_COMMUNITY.getCommunities();
//
//            Map<String, List<com.readerbench.solr.entities.cscl.Community>> results =
//                    new HashMap<String, List<com.readerbench.solr.entities.cscl.Community>>();
//            for (com.readerbench.solr.entities.cscl.Community community : communities) {
//                if (results.get(community.getCategoryName()) != null) {
//                    results.get(community.getCategoryName()).add(community);
//                } else {
//                    results.put(community.getCategoryName(), Arrays.asList(community));
//                }
//            }
            webService.services.cscl.result.dto.Community community1
                    = new webService.services.cscl.result.dto.Community("prisonarchitect", "Prison Architect");
            webService.services.cscl.result.dto.Community community2
                    = new webService.services.cscl.result.dto.Community("ThisWarofMine_2014", "This War of Mine");

            webService.services.cscl.result.dto.Community community3
                    = new webService.services.cscl.result.dto.Community("mathequalslove.blogspot.ro", "Math Equals Love");
            webService.services.cscl.result.dto.Community community4
                    = new webService.services.cscl.result.dto.Community("MOOC", "Massive Open Online Courses");

            webService.services.cscl.result.dto.Category category1
                    = new webService.services.cscl.result.dto.Category("online communities", "Online Communities",
                            Arrays.asList(community1, community2));
            webService.services.cscl.result.dto.Category category2
                    = new webService.services.cscl.result.dto.Category("OKBC", "Online Knowledge Building Community",
                            Arrays.asList(community3));
            webService.services.cscl.result.dto.Category category3
                    = new webService.services.cscl.result.dto.Category("MOOC", "Massive Open Online Courses",
                            Arrays.asList(community4));
            List<webService.services.cscl.result.dto.Category> categories = Arrays.asList(category1, category2, category3);
            QueryResultAllCommunities queryResult = new QueryResultAllCommunities(categories);
            response.type("application/json");
            return queryResult.convertToJson();

        });
        Spark.post("/community/participants", (request, response) -> {
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());
            Map<String, String> hm = hmParams(json);

            String communityName = hm.get("communityName");
            Integer week = Integer.valueOf(hm.get("week"));

            List<Map> participantsStats = elasticsearchService.searchParticipantsStatsPerWeek(communityName, week);

            List<Map> filteredParticipants = participantsStats.stream()
                    .filter(p -> Float.valueOf(p.get("Contrib").toString()) > 0)
                    .collect(Collectors.toList());

            List<Map> unique = new ArrayList<Map>();
            for (Map map : filteredParticipants) {
                if (!elasticsearchService.isDuplicate(map, unique)) {
                    unique.add(map);
                }
            }

            QueryResultParticipants queryResult = new QueryResultParticipants(unique);
            response.type("application/json");
            return queryResult.convertToJson();
        });
        Spark.post("/community/participants/directedGraph", (request, response) -> {
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());
            Map<String, String> hm = hmParams(json);

            String communityName = hm.get("communityName");

            List<Map> participantsInteraction = elasticsearchService.searchParticipantsGraphRepresentation(
                    "participants", "directedGraph", communityName);

//            List<Map> filteredParticipantInteraction = participantsInteraction.stream()
//                    .filter(p ->  ((List)p.get("nodes")).size() > 0)
//                    .collect(Collectors.toList());
            QueryResultParticipantsInteraction queryResult = new QueryResultParticipantsInteraction(participantsInteraction);
            response.type("application/json");
            return queryResult.convertToJson();
        });
        Spark.post("/community/participants/edgeBundling", (request, response) -> {
            JSONObject json = (JSONObject) new JSONParser().parse(request.body());
            Map<String, String> hm = hmParams(json);

            String communityName = hm.get("communityName");

            List<Map> participantsInteraction = elasticsearchService.searchParticipantsGraphRepresentation(
                    "participants", "edgeBundling", communityName);
//            List<Map> filteredParticipantInteraction = participantsInteraction.stream()
//                    .filter(p -> ((List) p.get("data")).size() > 0)
//                    .collect(Collectors.toList());

            QueryResultParticipantsInteraction queryResult = new QueryResultParticipantsInteraction(participantsInteraction);
            response.type("application/json");
            return queryResult.convertToJson();
        });

    }

}
