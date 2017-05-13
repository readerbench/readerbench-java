package licenta;

import com.sun.jersey.api.client.ClientResponse;
import dao.CategoryDAO;
import dao.WordDAO;
import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.Block;
import data.Lang;
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
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import services.commons.Formatting;
import services.converters.PdfToTextConverter;

import services.semanticModels.ISemanticModel;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import services.semanticModels.SimilarityType;
import services.semanticModels.word2vec.Word2VecModel;
import services.similarity.TextSimilarity;
import spark.Request;
import spark.Response;
import spark.Spark;
import view.widgets.article.utils.GraphMeasure;
import webService.ReaderBenchServer;
import webService.cv.CVHelper;
import webService.keywords.KeywordsHelper;
import webService.query.QueryHelper;
import webService.queryResult.*;
import webService.result.ResultCategory;
import webService.result.ResultCv;
import webService.result.ResultCvCover;
import webService.result.ResultCvOrCover;
import webService.result.ResultFile;
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
import webService.services.cscl.CSCL;
import webService.services.lak.TwoModeGraphBuilder;
import webService.services.lak.TwoModeGraphFilter;
import webService.services.lak.result.QueryResultDocumentYears;
import webService.services.lak.result.QueryResultGraphMeasures;
import webService.services.lak.result.QueryResultTwoModeGraph;
import webService.services.lak.result.QueryResultTwoModeGraphNodes;
import webService.services.lak.result.TwoModeGraph;
import webService.services.lak.result.TwoModeGraphNode;
import webService.services.utils.FileProcessor;
import webService.services.utils.ParamsValidator;
import webService.services.vCoP.CommunityInteraction;
public class TestareSentimentAnalysis {

    private static Map<String, String> hmParams(JSONObject json) {
        Map<String, String> hm = new HashMap<>();
        for (String paramKey : (Set<String>) json.keySet()) {
            hm.put(paramKey, json.get(paramKey).toString());
        }
        return hm;
    }
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ReaderBenchServer.initializeDB();
		JSONObject request = new JSONObject();
		String text = "RAGE aims to develop, transform and enrich advanced technologies from the leisure games industry into self-contained gaming assets (i.e. solutions showing economic value potential) that support game studios at developing applied games easier, faster and more cost-effectively. These assets will be available along with a large volume of high-quality knowledge resources through a self-sustainable Ecosystem, which is a social space that connects research, gaming industries, intermediaries, education providers, policy makers and end-users. RAGE – Realising an Applied Gaming Eco-system,  is a 48-months Technology and Know-How driven Research and Innovation project co-funded by EU Framework Programme for Research and Innovation, Horizon 2020.The EU based industry for non-leisure games – Applied Games – is an emerging business with multiple uses in industry, education, health and the public administration sectors. As such, it is still fragmented and needs critical mass to compete globally. Nevertheless its growth potential is widely recognised and even suggested to exceed the growth potential of the leisure games market.The gaming technology assets gathered along the project lifecycle will be tested and evaluated by gaming companies integrated in the RAGE consortium. These companies will be creating games that will be empirically validated in real world pilots in different application scenarios representing different markets and target groups for the Applied Games industry.";
		request.put("text", text);
		request.put("language","en");
		request.put("lsa", "TASA");
		request.put("lda", "TASA");
		Map<String, String> hm = hmParams(request);
		System.out.println(hm);
		 QueryResultSentiment queryResult = new QueryResultSentiment();
         try {
             queryResult.setData(SentimentAnalysis.computeSentiments(QueryHelper.processQuery(hm)));
         } catch (Exception e) {
           e.printStackTrace();
         }
        // return queryResult.convertToJson();
         System.out.println(queryResult.convertToJson().toString());
		
	}

}
