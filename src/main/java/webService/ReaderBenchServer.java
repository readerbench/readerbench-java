package webService;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import DAO.AbstractDocument;
import DAO.AbstractDocumentTemplate;
import DAO.AbstractDocumentTemplate.BlockTemplate;
import DAO.Block;
import DAO.Sentence;
import DAO.discourse.Topic;
import DAO.document.Document;
import DAO.sentiment.SentimentValence;
import edu.cmu.lti.jawjaw.pobj.Lang;
import services.commons.Formatting;
import services.complexity.ComplexityIndices;
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

public class ReaderBenchServer {

    private static Logger logger = Logger.getLogger(ReaderBenchServer.class);
    public static final int PORT = 5656;

    public AbstractDocument processQuery(String query) {
        logger.info("Processign query ...");
        AbstractDocumentTemplate contents = new AbstractDocumentTemplate();
        String[] blocks = query.split("\n");
        for (int i = 0; i < blocks.length; i++) {
            BlockTemplate block = contents.new BlockTemplate();
            block.setId(i);
            block.setContent(blocks[i]);
            contents.getBlocks().add(block);
        }

        Lang lang = Lang.eng;
        AbstractDocument queryDoc = new Document(null, contents, LSA.loadLSA("resources/config/LSA/tasa_en", lang),
                LDA.loadLDA("resources/config/LDA/tasa_en", lang), lang, true, false);
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
    private List<Result> getTopics(String query) {
        List<Result> results = new ArrayList<Result>();
        AbstractDocument queryDoc = processQuery(query);
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
    private List<Result> getSentiment(String query) {
        List<Result> results = new ArrayList<Result>();
        AbstractDocument queryDoc = processQuery(query);

        //results.add(new Result("Document", Formatting.formatNumber(queryDoc.getSentimentEntity().getAggregatedValue())));
        Map<SentimentValence, Double> sentimentAggregatedValues = queryDoc.getSentimentEntity().getAggregatedValue();
        Iterator<Map.Entry<SentimentValence, Double>> it = sentimentAggregatedValues.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<SentimentValence, Double> pair = (Map.Entry<SentimentValence, Double>)it.next();
			SentimentValence sentimentValence = (SentimentValence)pair.getKey();
			Double sentimentValue = (Double)pair.getValue();
			results.add(new Result("Document (valence " + sentimentValence.getName() + ")", Formatting.formatNumber(sentimentValue)));
		}

        for (Block b : queryDoc.getBlocks()) {
            /*results.add(new Result("Paragraph " + b.getIndex(),
                    Formatting.formatNumber(b.getSentimentEntity().getAggregatedValue())));*/
        	
        	sentimentAggregatedValues = queryDoc.getSentimentEntity().getAggregatedValue();
            it = sentimentAggregatedValues.entrySet().iterator();
    		while (it.hasNext()) {
    			Map.Entry<SentimentValence, Double> pair = (Map.Entry<SentimentValence, Double>)it.next();
    			SentimentValence sentimentValence = (SentimentValence)pair.getKey();
    			Double sentimentValue = (Double)pair.getValue();
    			results.add(new Result("Paragraph " + b.getIndex() + " (valence " + sentimentValence.getName() + ")", Formatting.formatNumber(sentimentValue)));
    		}
        	
            for (Sentence s : b.getSentences()) {
                /*results.add(new Result("Paragraph " + b.getIndex() + " / Sentence " + s.getIndex(),
                        Formatting.formatNumber(s.getSentimentEntity().getAggregatedValue())));*/
            	
            	sentimentAggregatedValues = queryDoc.getSentimentEntity().getAggregatedValue();
                it = sentimentAggregatedValues.entrySet().iterator();
        		while (it.hasNext()) {
        			Map.Entry<SentimentValence, Double> pair = (Map.Entry<SentimentValence, Double>)it.next();
        			SentimentValence sentimentValence = (SentimentValence)pair.getKey();
        			Double sentimentValue = (Double)pair.getValue();
        			results.add(new Result("Paragraph " + b.getIndex() + " / Sentence " + s.getIndex() + " (valence " + sentimentValence.getName() + ")", Formatting.formatNumber(sentimentValue)));
        		}
            	
            }
        }

        return results;
    }

    /**
     * Get values for all textual complexity indices applied on the entire
     * document
     *
     * @param query
     * @return List of sentiment values per entity
     */
    private List<Result> getComplexityIndices(String query) {
        List<Result> results = new ArrayList<Result>();
        AbstractDocument queryDoc = processQuery(query);

        for (int i = 0; i < ComplexityIndices.NO_COMPLEXITY_INDICES; i++) {
            results.add(new Result(ComplexityIndices.TEXTUAL_COMPLEXITY_INDEX_NAMES[i],
                    Formatting.formatNumber(queryDoc.getComplexityIndices()[i])));
        }
        return results;
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
    	return null;
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

    public void start() {
        Spark.port(PORT);
        Spark.get("/", (request, response) -> {
            return "OK";
        });
        Spark.get("/getTopics", (request, response) -> {
            response.type("text/xml");
            
            String q = request.queryParams("q");
            /*String pathToLSA = request.queryParams("lsa");
            String pathToLDA = request.queryParams("lda");
            String lang = request.queryParams("lang");
            String usePOSTagging = request.queryParams("postagging");*/
            
            QueryResult queryResult = new QueryResult();
            queryResult.data = getTopics(q);
            String result = convertToXml(queryResult);
            return result;
        });
        Spark.get("/getSentiment", (request, response) -> {
            response.type("text/xml");
            
            String q = request.queryParams("q");
            /*String pathToLSA = request.queryParams("lsa");
            String pathToLDA = request.queryParams("lda");
            String lang = request.queryParams("lang");
            String usePOSTagging = request.queryParams("postagging");*/
            
            System.out.println("Am primit: " + q);
            QueryResult queryResult = new QueryResult();
            queryResult.data = getSentiment(q);
            String result = convertToXml(queryResult);
            return result;
        });
        Spark.get("/getComplexity", (request, response) -> {
            response.type("text/xml");
            
            String q = request.queryParams("q");
            String pathToLSA = request.queryParams("lsa");
            String pathToLDA = request.queryParams("lda");
            String lang = request.queryParams("lang");
            String usePOSTagging = request.queryParams("postagging");
            
            QueryResult queryResult = new QueryResult();
            queryResult.data = getComplexityIndices(q);
            String result = convertToXml(queryResult);
            return result;
        });

    }

    public static void main(String[] args) {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO); // changing log level

        ReaderBenchServer server = new ReaderBenchServer();
        server.start();
    }
}
