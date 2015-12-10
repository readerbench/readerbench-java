package webService;

import java.awt.Color;
import java.io.File;
import java.io.FilenameFilter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.AbstractDocumentTemplate.BlockTemplate;
import data.dao.CategoryDAO;
import data.Block;
import data.Sentence;
import data.Word;
import data.discourse.SemanticCohesion;
import data.discourse.Topic;
import data.document.Document;
import data.pojo.Category;
import data.pojo.CategoryPhrase;
import data.sentiment.SentimentGrid;
import data.sentiment.SentimentValence;
import data.sentiment.SentimentWeights;
import edu.cmu.lti.jawjaw.pobj.Lang;
import services.commons.Formatting;
import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import services.converters.PdfToTextConverter;
import services.discourse.topicMining.TopicModeling;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticSearch.SemanticSearch;
import services.semanticSearch.SemanticSearchResult;
import spark.Spark;

class Result implements Comparable<Result> {

	private String content;
	private double score;

	public Result(String content, double score) {
		super();
		this.content = content;
		this.score = score;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public double getScore() {
		return score;
	}

	@Override
	public int compareTo(Result o) {
		return (int) Math.signum(o.getScore() - this.getScore());
	}
}

class ResultSentiment {

	private String content;
	private List<Result> valences;

	public ResultSentiment(String content, List<Result> valences) {
		super();
		this.content = content;
		this.valences = valences;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public List<Result> getValences() {
		return valences;
	}
}

class ResultSearch implements Comparable<ResultSearch> {

	private String url;
	private String content;
	private double relevance;

	public ResultSearch(String url, String content, double relevance) {
		this.url = url;
		this.content = content;
		this.relevance = relevance;
	}

	public String getUrl() {
		return url;
	}

	public String getContent() {
		return content;
	}

	public double getRelevance() {
		return relevance;
	}

	@Override
	public int compareTo(ResultSearch o) {
		return (int) Math.signum(o.getRelevance() - this.getRelevance());
	}
}

class ResultTopic {

	private List<ResultNode> nodes;
	private List<ResultEdge> links;

	public ResultTopic(List<ResultNode> nodes, List<ResultEdge> links) {
		this.nodes = nodes;
		this.links = links;
	}

}


class ResultKeyword implements Comparable<ResultKeyword> {
	
	private String name;
	private int noOccurences;
	private double relevance;
	
	public ResultKeyword(String name, int noOccurences, double relevance) {
		this.name = name;
		this.noOccurences = noOccurences;
		this.relevance = relevance;
	}
	
	public String getName() {
		return name;
	}
	
	private int getNoOccurences() {
		return noOccurences;
	}

	public double getRelevance() {
		return relevance;
	}

	@Override
	public int compareTo(ResultKeyword o) {
		return (int) Math.signum(o.getRelevance() - this.getRelevance());
	}
	
}

class ResultCategory implements Comparable<ResultCategory> {
	
	private String name;
	private double relevance;
	
	public ResultCategory(String name, double relevance) {
		this.name = name;
		this.relevance = relevance;
	}
	
	public String getName() {
		return name;
	}

	public double getRelevance() {
		return relevance;
	}

	@Override
	public int compareTo(ResultCategory o) {
		return (int) Math.signum(o.getRelevance() - this.getRelevance());
	}
	
}


class ResultSemanticAnnotation {
	
	private ResultTopic concepts;
	
	private double abstractDocumentSimilarity;
	private double keywordsAbstractCoverage;
	private double keywordsDocumentCoverage;
	
	private List<ResultKeyword> keywords;
	private List<ResultCategory> categories;

	public ResultSemanticAnnotation(
			ResultTopic resultTopic,
			double abstractDocumentSimilarity,
			double keywordsAbstractCoverage,
			double keywordsDocumentCoverage,
			List<ResultKeyword> resultKeywords,
			List<ResultCategory> resultCategories) {
		this.concepts = resultTopic;
		this.abstractDocumentSimilarity = abstractDocumentSimilarity;
		this.keywordsAbstractCoverage = keywordsAbstractCoverage;
		this.keywordsDocumentCoverage = keywordsDocumentCoverage;
		this.keywords = resultKeywords;
		this.categories = resultCategories;
	}

}

class ResultNode implements Comparable<ResultNode> {

