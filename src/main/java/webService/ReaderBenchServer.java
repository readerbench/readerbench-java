package webService;

import java.awt.Color;
import java.io.File;
import java.io.FilenameFilter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dao.CategoryDAO;
import dao.WordDAO;
import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.Block;
import data.Word;
import data.cscl.Conversation;
import data.discourse.SemanticCohesion;
import data.document.Document;
import data.document.Summary;
import data.pojo.Category;
import data.pojo.CategoryPhrase;
import data.sentiment.SentimentWeights;
import edu.cmu.lti.jawjaw.pobj.Lang;
import services.commons.Formatting;
import services.complexity.ComplexityIndices;
import services.converters.PdfToTextConverter;
import services.readingStrategies.ReadingStrategies;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import spark.Spark;
import webService.result.ResultCategory;
import webService.result.ResultCscl;
import webService.result.ResultKeyword;
import webService.result.ResultPdfToText;
import webService.result.ResultReadingStrategy;
import webService.result.ResultSearch;
import webService.result.ResultSelfExplanation;
import webService.result.ResultSemanticAnnotation;
import webService.result.ResultSentiment;
import webService.result.ResultTopic;
import webService.result.ResultValence;
import webService.semanticSearch.SearchClient;
import webService.services.ConceptMap;
import webService.services.TextualComplexity;
import webService.services.cscl.Cscl;
import webService.services.cscl.ParticipantInteraction;

public class ReaderBenchServer {
	private static Logger logger = Logger.getLogger(ReaderBenchServer.class);
	public static final int PORT = 8080;

	public static final Color COLOR_TOPIC = new Color(204, 204, 204); // silver
	public static final Color COLOR_INFERRED_CONCEPT = new Color(102, 102, 255); // orchid

	private static List<AbstractDocument> loadedDocs;
	private static String loadedPath;

	public AbstractDocument processQuery(String query, String pathToLSA, String pathToLDA, String language,
			boolean posTagging) {
		logger.info("Processign query ...");
		AbstractDocumentTemplate contents = AbstractDocumentTemplate.getDocumentModel(query);

		// Lang lang = Lang.eng;
		Lang lang = Lang.getLang(language);
		AbstractDocument queryDoc = new Document(null, contents, LSA.loadLSA(pathToLSA, lang),
				LDA.loadLDA(pathToLDA, lang), lang, posTagging, false);
		logger.info("Built document has " + queryDoc.getBlocks().size() + " blocks.");
		queryDoc.computeAll(null, null);
		ComplexityIndices.computeComplexityFactors(queryDoc);

		return queryDoc;
	}

	public List<ResultKeyword> getKeywords(String documentKeywords, String documentContent, String pathToLSA,
			String pathToLDA, String lang, boolean usePOSTagging, double threshold) {

		ArrayList<ResultKeyword> resultKeywords = new ArrayList<ResultKeyword>();

		AbstractDocument queryDoc = processQuery(documentContent, pathToLSA, pathToLDA, lang, usePOSTagging);
		AbstractDocument queryKey = processQuery(documentKeywords, pathToLSA, pathToLDA, lang, usePOSTagging);
		queryKey.computeAll(null, null);

		for (Word keyword : queryKey.getWordOccurences().keySet()) {
			AbstractDocument queryKeyword = processQuery(keyword.getLemma(), pathToLSA, pathToLDA, lang, usePOSTagging);
			SemanticCohesion sc = new SemanticCohesion(queryKeyword, queryDoc);
			int occ = 0;
			if (queryDoc.getWordOccurences().containsKey(keyword)) {
				occ = queryDoc.getWordOccurences().get(keyword).intValue();
			}
			resultKeywords.add(new ResultKeyword(keyword.getLemma(), occ, Formatting.formatNumber(sc.getCohesion())));
		}

		Collections.sort(resultKeywords, ResultKeyword.ResultKeywordRelevanceComparator);
		return resultKeywords;

	}

	public List<ResultCategory> getCategories(String documentContent, String pathToLSA, String pathToLDA, String lang,
			boolean usePOSTagging, double threshold) {

		List<ResultCategory> resultCategories = new ArrayList<ResultCategory>();

		AbstractDocument queryDoc = processQuery(documentContent, pathToLSA, pathToLDA, lang, usePOSTagging);
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

			AbstractDocument queryCategory = processQuery(sb.toString(), pathToLSA, pathToLDA, lang, usePOSTagging);
			SemanticCohesion sc = new SemanticCohesion(queryCategory, queryDoc);
			resultCategories.add(new ResultCategory(cat.getLabel(), Formatting.formatNumber(sc.getCohesion())));
		}

