package webService;

import java.awt.Color;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import dao.CategoryDAO;
import dao.WordDAO;
import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.Block;
import data.Lang;
import data.Word;
import data.cscl.Conversation;
import data.discourse.SemanticCohesion;
import data.document.Document;
import data.document.Summary;
import data.pojo.Category;
import data.pojo.CategoryPhrase;
import data.sentiment.SentimentWeights;
import services.commons.Formatting;
import services.converters.PdfToTextConverter;
import services.readingStrategies.ReadingStrategies;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import spark.Spark;
import webService.cv.CVHelper;
import webService.keywords.KeywordsHelper;
import webService.query.QueryHelper;
import webService.queryResult.QueryResultCscl;
import webService.queryResult.QueryResultCv;
import webService.queryResult.QueryResultCvCover;
import webService.queryResult.QueryResultSearch;
import webService.queryResult.QueryResultSelfExplanation;
import webService.queryResult.QueryResultSemanticAnnotation;
import webService.queryResult.QueryResultSentiment;
import webService.queryResult.QueryResultTextCategorization;
import webService.queryResult.QueryResultTextualComplexity;
import webService.queryResult.QueryResultTopic;
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
import webService.semanticSearch.SearchClient;
import webService.services.ConceptMap;
import webService.services.TextualComplexity;
import webService.services.cscl.Cscl;
import webService.services.utils.FileProcessor;

public class ReaderBenchServer {
	private static Logger logger = Logger.getLogger(ReaderBenchServer.class);
	public static final int PORT = 8080;

	public static final Color COLOR_TOPIC = new Color(204, 204, 204); // silver
	public static final Color COLOR_INFERRED_CONCEPT = new Color(102, 102, 255); // orchid

	private static List<AbstractDocument> loadedDocs;
	private static String loadedPath;

	public List<ResultCategory> getCategories(String documentContent, String pathToLSA, String pathToLDA, Lang lang,
			boolean usePOSTagging, boolean computeDialogism, double threshold) {

		List<ResultCategory> resultCategories = new ArrayList<ResultCategory>();

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

			AbstractDocument queryCategory = QueryHelper.processQuery(sb.toString(), pathToLSA, pathToLDA, lang, usePOSTagging,
					computeDialogism);
			SemanticCohesion sc = new SemanticCohesion(queryCategory, queryDoc);
			resultCategories.add(new ResultCategory(cat.getLabel(), Formatting.formatNumber(sc.getCohesion())));
		}