	private int id;
	private String name;
	private double value;
	private int group;

	public ResultNode(int id, String name, double value, int group) {
		super();
		this.id = id;
		this.name = name;
		this.value = value;
		this.group = group;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getValue() {
		return value;
	}

	public double getGroup() {
		return group;
	}

	@Override
	public int compareTo(ResultNode o) {
		return (int) Math.signum(o.getValue() - this.getValue());
	}
}

class ResultEdge implements Comparable<ResultEdge> {

	private int source;
	private int target;
	private double score;

	public ResultEdge(int source, int target, double score) {
		super();
		this.source = source;
		this.target = target;
		this.score = score;
	}

	public int getSource() {
		return source;
	}

	public int getTarget() {
		return target;
	}

	public double getScore() {
		return score;
	}

	@Override
	public int compareTo(ResultEdge o) {
		return (int) Math.signum(o.getScore() - this.getScore());
	}
}

class ResultPdfToText {

	private String content;

	public ResultPdfToText(String content) {
		super();
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}

public class ReaderBenchServer {

	private static Logger logger = Logger.getLogger(ReaderBenchServer.class);
	public static final int PORT = 8080;

	private static final double MIN_SIZE = 5;
	private static final double MAX_SIZE_TOPIC = 20;
	private static final double MAX_SIZE_INFERRED_CONCEPT = 20;

	public static final double MIN_THRESHOLD = 0.2d;
	public static final int NO_RESULTS = 20;
	public static final Color COLOR_TOPIC = new Color(204, 204, 204); // silver
	public static final Color COLOR_INFERRED_CONCEPT = new Color(102, 102, 255); // orchid

	private static List<AbstractDocument> loadedDocs;
	private static String loadedPath;

	public AbstractDocument processQuery(String query, String pathToLSA, String pathToLDA, String language,
			boolean posTagging) {
		logger.info("Processign query ...");
		AbstractDocumentTemplate contents = new AbstractDocumentTemplate();
		String[] blocks = query.split("\n");
		logger.info("[Processing] There should be " + blocks.length + " blocks in the document");
		for (int i = 0; i < blocks.length; i++) {
			BlockTemplate block = contents.new BlockTemplate();
			block.setId(i);
			block.setContent(blocks[i]);
			contents.getBlocks().add(block);
		}

		// Lang lang = Lang.eng;
		Lang lang = Lang.getLang(language);
		AbstractDocument queryDoc = new Document(null, contents, LSA.loadLSA(pathToLSA, lang),
				LDA.loadLDA(pathToLDA, lang), lang, posTagging, false);
		logger.info("Built document has " + queryDoc.getBlocks().size() + " blocks.");
		queryDoc.computeAll(null, null);
		ComplexityIndices.computeComplexityFactors(queryDoc);

		return queryDoc;
	}