		Collections.sort(resultCategories, ResultCategory.ResultCategoryRelevanceComparator);
		return resultCategories;
	}

	private ResultSemanticAnnotation getSemanticAnnotation(String documentAbstract, String documentKeywords,
			String documentContent, String pathToLSA, String pathToLDA, String lang, boolean usePOSTagging,
			double threshold) {

		// concepts
		ResultTopic resultTopic = ConceptMap
				.getTopics(processQuery(documentContent, pathToLSA, pathToLDA, lang, usePOSTagging), threshold);
		List<ResultKeyword> resultKeywords = getKeywords(documentKeywords, documentContent, pathToLSA, pathToLDA, lang,
				usePOSTagging, threshold);
		List<ResultCategory> resultCategories = getCategories(documentContent, pathToLSA, pathToLDA, lang,
				usePOSTagging, threshold);

		AbstractDocument queryDoc = processQuery(documentContent, pathToLSA, pathToLDA, lang, usePOSTagging);
		AbstractDocument queryAbs = processQuery(documentAbstract, pathToLSA, pathToLDA, lang, usePOSTagging);
		AbstractDocument queryKey = processQuery(documentKeywords, pathToLSA, pathToLDA, lang, usePOSTagging);

		// (abstract, document) relevance
		SemanticCohesion scAbstractDocument = new SemanticCohesion(queryAbs, queryDoc);

		// (abstract, keywords) relevance
		SemanticCohesion scKeywordsAbstract = new SemanticCohesion(queryKey, queryAbs);

		// (keywords, document) relevance
		SemanticCohesion scKeywordsDocument = new SemanticCohesion(queryKey, queryDoc);

		ResultSemanticAnnotation rsa = new ResultSemanticAnnotation(resultTopic,
				Formatting.formatNumber(scAbstractDocument.getCohesion()),
				Formatting.formatNumber(scKeywordsAbstract.getCohesion()),
				Formatting.formatNumber(scKeywordsDocument.getCohesion()), resultKeywords, resultCategories);

		return rsa;
	}

	private ResultSelfExplanation getSelfExplanation(String initialText, String selfExplanation, String pathToLSA,
			String pathToLDA, String lang, boolean usePOSTagging) {

		Document queryInitialText = new Document(null, AbstractDocumentTemplate.getDocumentModel(initialText),
				LSA.loadLSA(pathToLSA, Lang.valueOf(lang)), LDA.loadLDA(pathToLDA, Lang.valueOf(lang)),
				Lang.valueOf(lang), usePOSTagging, false);

		Summary s = new Summary(selfExplanation, queryInitialText, true, true);

		s.computeAll(false);

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
		// MS_training_SE_1999
		if (localFile) {
			return new ResultPdfToText(PdfToTextConverter.pdftoText("resources/papers/" + uri + ".pdf", true));
		} else {
			return new ResultPdfToText(PdfToTextConverter.pdftoText(uri, false));
		}
	}

	private String convertToXml(QueryResult queryResult) {
		Serializer serializer = new Persister();
		StringWriter result = new StringWriter();
		try {
			serializer.write(queryResult, result);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return result.toString();
	}

	private String convertToJson(QueryResult queryResult) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
		String json = gson.toJson(queryResult);
		return json;
	}

	private String convertToJson(QueryResultSentiment queryResult) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
		String json = gson.toJson(queryResult);
		return json;
	}

	private String convertToJson(QueryResultSearch queryResult) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
		String json = gson.toJson(queryResult);
		return json;
	}

	private String convertToJson(QueryResultTopic queryResult) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
		String json = gson.toJson(queryResult);
		return json;
	}

	private String convertToJson(QueryResultSemanticAnnotation queryResult) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
		String json = gson.toJson(queryResult);
		return json;
	}

	private String convertToJson(QueryResultSelfExplanation queryResult) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
		String json = gson.toJson(queryResult);
		return json;
	}

	@Root(name = "response")
	private static class QueryResult {

		@Element
		private boolean success;

		@Element(name = "errormsg")
		private String errorMsg; // custom error message (optional)

		@Path("data")
		@ElementList(inline = true, entry = "result")
		private List<ResultValence> data; // list of query results (urls)

		private QueryResult() {
			success = true;
			errorMsg = "";
			data = new ArrayList<ResultValence>();
		}
	}

	@Root(name = "response")
	private static class QueryResultSentiment {

		@Element
		private boolean success;

		@Element(name = "errormsg")
		private String errorMsg; // custom error message (optional)

		@Path("data")
		@ElementList(inline = true, entry = "result")
		private List<ResultSentiment> data; // list of query results (urls)

		private QueryResultSentiment() {
			success = true;
			errorMsg = "";
			data = new ArrayList<ResultSentiment>();
		}
	}

	@Root(name = "response")
	private static class QueryResultSearch {

		@Element
		public boolean success;

		@Element(name = "errormsg")
		public String errorMsg; // custom error message (optional)

		@Path("data")
		@ElementList(inline = true, entry = "result")
		public List<ResultSearch> data; // list of query results (urls)

		QueryResultSearch() {
			success = true;
			errorMsg = "";
			data = new ArrayList<ResultSearch>();
		}
	}

	@Root(name = "response")
	private static class QueryResultTopic {

		@Element
		private boolean success;

		@Element(name = "errormsg")
		private String errorMsg; // custom error message (optional)

		@Path("data")
		@ElementList(inline = true, entry = "result")
		private ResultTopic data; // list of query results (urls)

		private QueryResultTopic() {
			success = true;
			errorMsg = "";
			data = new ResultTopic(null, null);
		}
	}

	@Root(name = "response")
	private static class QueryResultSemanticAnnotation {

		@Element
		private boolean success;

		@Element(name = "errormsg")
		private String errorMsg; // custom error message (optional)

		@Path("data")
		@ElementList(inline = true, entry = "result")
		private ResultSemanticAnnotation data; // list of query results (urls)

		private QueryResultSemanticAnnotation() {
			success = true;
			errorMsg = "";
			data = new ResultSemanticAnnotation(null, 0, 0, 0, null, null);
		}
	}

	@Root(name = "response")
	private static class QueryResultSelfExplanation {

		@Element
		private boolean success;

		@Element(name = "errormsg")
		private String errorMsg; // custom error message (optional)

		@Path("data")
		@ElementList(inline = true, entry = "result")
		private ResultSelfExplanation data; // list of query results (urls)

		private QueryResultSelfExplanation() {
			success = true;
			errorMsg = "";
			data = new ResultSelfExplanation(null, null);
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

	@Root(name = "response")
	private static class QueryResultCscl {

		@Element
		private boolean success;

		@Element(name = "errormsg")
		private String errorMsg; // custom error message (optional)

		@Path("data")
		@ElementList(inline = true, entry = "result")
		private ResultCscl data;

		private QueryResultCscl() {
			success = true;
			errorMsg = "";
			data = new ResultCscl("");
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

			String q = request.queryParams("q");
			String pathToLSA = request.queryParams("lsa");
			String pathToLDA = request.queryParams("lda");
			String lang = request.queryParams("lang");
			boolean usePOSTagging = Boolean.parseBoolean(request.queryParams("postagging"));
			double threshold = Double.parseDouble(request.queryParams("threshold"));

			QueryResultTopic queryResult = new QueryResultTopic();
			queryResult.data = ConceptMap.getTopics(processQuery(q, pathToLSA, pathToLDA, lang, usePOSTagging),
					threshold);
			String result = convertToJson(queryResult);
			// return Charset.forName("UTF-8").encode(result);
			return result;
		});
		Spark.get("/getSentiment", (request, response) -> {
			response.type("application/json");

			String q = request.queryParams("q");
			String pathToLSA = request.queryParams("lsa");
			String pathToLDA = request.queryParams("lda");
			String lang = request.queryParams("lang");
			boolean usePOSTagging = Boolean.parseBoolean(request.queryParams("postagging"));

			// System.out.println("Am primit: " + q);
			QueryResultSentiment queryResult = new QueryResultSentiment();
			queryResult.data = webService.services.SentimentAnalysis
					.getSentiment(processQuery(q, pathToLSA, pathToLDA, lang, usePOSTagging));
			String result = convertToJson(queryResult);
			return result;
		});
		Spark.get("/getComplexity", (request, response) -> {
			response.type("application/json");

			String q = request.queryParams("q");
			logger.info("Text primit");
			logger.info(q);
			String pathToLSA = request.queryParams("lsa");
			String pathToLDA = request.queryParams("lda");
			String lang = request.queryParams("lang");
			boolean usePOSTagging = Boolean.parseBoolean(request.queryParams("postagging"));

			QueryResultSentiment queryResult = new QueryResultSentiment();
			queryResult.data = TextualComplexity
					.getComplexityIndices(processQuery(q, pathToLSA, pathToLDA, lang, usePOSTagging));
			String result = convertToJson(queryResult);
			return result;
		});
		Spark.get("/search", (request, response) -> {

			response.type("application/json");

			String q = request.queryParams("q");
			String path = request.queryParams("path");
			/*
			 * String pathToLSA = request.queryParams("lsa"); String pathToLDA =
			 * request.queryParams("lda"); String lang =
			 * request.queryParams("lang"); boolean usePOSTagging =
			 * Boolean.parseBoolean(request.queryParams("postagging"));
			 */

			int maxContentSize = Integer.MAX_VALUE;
			String maxContentSizeStr = request.queryParams("mcs");
			if (maxContentSizeStr != null) {
				maxContentSize = Integer.parseInt(maxContentSizeStr);
			}

			QueryResultSearch queryResult = new QueryResultSearch();
			// queryResult.data = getComplexityIndices(q, pathToLSA, pathToLDA,
			// lang, usePOSTagging);
			queryResult.success = true;
			queryResult.errorMsg = "";
			queryResult.data = SearchClient.search(q, setDocuments(path), maxContentSize);
			String result = convertToJson(queryResult);
			return result;
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
			String lang = request.queryParams("lang");
			boolean usePOSTagging = Boolean.parseBoolean(request.queryParams("postagging"));
			double threshold = Double.parseDouble(request.queryParams("threshold"));

			QueryResultTopic queryResult = new QueryResultTopic();
			queryResult.data = ConceptMap.getTopics(processQuery(q, pathToLSA, pathToLDA, lang, usePOSTagging),
					threshold);
			String result = convertToJson(queryResult);
			// return Charset.forName("UTF-8").encode(result);
			return result;

		});
		Spark.post("/semanticProcess", (request, response) -> {
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

			String documentAbstract = (String) json.get("abstract");
			String documentKeywords = (String) json.get("keywords");
			String lang = (String) json.get("lang");
			String pathToLSA = (String) json.get("lsa");
			String pathToLDA = (String) json.get("lda");
			boolean usePOSTagging = (boolean) json.get("postagging");
			double threshold = (double) json.get("threshold");

			QueryResultSemanticAnnotation queryResult = new QueryResultSemanticAnnotation();
			queryResult.data = getSemanticAnnotation(documentAbstract, documentKeywords, documentContent, pathToLSA,
					pathToLDA, lang, usePOSTagging, threshold);
			String result = convertToJson(queryResult);
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

			QueryResultSelfExplanation queryResult = new QueryResultSelfExplanation();
			queryResult.data = getSelfExplanation(text, explanation, pathToLSA, pathToLDA, lang, usePOSTagging);
			String result = convertToJson(queryResult);
			// return Charset.forName("UTF-8").encode(result);
			return result;

		});
		Spark.post("/csclProcessing", (request, response) -> {
			JSONObject json = (JSONObject) new JSONParser().parse(request.body());

			response.type("application/json");

			String conversationText = (String) json.get("conversation");
			String language = (String) json.get("lang");
			String pathToLSA = (String) json.get("lsa");
			String pathToLDA = (String) json.get("lda");
			boolean usePOSTagging = (boolean) json.get("postagging");
			double threshold = (Double) json.get("threshold");
			
			AbstractDocumentTemplate contents = Cscl.getConversationText(conversationText);
			logger.info("Contents: blocks = " + contents.getBlocks().size());
			Lang lang = Lang.getLang(language);
			Conversation conversation = new Conversation(
					null,
					contents,
					LSA.loadLSA(pathToLSA, lang),
					LDA.loadLDA(pathToLDA, lang),
					lang,
					usePOSTagging,
					false);

			QueryResultTopic queryResult = new QueryResultTopic();
			queryResult.data = ParticipantInteraction.buildParticipantGraph(conversation);
			String result = convertToJson(queryResult);
			// return Charset.forName("UTF-8").encode(result);
			return result;

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
