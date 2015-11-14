package webService;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import DAO.AbstractDocument;
import DAO.AbstractDocumentTemplate;
import DAO.AbstractDocumentTemplate.BlockTemplate;
import DAO.Block;
import DAO.Sentence;
import DAO.discourse.Topic;
import DAO.document.Document;
import DAO.sentiment.SentimentValence;
import DAO.sentiment.SentimentWeights;
import edu.cmu.lti.jawjaw.pobj.Lang;
import services.commons.Formatting;
import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
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

public class ReaderBenchServer {

	private static Logger logger = Logger.getLogger(ReaderBenchServer.class);
	public static final int PORT = 5656;

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
	private List<Result> getTopics(String query, String pathToLSA, String pathToLDA, String lang, boolean posTagging) {
		List<Result> results = new ArrayList<Result>();
		AbstractDocument queryDoc = processQuery(query, pathToLSA, pathToLDA, lang, posTagging);
		for (Topic t : queryDoc.getTopics()) {
			results.add(new Result(t.getWord().getLemma(), Formatting.formatNumber(t.getRelevance())));
		}
		return results;
	}

	/**
	 * Get sentiment values for the entire document and for each paragraph
	 *
	 * @param query
	 * @return List of sentiment values per entity
	 */
	private List<ResultSentiment> getSentiment(String query, String pathToLSA, String pathToLDA, String lang,
			boolean posTagging) {

		logger.info("Valence map has " + DAO.sentiment.SentimentValence.getValenceMap().size()
				+ " sentiments after initialization.");
		SentimentWeights sw = new SentimentWeights();

		List<Result> results = new ArrayList<Result>();
		List<ResultSentiment> resultsSentiments = new ArrayList<ResultSentiment>();
		AbstractDocument queryDoc = processQuery(query, pathToLSA, pathToLDA, lang, posTagging);

		logger.info("Starting building sentiments...");
		// results.add(new Result("Document",
		// Formatting.formatNumber(queryDoc.getSentimentEntity().getAggregatedValue())));
		Map<SentimentValence, Double> rageSentimentsValues = queryDoc.getSentimentEntity().getAggregatedValue();
		logger.info("There are " + rageSentimentsValues.size() + " rage setiments.");
		Iterator<Map.Entry<SentimentValence, Double>> it = rageSentimentsValues.entrySet().iterator();
		List<Result> localResults = new ArrayList<Result>();
		while (it.hasNext()) {
			Map.Entry<SentimentValence, Double> pair = (Map.Entry<SentimentValence, Double>) it.next();
			SentimentValence sentimentValence = (SentimentValence) pair.getKey();
			Double sentimentValue = (Double) pair.getValue();
			localResults.add(new Result(sentimentValence.getIndexLabel(), Formatting.formatNumber(sentimentValue)));
		}
		resultsSentiments.add(new ResultSentiment("Document", localResults));

		for (Block b : queryDoc.getBlocks()) {
			/*
			 * results.add(new Result("Paragraph " + b.getIndex(),
			 * Formatting.formatNumber(b.getSentimentEntity().getAggregatedValue
			 * ())));
			 */

			rageSentimentsValues = queryDoc.getSentimentEntity().getAggregatedValue();
			it = rageSentimentsValues.entrySet().iterator();
			localResults = new ArrayList<Result>();
			while (it.hasNext()) {
				Map.Entry<SentimentValence, Double> pair = (Map.Entry<SentimentValence, Double>) it.next();
				SentimentValence sentimentValence = (SentimentValence) pair.getKey();
				Double sentimentValue = (Double) pair.getValue();
				localResults.add(new Result(sentimentValence.getIndexLabel(), Formatting.formatNumber(sentimentValue)));
			}
			resultsSentiments.add(new ResultSentiment("Paragraph " + b.getIndex(), localResults));

			for (Sentence s : b.getSentences()) {
				/*
				 * results.add(new Result("Paragraph " + b.getIndex() +
				 * " / Sentence " + s.getIndex(),
				 * Formatting.formatNumber(s.getSentimentEntity().
				 * getAggregatedValue())));
				 */

				rageSentimentsValues = queryDoc.getSentimentEntity().getAggregatedValue();
				it = rageSentimentsValues.entrySet().iterator();
				localResults = new ArrayList<Result>();
				while (it.hasNext()) {
					Map.Entry<SentimentValence, Double> pair = (Map.Entry<SentimentValence, Double>) it.next();
					SentimentValence sentimentValence = (SentimentValence) pair.getKey();
					Double sentimentValue = (Double) pair.getValue();
					localResults
							.add(new Result(sentimentValence.getIndexLabel(), Formatting.formatNumber(sentimentValue)));
				}
				resultsSentiments.add(
						new ResultSentiment("Paragraph " + b.getIndex() + " / Sentence " + s.getIndex(), localResults));

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
				localResults.add(new Result(ComplexityIndices.TEXTUAL_COMPLEXITY_INDEX_NAMES[id], Formatting.formatNumber(queryDoc.getComplexityIndices()[id])));
			}
			resultsComplexity.add(new ResultSentiment(complexityClass.getClassName(), localResults));
		}

		return resultsComplexity;
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
	
	/*before(new Filter("/") {
	    @Override
	    public void handle(Request request, Response response) {
	        try (InputStream stream = getClass().getResourceAsStream("/public/index.html")) {
	            halt(200, IOUtils.toString(stream));
	        } catch (IOException e) {
	            // if the resource doesn't exist we just carry on.
	        }
	    }
	});*/
	
	/*private static void enableCORS(final String origin, final String methods, final String headers) {
	    before(new Filter() {
	        @Override
	        public void handle(Request request, Response response) {
	            response.header("Access-Control-Allow-Origin", origin);
	            response.header("Access-Control-Request-Method", methods);
	            response.header("Access-Control-Allow-Headers", headers);
	        }
	    });
	}*/

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

			QueryResult queryResult = new QueryResult();
			queryResult.data = getTopics(q, pathToLSA, pathToLDA, lang, usePOSTagging);
			String result = convertToJson(queryResult);
			return result;
		});
		Spark.get("/getSentiment", (request, response) -> {
			response.type("application/json");

			String q = request.queryParams("q");
			String pathToLSA = request.queryParams("lsa");
			String pathToLDA = request.queryParams("lda");
			String lang = request.queryParams("lang");
			boolean usePOSTagging = Boolean.parseBoolean(request.queryParams("postagging"));

			System.out.println("Am primit: " + q);
			QueryResultSentiment queryResult = new QueryResultSentiment();
			queryResult.data = getSentiment(q, pathToLSA, pathToLDA, lang, usePOSTagging);
			String result = convertToJson(queryResult);
			return result;
		});
		Spark.get("/getComplexity", (request, response) -> {
			response.type("application/json");

			String q = request.queryParams("q");
			String pathToLSA = request.queryParams("lsa");
			String pathToLDA = request.queryParams("lda");
			String lang = request.queryParams("lang");
			boolean usePOSTagging = Boolean.parseBoolean(request.queryParams("postagging"));

			QueryResultSentiment queryResult = new QueryResultSentiment();
			queryResult.data = getComplexityIndices(q, pathToLSA, pathToLDA, lang, usePOSTagging);
			String result = convertToJson(queryResult);
			return result;
		});
		/*Spark.get("/search", (request, response) -> {

			// TODO: de modificat sa faca search
			response.type("application/json");

			String q = request.queryParams("q");
			String pathToLSA = request.queryParams("lsa");
			String pathToLDA = request.queryParams("lda");
			String lang = request.queryParams("lang");
			boolean usePOSTagging = Boolean.parseBoolean(request.queryParams("postagging"));

			QueryResult queryResult = new QueryResultS();
			//queryResult.data = getComplexityIndices(q, pathToLSA, pathToLDA, lang, usePOSTagging);
			String result = convertToJson(queryResult);
			return result;
		});*/

	}

	public static void main(String[] args) {
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.INFO); // changing log level

		ReaderBenchServer server = new ReaderBenchServer();
		server.start();
	}
}