	/**
	 * Get document topics
	 *
	 * @param query
	 * @return List of keywords and corresponding relevance scores for results
	 */
	private ResultTopic getTopics(String query, String pathToLSA, String pathToLDA, String lang, boolean posTagging,
			double threshold) {

		List<ResultNode> nodes = new ArrayList<ResultNode>();
		List<ResultEdge> links = new ArrayList<ResultEdge>();
		AbstractDocument queryDoc = processQuery(query, pathToLSA, pathToLDA, lang, posTagging);

		//List<Topic> topics = queryDoc.getTopics();
		List<Topic> topics = TopicModeling.getSublist(queryDoc.getTopics(), 50,
				false, false);

		// build connected graph
		Map<Word, Boolean> visibleConcepts = new TreeMap<Word, Boolean>();
		// build nodes
		Map<Word, Double> nodeSizes = new TreeMap<Word, Double>();
		Map<Word, Integer> nodeGroups = new TreeMap<Word, Integer>();
		SentimentGrid<Double> edges = new SentimentGrid<>(topics.size(), topics.size());
		Map<Word, Integer> nodeIndexes = new TreeMap<Word, Integer>();

		for (Topic t : topics) {
			visibleConcepts.put(t.getWord(), false);
		}

		// determine similarities
		for (Word w1 : visibleConcepts.keySet()) {
			for (Word w2 : visibleConcepts.keySet()) {
				double lsaSim = 0;
				double ldaSim = 0;
				if (queryDoc.getLSA() != null)
					lsaSim = queryDoc.getLSA().getSimilarity(w1, w2);
				if (queryDoc.getLDA() != null)
					ldaSim = queryDoc.getLDA().getSimilarity(w1, w2);
				double sim = SemanticCohesion.getAggregatedSemanticMeasure(lsaSim, ldaSim);
				if (!w1.equals(w2) && sim >= threshold) {
					visibleConcepts.put(w1, true);
					visibleConcepts.put(w2, true);
				}
			}
		}

		// determine optimal sizes
		double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
		for (Topic t : topics) {
			if (visibleConcepts.get(t.getWord()) && t.getRelevance() >= 0) {
				min = Math.min(min, Math.log(1 + t.getRelevance()));
				max = Math.max(max, Math.log(1 + t.getRelevance()));
			}
		}

		int i = 0, j;
		for (Topic t : topics) {
			if (visibleConcepts.get(t.getWord())) {
				double nodeSize = 0;
				if (max != min && t.getRelevance() >= 0) {
					nodeSize = (MIN_SIZE
							+ (Math.log(1 + t.getRelevance()) - min) / (max - min) * (MAX_SIZE_TOPIC - MIN_SIZE));
				} else {
					nodeSize = MIN_SIZE;
				}
				nodeIndexes.put(t.getWord(), i);
				nodes.add(new ResultNode(i++, t.getWord().getLemma(), Formatting.formatNumber(t.getRelevance()), 1));
			}
		}

		// determine similarities
		i = 0;
		j = 0;
		for (Word w1 : visibleConcepts.keySet()) {
			edges.setIndex(w1.toString(), i++);
			for (Word w2 : visibleConcepts.keySet()) {
				edges.setIndex(w2.toString(), j++);
				if (!w1.equals(w2) && visibleConcepts.get(w1) && visibleConcepts.get(w2)) {
					double lsaSim = 0;
					double ldaSim = 0;
					if (queryDoc.getLSA() != null) {
						lsaSim = queryDoc.getLSA().getSimilarity(w1, w2);
					}
					if (queryDoc.getLDA() != null) {
						ldaSim = queryDoc.getLDA().getSimilarity(w1, w2);
					}
					double sim = SemanticCohesion.getAggregatedSemanticMeasure(lsaSim, ldaSim);
					if (sim >= threshold) {
						double distance = Double.MAX_VALUE;
						if (sim > .9)
							distance = 1;
						else
							distance = (1f - sim) * 10;
						links.add(new ResultEdge(nodeIndexes.get(w1), nodeIndexes.get(w2), distance));
					}
				}
			}
		}

		return new ResultTopic(nodes, links);
	}