		Collections.sort(resultCategories, ResultCategory.ResultCategoryRelevanceComparator);
		return resultCategories;
	}

	private ResultSemanticAnnotation getSemanticAnnotation(AbstractDocument abstractDocument, 
			AbstractDocument keywordsDocument,
			AbstractDocument document, 
			Set<String> keywordsList,
			String pathToLSA, String pathToLDA, Lang lang, boolean usePOSTagging,
			boolean computeDialogism, double threshold) {

		// concepts
		ResultTopic resultTopic = ConceptMap.getTopics(document, threshold);
		List<ResultKeyword> resultKeywords = KeywordsHelper.getKeywords(document, keywordsDocument, keywordsList, pathToLSA, pathToLDA, lang,
				usePOSTagging, computeDialogism, threshold);
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
			String pathToLDA, String lang, boolean usePOSTagging, boolean computeDialogism) {

		Document queryInitialText = new Document(null, AbstractDocumentTemplate.getDocumentModel(initialText),
				LSA.loadLSA(pathToLSA, Lang.valueOf(lang)), LDA.loadLDA(pathToLDA, Lang.valueOf(lang)),
				Lang.valueOf(lang), usePOSTagging, false);

		Summary s = new Summary(selfExplanation, queryInitialText, true, true);

		s.computeAll(computeDialogism, false);

		List<ResultReadingStrategy> readingStrategies = new ArrayList<ResultReadingStrategy>();
		for (int i = 0; i < ReadingStrategies.NO_READING_STRATEGIES; i++) {
			readingStrategies.add(new ResultReadingStrategy(ReadingStrategies.STRATEGY_NAMES[i],
					s.getAutomaticReadingStrategies()[0][i]));
		}

		StringBuilder summary = new StringBuilder();
		for (Block b : s.getBlocks()) {
			logger.info("Block alternate text: " + b.getAlternateText());
			summary.append(b.getAlternateText());
			summary.append("<br/>");
		}

		summary.append(s.getAlternateText());

		return new ResultSelfExplanation(summary.toString(), readingStrategies);
	}

	private ResultPdfToText getTextFromPdf(String uri, boolean localFile) {
		PdfToTextConverter pdfConverter = new PdfToTextConverter();
		if (localFile) {
			//return new ResultPdfToText(PdfToTextConverter.pdftoText("resources/papers/" + uri + ".pdf", true));
			return new ResultPdfToText(pdfConverter.pdftoText(uri, true));
		} else {
			return new ResultPdfToText(pdfConverter.pdftoText(uri, false));
		}
	}

	@Root(name = "response")
	private static class QueryResultPdfToText {

		@Element
		private boolean success;

		@Element(name = "errormsg")
		private String errorMsg; // custom error message (optional)

		@Path("data")
		@ElementList(inline = true, entry = "result")
		private ResultPdfToText data;

		private QueryResultPdfToText() {
			success = true;
			errorMsg = "";
			data = new ResultPdfToText("");
		}
	}

	public void start() {
		Spark.port(PORT);
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
			queryResult.setData(ConceptMap.getTopics(
					QueryHelper.processQuery(text, pathToLSA, pathToLDA, Lang.getLang(language), usePOSTagging, computeDialogism), threshold));
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

			// System.out.println("Am primit: " + q);
			QueryResultSentiment queryResult = new QueryResultSentiment();
			queryResult.setData(webService.services.SentimentAnalysis
					.getSentiment(QueryHelper.processQuery(text, pathToLSA, pathToLDA, Lang.getLang(language), usePOSTagging, computeDialogism)));
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
			TextualComplexity textualComplexity = new TextualComplexity(QueryHelper.processQuery(text, pathToLSA, pathToLDA, Lang.getLang(language), usePOSTagging, computeDialogism), 
					Lang.getLang(language), usePOSTagging, computeDialogism);
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
			logger.info("URI primit");
			logger.info(uri);

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
			queryResult.setData(ConceptMap
					.getTopics(QueryHelper.processQuery(q, pathToLSA, pathToLDA, Lang.getLang(language), usePOSTagging, computeDialogism), threshold));
			String result = queryResult.convertToJson();
			// return Charset.forName("UTF-8").encode(result);
			return result;

		});
		Spark.post("/semanticProcessUri", (request, response) -> {
			JSONObject json = (JSONObject) new JSONParser().parse(request.body());

			response.type("application/json");
			
			String uri = (String) json.get("uri");
			String documentContent = null;
			if (uri == null || uri.isEmpty()) {
				logger.error("URI an URL are empty. Aborting...");
				System.exit(-1);
			}
			if (uri.contains("http") || uri.contains("https") || uri.contains("ftp")) {
				documentContent = getTextFromPdf(uri, false).getContent();
			} else {
				documentContent = getTextFromPdf(uri, true).getContent();
			}
			if (uri != null && !uri.isEmpty()) {

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
			
			Set<String> keywordsList = new HashSet<String>(Arrays.asList(keywords.split(",")));

			AbstractDocument document = QueryHelper.processQuery(documentContent, pathToLSA, pathToLDA, lang, usePOSTagging,
					computeDialogism);
			AbstractDocument keywordsDocument = QueryHelper.processQuery(keywords, pathToLSA, pathToLDA, lang, usePOSTagging,
					computeDialogism);
			AbstractDocument abstractDocument = QueryHelper.processQuery(documentAbstract, pathToLSA, pathToLDA, lang, usePOSTagging,
					computeDialogism);
			
			QueryResultSemanticAnnotation queryResult = new QueryResultSemanticAnnotation();
			queryResult.setData(getSemanticAnnotation(abstractDocument, keywordsDocument, document, keywordsList, pathToLSA,
					pathToLDA, Lang.getLang(language), usePOSTagging, computeDialogism, threshold));
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
			
			Set<String> keywordsList = new HashSet<String>(Arrays.asList(keywords.split(",")));

			Lang lang = Lang.getLang(language);

			AbstractDocument document = QueryHelper.processQuery(documentContent, pathToLSA, pathToLDA, lang, usePOSTagging,
					computeDialogism);
			AbstractDocument keywordsDocument = QueryHelper.processQuery(keywords, pathToLSA, pathToLDA, lang, usePOSTagging,
					computeDialogism);
			AbstractDocument abstractDocument = QueryHelper.processQuery(documentAbstract, pathToLSA, pathToLDA, lang, usePOSTagging,
					computeDialogism);
			
			QueryResultSemanticAnnotation queryResult = new QueryResultSemanticAnnotation();
			queryResult.setData(getSemanticAnnotation(abstractDocument, keywordsDocument, document, keywordsList, pathToLSA,
					pathToLDA, Lang.getLang(language), usePOSTagging, computeDialogism, threshold));
			
			String result = queryResult.convertToJson();
			// return Charset.forName("UTF-8").encode(result);
			return result;

		});
		Spark.post("/selfExplanation", (request, response) -> {
			JSONObject json = (JSONObject) new JSONParser().parse(request.body());

			response.type("application/json");

			String text = (String) json.get("text");
			String explanation = (String) json.get("explanation");
			String lang = (String) json.get("lang");
			String pathToLSA = (String) json.get("lsa");
			String pathToLDA = (String) json.get("lda");
			boolean usePOSTagging = (boolean) json.get("postagging");
			boolean computeDialogism = Boolean.parseBoolean(request.queryParams("dialogism"));

			QueryResultSelfExplanation queryResult = new QueryResultSelfExplanation();
			queryResult.setData(getSelfExplanation(text, explanation, pathToLSA, pathToLDA, lang, usePOSTagging,
					computeDialogism));
			String result = queryResult.convertToJson();
			// return Charset.forName("UTF-8").encode(result);
			return result;

		});
		Spark.post("/csclProcessing", (request, response) -> {
			JSONObject json = (JSONObject) new JSONParser().parse(request.body());

			response.type("application/json");

			// String conversationText = (String) json.get("conversation");
			//String conversationPath = (String) json.get("conversationPath");
			String csclFile = (String) json.get("csclFile");
			String language = (String) json.get("lang");
			String pathToLSA = (String) json.get("lsa");
			String pathToLDA = (String) json.get("lda");
			boolean usePOSTagging = (boolean) json.get("postagging");
			boolean computeDialogism = Boolean.parseBoolean(request.queryParams("dialogism"));
			double threshold = (Double) json.get("threshold");

			// AbstractDocumentTemplate contents =
			// Cscl.getConversationText(conversationText);
			// logger.info("Contents: blocks = " + contents.getBlocks().size());
			Lang lang = Lang.getLang(language);
			/*
			 * Conversation conversation = new Conversation( null, contents,
			 * LSA.loadLSA(pathToLSA, lang), LDA.loadLDA(pathToLDA, lang), lang,
			 * usePOSTagging, false);
			 */
			
			Conversation conversation = Conversation.load(new File("tmp/" + csclFile), LSA.loadLSA(pathToLSA, lang),
					LDA.loadLDA(pathToLDA, lang), lang, usePOSTagging, false);
			conversation.computeAll(computeDialogism, null, null, true);
			AbstractDocument conversationDocument = QueryHelper.processQuery(conversation.getText(), pathToLSA, pathToLDA, Lang.getLang(language),
					usePOSTagging, computeDialogism);

			QueryResultCscl queryResult = new QueryResultCscl();
			// queryResult.data =
			// ParticipantInteraction.buildParticipantGraph(conversation);
			queryResult.setData(Cscl.getAll(conversationDocument, conversation, threshold));
			String result = queryResult.convertToJson();
			// return Charset.forName("UTF-8").encode(result);
			return result;

		});
		Spark.post("/textCategorization", (request, response) -> {
			JSONObject json = (JSONObject) new JSONParser().parse(request.body());

			response.type("application/json");

			String uri = (String) json.get("uri");
			// String url = (String) json.get("url");

			/*
			 * QueryResultPdfToText queryResult = new QueryResultPdfToText();
			 * queryResult.data = getTextFromPdf(uri); String result =
			 * convertToJson(queryResult);
			 */

			String documentContent = null;
			if (uri == null || uri.isEmpty()) {
				logger.error("URI an URL are empty. Aborting...");
				System.exit(-1);
			}
			if (uri.contains("http") || uri.contains("https") || uri.contains("ftp")) {
				documentContent = getTextFromPdf(uri, false).getContent();
			} else {
				documentContent = getTextFromPdf(uri, true).getContent();
			}
			if (uri != null && !uri.isEmpty()) {

			}

			ResultTopic resultTopic = null;
			List<ResultCategory> resultCategories = null;

			String language = (String) json.get("lang");
			String pathToLSA = (String) json.get("lsa");
			String pathToLDA = (String) json.get("lda");
			boolean usePOSTagging = (boolean) json.get("postagging");
			boolean computeDialogism = Boolean.parseBoolean(request.queryParams("dialogism"));
			double threshold = (double) json.get("threshold");
			
			resultTopic = ConceptMap.getTopics(
					QueryHelper.processQuery(documentContent, pathToLSA, pathToLDA, Lang.getLang(language), usePOSTagging, computeDialogism), threshold);
			resultCategories = getCategories(documentContent, pathToLSA, pathToLDA, Lang.getLang(language),
					usePOSTagging, computeDialogism, threshold);

			QueryResultTextCategorization queryResult = new QueryResultTextCategorization();
			queryResult.setData(new ResultTextCategorization(resultTopic, resultCategories));
			String result = queryResult.convertToJson();
			// return Charset.forName("UTF-8").encode(result);
			return result;

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

			// AbstractDocumentTemplate contents =
			// Cscl.getConversationText(conversationText);
			// logger.info("Contents: blocks = " + contents.getBlocks().size());
			Lang lang = Lang.getLang(language);
			/*
			 * Conversation conversation = new Conversation( null, contents,
			 * LSA.loadLSA(pathToLSA, lang), LDA.loadLDA(pathToLDA, lang), lang,
			 * usePOSTagging, false);
			 */
			/*Document cvContent = Document.load(new File(cvContent), LSA.loadLSA(pathToLSA, lang),
					LDA.loadLDA(pathToLDA, lang), lang, usePOSTagging, false);
			cvContent.computeAll(computeDialogism, null, null, true);*/
			/*AbstractDocument cvDocument = processQuery(cvContent.getText(), pathToLSA, pathToLDA, language,
					usePOSTagging, computeDialogism);*/
			
			Map<String, Integer> commonWords = new HashMap<String, Integer>();
			
			String cvContent = getTextFromPdf("tmp/" + cvFile, true).getContent();
			AbstractDocument cvDocument = QueryHelper.processQuery(cvContent, pathToLSA, pathToLDA, Lang.getLang(language),
					usePOSTagging, computeDialogism);
			Map<Word, Integer> cvWords = cvDocument.getWordOccurences();
			
			/*Document coverContent = Document.load(new File("tmp/" + coverFile), LSA.loadLSA(pathToLSA, lang),
					LDA.loadLDA(pathToLDA, lang), lang, usePOSTagging, false);
			coverContent.computeAll(computeDialogism, null, null, true);*/
			
			QueryResultCvCover queryResult = new QueryResultCvCover();
			// queryResult.data =
			// ParticipantInteraction.buildParticipantGraph(conversation);
			ResultCvCover result = new ResultCvCover(null, null);
			ResultCvOrCover resultCv = new ResultCvOrCover(null, null);
			resultCv.setConcepts(ConceptMap.getTopics(
					QueryHelper.processQuery(cvContent, pathToLSA, pathToLDA, lang, usePOSTagging, computeDialogism), threshold));
			resultCv.setSentiments(webService.services.SentimentAnalysis
					.getSentiment(QueryHelper.processQuery(cvContent, pathToLSA, pathToLDA, lang, usePOSTagging, computeDialogism)));
			result.setCv(resultCv);
			
			//if (coverFile != null) {
				String coverContent = getTextFromPdf("tmp/" + coverFile, true).getContent();
				AbstractDocument coverDocument = QueryHelper.processQuery(coverContent, pathToLSA, pathToLDA, lang,
						usePOSTagging, computeDialogism);
				
				ResultCvOrCover resultCover = new ResultCvOrCover(null, null);
				resultCover.setConcepts(ConceptMap.getTopics(
						QueryHelper.processQuery(coverContent, pathToLSA, pathToLDA, lang, usePOSTagging, computeDialogism), threshold));
				resultCover.setSentiments(webService.services.SentimentAnalysis
						.getSentiment(QueryHelper.processQuery(cvContent, pathToLSA, pathToLDA, lang, usePOSTagging, computeDialogism)));
				result.setCover(resultCover);
				
				Map<Word, Integer> coverWords = coverDocument.getWordOccurences();
			    
			    Iterator<Entry<Word, Integer>> itCvWords = cvWords.entrySet().iterator();
			    while (itCvWords.hasNext()) {
			        Map.Entry cvPair = (Map.Entry)itCvWords.next();
			        //System.out.println(pair.getKey() + " = " + pair.getValue());
			        Word cvWord = (Word)cvPair.getKey();
			        Integer cvWordOccurences = (Integer)cvPair.getValue();
			        if (coverWords.containsKey(cvWord)) {
			        	commonWords.put(cvWord.getLemma(), cvWordOccurences + coverWords.get(cvWord));
			        }
			        itCvWords.remove(); // avoids a ConcurrentModificationException
			    }
			//}
			result.setWordOccurences(commonWords);
			Map<String, Double> similarity = new HashMap<String, Double>();
			
			// semantic similarity between Cover Letter & CV
			/*SemanticCohesion sc = new SemanticCohesion(cvDocument, coverDocument);
			similarity.put(sc.getSemanticDistances()[1], Formatting.formatNumber(sc.getLSASim()));
			similarity.put("LDA", Formatting.formatNumber(sc.getLDASim()));
			sc.getSemanticDistances()[0]=0;
			for(int i = 0; i < sc.getOntologySim().length; i++)
				similarity.put("LDA", Formatting.formatNumber(sc.getOntologySim()[i]));
			result.setSimilarity(similarity);
			1
			3
			4
			*/
			
			queryResult.setData(result);
			
			return queryResult.convertToJson();
			// return Charset.forName("UTF-8").encode(result);

		});
		Spark.post("/cvProcessing", (request, response) -> {
			JSONObject json = (JSONObject) new JSONParser().parse(request.body());

			response.type("application/json");

			String cvFile = (String) json.get("cvFile");
			String keywords = (String) json.get("keywords");
			String language = (String) json.get("lang");
			String pathToLSA = (String) json.get("lsa");
			String pathToLDA = (String) json.get("lda");
			boolean usePOSTagging = (boolean) json.get("postagging");
			boolean computeDialogism = Boolean.parseBoolean(request.queryParams("dialogism"));
			double threshold = (Double) json.get("threshold");
			
			Set<String> keywordsList = new HashSet<String>(Arrays.asList(keywords.split(",")));
			
			// AbstractDocumentTemplate contents =
			// Cscl.getConversationText(conversationText);
			// logger.info("Contents: blocks = " + contents.getBlocks().size());
			Lang lang = Lang.getLang(language);
			/*
			 * Conversation conversation = new Conversation( null, contents,
			 * LSA.loadLSA(pathToLSA, lang), LDA.loadLDA(pathToLDA, lang), lang,
			 * usePOSTagging, false);
			 */
			/*Document cvContent = Document.load(new File(cvContent), LSA.loadLSA(pathToLSA, lang),
					LDA.loadLDA(pathToLDA, lang), lang, usePOSTagging, false);
			cvContent.computeAll(computeDialogism, null, null, true);*/
			
			PdfToTextConverter pdfConverter = new PdfToTextConverter();
			String cvContent = pdfConverter.pdftoText("tmp/" + cvFile, true);
			
			logger.info("Continut cv: " + cvContent);
			AbstractDocument cvDocument = QueryHelper.processQuery(cvContent, pathToLSA, pathToLDA, lang,
					usePOSTagging, computeDialogism);
			AbstractDocument keywordsDocument = QueryHelper.processQuery(keywords, pathToLSA, pathToLDA, lang,
					usePOSTagging, computeDialogism);
			
			/*Document coverContent = Document.load(new File("tmp/" + coverFile), LSA.loadLSA(pathToLSA, lang),
					LDA.loadLDA(pathToLDA, lang), lang, usePOSTagging, false);
			coverContent.computeAll(computeDialogism, null, null, true);*/
			
			QueryResultCv queryResult = new QueryResultCv();
			// queryResult.data =
			// ParticipantInteraction.buildParticipantGraph(conversation);
			ResultCv result = CVHelper.process(cvDocument, keywordsDocument, pdfConverter, keywordsList, pathToLSA, pathToLDA, lang, usePOSTagging, computeDialogism, threshold);

			queryResult.setData(result);
			
			return queryResult.convertToJson();
			// return Charset.forName("UTF-8").encode(result);

		});
		// File Upload - send file as multipart form-data to be accepted
		Spark.post("/fileUpload", (request, response) -> {
			MultipartConfigElement multipartConfigElement = new MultipartConfigElement("/tmp");
			request.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);
			Part file = request.raw().getPart("file"); //file is name of the input in the upload form
			return FileProcessor.getInstnace().saveFile(file);
		});
		Spark.options("/fileUpload", (request, response) -> {
			return "";
		});
	}

	private static List<AbstractDocument> setDocuments(String path) {
		if (loadedPath != null && loadedPath.equals(path))
			return loadedDocs;

		loadedPath = path;
		loadedDocs = new ArrayList<AbstractDocument>();
		try {
			File dir = new File("resources/in/" + path);
			File[] files = dir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".ser");
				}
			});

			for (File file : files) {
				Document d = (Document) AbstractDocument.loadSerializedDocument(file.getPath());
				loadedDocs.add(d);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return loadedDocs;
	}

	public static void initializeDB() {
		logger.info("Initialize words...");
		WordDAO.getInstance().loadAll();
		logger.info("Words initialization finished");

		SentimentWeights.initialize();
		logger.info("Valence map has " + data.sentiment.SentimentValence.getValenceMap().size()
				+ " sentiments after initialization.");
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.INFO); // changing log level

		ReaderBenchServer.initializeDB();

		ReaderBenchServer server = new ReaderBenchServer();

		server.start();
	}
}