	/**
	 * Get sentiment values for the entire document and for each paragraph
	 *
	 * @param query
	 * @return List of sentiment values per entity
	 */
	private List<ResultSentiment> getSentiment(String query, String pathToLSA, String pathToLDA, String lang,
			boolean posTagging) {

		logger.info("Valence map has " + data.sentiment.SentimentValence.getValenceMap().size()
				+ " sentiments after initialization.");
		SentimentWeights sw = new SentimentWeights();

		List<Result> results = new ArrayList<Result>();
		List<ResultSentiment> resultsSentiments = new ArrayList<ResultSentiment>();
		AbstractDocument queryDoc = processQuery(query, pathToLSA, pathToLDA, lang, posTagging);

		logger.info("Starting building sentiments...");
		// results.add(new Result("Document",
		// Formatting.formatNumber(queryDoc.getSentimentEntity().getAggregatedValue())));
		Map<SentimentValence, Double> rageSentimentsValues = queryDoc.getSentimentEntity().getAggregatedValue();
		//logger.info("There are " + rageSentimentsValues.size() + " rage setiments.");
		Iterator<Map.Entry<SentimentValence, Double>> it = rageSentimentsValues.entrySet().iterator();
		List<Result> localResults = new ArrayList<Result>();
		while (it.hasNext()) {
			Map.Entry<SentimentValence, Double> pair = (Map.Entry<SentimentValence, Double>) it.next();
			SentimentValence sentimentValence = (SentimentValence) pair.getKey();
			Double sentimentValue = (Double) pair.getValue();
			localResults.add(new Result(sentimentValence.getIndexLabel().replace("_RAGE", ""),
					Formatting.formatNumber(sentimentValue)));
		}
		resultsSentiments.add(new ResultSentiment("Document", localResults));

		for (Block b : queryDoc.getBlocks()) {
			/*
			 * results.add(new Result("Paragraph " + b.getIndex(),
			 * Formatting.formatNumber(b.getSentimentEntity().getAggregatedValue
			 * ())));
			 */

			rageSentimentsValues = b.getSentimentEntity().getAggregatedValue();
			it = rageSentimentsValues.entrySet().iterator();
			localResults = new ArrayList<Result>();
			while (it.hasNext()) {
				Map.Entry<SentimentValence, Double> pair = (Map.Entry<SentimentValence, Double>) it.next();
				SentimentValence sentimentValence = (SentimentValence) pair.getKey();
				Double sentimentValue = (Double) pair.getValue();
				localResults.add(new Result(sentimentValence.getIndexLabel().replace("_RAGE", ""),
						Formatting.formatNumber(sentimentValue)));
			}
			resultsSentiments.add(new ResultSentiment("\tParagraph " + b.getIndex(), localResults));

			for (Sentence s : b.getSentences()) {
				/*
				 * results.add(new Result("Paragraph " + b.getIndex() +
				 * " / Sentence " + s.getIndex(),
				 * Formatting.formatNumber(s.getSentimentEntity().
				 * getAggregatedValue())));
				 */

				rageSentimentsValues = s.getSentimentEntity().getAggregatedValue();
				it = rageSentimentsValues.entrySet().iterator();
				localResults = new ArrayList<Result>();
				while (it.hasNext()) {
					Map.Entry<SentimentValence, Double> pair = (Map.Entry<SentimentValence, Double>) it.next();
					SentimentValence sentimentValence = (SentimentValence) pair.getKey();
					Double sentimentValue = (Double) pair.getValue();
					localResults.add(new Result(sentimentValence.getIndexLabel().replace("_RAGE", ""),
							Formatting.formatNumber(sentimentValue)));
				}
				resultsSentiments.add(new ResultSentiment("\t\tSentence " + s.getIndex(), localResults));

			}
		}

		return resultsSentiments;
	}

	/**
	 * Get values for all textual complexity indices applied on the entire
	 * document
	 *
	 * @param query
	 * @return List of sentiment values per entity
	 */
	private List<ResultSentiment> getComplexityIndices(String query, String pathToLSA, String pathToLDA, String lang,
			boolean posTagging) {
		List<ResultSentiment> resultsComplexity = new ArrayList<ResultSentiment>();
		AbstractDocument queryDoc = processQuery(query, pathToLSA, pathToLDA, lang, posTagging);

		List<Result> localResults;
		for (IComplexityFactors complexityClass : ComplexityIndices.TEXTUAL_COMPLEXITY_FACTORS) {
			localResults = new ArrayList<Result>();
			for (int id : complexityClass.getIDs()) {
				localResults.add(new Result(ComplexityIndices.TEXTUAL_COMPLEXITY_INDEX_ACRONYMS[id],
						Formatting.formatNumber(queryDoc.getComplexityIndices()[id])));
			}
			resultsComplexity.add(new ResultSentiment(complexityClass.getClassName(), localResults));
		}

		return resultsComplexity;
	}

	/**
	 * Search for query in documents
	 *
	 * @param documents
	 * @param query
	 * @return List of urls for results
	 */
	private List<ResultSearch> search(String query, List<AbstractDocument> documents, int maxContentSize) {
		logger.info("Processign query:" + query);
		AbstractDocumentTemplate contents = new AbstractDocumentTemplate();
		BlockTemplate block = contents.new BlockTemplate();
		block.setId(0);
		block.setContent(query);
		contents.getBlocks().add(block);

		AbstractDocument queryDoc = new Document(null, contents, documents.get(0).getLSA(), documents.get(0).getLDA(),
				documents.get(0).getLanguage(), false, false);
		queryDoc.computeAll(null, null);
		queryDoc.setTitleText(query);

		List<SemanticSearchResult> results = SemanticSearch.search(queryDoc, documents, MIN_THRESHOLD, NO_RESULTS);
		List<ResultSearch> searchResults = new ArrayList<ResultSearch>();
		for (SemanticSearchResult r : results) {
			String content = r.getDoc().getText();
			if (content.length() > maxContentSize) {
				content = content.substring(0, maxContentSize);
				content = content.substring(0, content.lastIndexOf(" ")) + "...";
			}
			searchResults
					.add(new ResultSearch(r.getDoc().getPath(), content, Formatting.formatNumber(r.getRelevance())));
		}

		return searchResults;
	}
	
	public List<ResultKeyword> getKeywords(
			String documentKeywords,
			String documentContent,
			String pathToLSA,
			String pathToLDA,
			String lang,
			boolean usePOSTagging,
			double threshold) {
		
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
			resultKeywords.add(new ResultKeyword(keyword.getLemma(), occ, sc.getCohesion()));
		}
		
		Collections.sort(resultKeywords, Collections.reverseOrder());
		return resultKeywords;
		
	}
	
	public List<ResultCategory> getCategories(
			String documentContent,
			String pathToLSA,
			String pathToLDA,
			String lang,
			boolean usePOSTagging,
			double threshold) {
		
		List<ResultCategory> resultCategories = new ArrayList<ResultCategory>();
		
		AbstractDocument queryDoc = processQuery(documentContent, pathToLSA, pathToLDA, lang, usePOSTagging);
		List<Category> dbCategories = CategoryDAO.getInstance().findAll();
		
		
		for (Category cat : dbCategories) {
			//System.out.println("bau");
			//System.out.print(cat.getLabel()+"\t");
			List<CategoryPhrase> categoryPhrases = cat.getCategoryPhraseList();
			StringBuilder sb = new StringBuilder();
			for (CategoryPhrase categoryPhrase : categoryPhrases) {
				sb.append(categoryPhrase.getLabel());
				sb.append(", ");
			}
			//System.out.println(sb);
			
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
			resultCategories.add(new ResultCategory(cat.getLabel(), sc.getCohesion()));
		}
		
		Collections.sort(resultCategories, Collections.reverseOrder());
		return resultCategories;
	}
	
	private ResultSemanticAnnotation getSemanticAnnotation(
			String documentAbstract,
			String documentKeywords,
			String documentContent,
			String pathToLSA,
			String pathToLDA,
			String lang,
			boolean usePOSTagging,
			double threshold) {
		
		// concepts
		ResultTopic resultTopic = getTopics(documentContent, pathToLSA, pathToLDA, lang, usePOSTagging, threshold);
		List<ResultKeyword> resultKeywords = getKeywords(documentKeywords, documentContent, pathToLSA, pathToLDA, lang, usePOSTagging, threshold);
		List<ResultCategory> resultCategories = getCategories(documentContent, pathToLSA, pathToLDA, lang, usePOSTagging, threshold);

		AbstractDocument queryDoc = processQuery(documentContent, pathToLSA, pathToLDA, lang, usePOSTagging);
		AbstractDocument queryAbs = processQuery(documentAbstract, pathToLSA, pathToLDA, lang, usePOSTagging);
		AbstractDocument queryKey = processQuery(documentKeywords, pathToLSA, pathToLDA, lang, usePOSTagging);
		
		// (abstract, document) relevance
		SemanticCohesion scAbstractDocument = new SemanticCohesion(queryAbs, queryDoc);
		
		// (abstract, keywords) relevance
		SemanticCohesion scKeywordsAbstract = new SemanticCohesion(queryKey, queryAbs);
		
		// (keywords, document) relevance
		SemanticCohesion scKeywordsDocument = new SemanticCohesion(queryKey, queryDoc);
		
		ResultSemanticAnnotation rsa = new ResultSemanticAnnotation(
				resultTopic,
				scAbstractDocument.getCohesion(),
				scKeywordsAbstract.getCohesion(),
				scKeywordsDocument.getCohesion(),
				resultKeywords,
				resultCategories);
		
		return rsa;
	}
	
	private ResultPdfToText getTextFromPdf(String uri, boolean localFile) {
		// MS_training_SE_1999
		if (localFile) {
			return new ResultPdfToText(PdfToTextConverter.pdftoText("resources/papers/" + uri + ".pdf", true));
		}
		else {
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
	
	private String convertToJson(QueryResultPdfToText queryResult) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
		String json = gson.toJson(queryResult);
		return json;
	}
	
	private String convertToJson(QueryResultSemanticAnnotation queryResult) {
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
		private List<Result> data; // list of query results (urls)

		private QueryResult() {
			success = true;
			errorMsg = "";
			data = new ArrayList<Result>();
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

			String q = request.queryParams("q");
			String pathToLSA = request.queryParams("lsa");
			String pathToLDA = request.queryParams("lda");
			String lang = request.queryParams("lang");
			boolean usePOSTagging = Boolean.parseBoolean(request.queryParams("postagging"));
			double threshold = Double.parseDouble(request.queryParams("threshold"));

			QueryResultTopic queryResult = new QueryResultTopic();
			queryResult.data = getTopics(q, pathToLSA, pathToLDA, lang, usePOSTagging, threshold);
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
			queryResult.data = getSentiment(q, pathToLSA, pathToLDA, lang, usePOSTagging);
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
			queryResult.data = getComplexityIndices(q, pathToLSA, pathToLDA, lang, usePOSTagging);
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
			queryResult.data = search(q, setDocuments(path), maxContentSize);
			String result = convertToJson(queryResult);
			return result;
		});
		Spark.get("/getTopicsFromPdf", (request, response) -> {
			response.type("application/json");

			String uri = request.queryParams("uri");
			logger.info("URI primit");
			logger.info(uri);
			
			/*QueryResultPdfToText queryResult = new QueryResultPdfToText();
			queryResult.data = getTextFromPdf(uri);
			String result = convertToJson(queryResult);*/
			
			String q = getTextFromPdf(uri, true).getContent();
			String pathToLSA = request.queryParams("lsa");
			String pathToLDA = request.queryParams("lda");
			String lang = request.queryParams("lang");
			boolean usePOSTagging = Boolean.parseBoolean(request.queryParams("postagging"));
			double threshold = Double.parseDouble(request.queryParams("threshold"));

			QueryResultTopic queryResult = new QueryResultTopic();
			queryResult.data = getTopics(q, pathToLSA, pathToLDA, lang, usePOSTagging, threshold);
			String result = convertToJson(queryResult);
			// return Charset.forName("UTF-8").encode(result);
			return result;
			
		});
		Spark.post("/semanticProcess", (request, response) -> {
			JSONObject json = (JSONObject)new JSONParser().parse(request.body());
			
			response.type("application/json");

			String uri = (String) json.get("uri");
			//String url = (String) json.get("url");
			
			/*QueryResultPdfToText queryResult = new QueryResultPdfToText();
			queryResult.data = getTextFromPdf(uri);
			String result = convertToJson(queryResult);*/
			
			String documentContent = null;
			if (uri == null || uri.isEmpty()) {
				logger.error("URI an URL are empty. Aborting...");
				System.exit(-1);
			}
			if (uri.contains("http") || uri.contains("https") || uri.contains("ftp")) {
				documentContent = getTextFromPdf(uri, false).getContent();
			}
			else {
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
			queryResult.data = getSemanticAnnotation(
					documentAbstract,
					documentKeywords,
					documentContent,
					pathToLSA,
					pathToLDA,
					lang,
					usePOSTagging,
					threshold
			);
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
			File dir = new File(URLDecoder.decode("resources/in/" + path));
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

	public static void main(String[] args) {
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.INFO); // changing log level

		ReaderBenchServer server = new ReaderBenchServer();
		server.start();
	}
}
