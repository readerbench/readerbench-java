/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.parallelprocessingservice;

import akka.pattern.Patterns;
import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.coreservices.data.cscl.Community;
import com.readerbench.coreservices.data.cscl.Conversation;
import com.readerbench.coreservices.data.AbstractDocumentTemplate;
import com.readerbench.coreservices.keywordmining.Keyword;
import com.readerbench.coreservices.keywordmining.KeywordModeling;
import com.readerbench.coreservices.semanticmodels.SemanticModel;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.parallelprocessingservice.actors.cscl.ConversationActorSystem;
import com.readerbench.datasourceprovider.elasticsearch.ElasticsearchService;
import com.readerbench.parallelprocessingservice.messages.ProcessDocumentsInitMessage;
import com.readerbench.processingservice.Annotators;
import com.readerbench.processingservice.cscl.CommunityProcessingPipeline;
import com.readerbench.processingservice.cscl.CommunityUtils;
import com.readerbench.processingservice.cscl.ConversationProcessingPipeline;
import com.readerbench.processingservice.exportdata.ExportCommunity;
import com.readerbench.processingservice.exportdata.ExportCommunityToES;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import org.json.*;
import org.json.simple.parser.JSONParser;
import org.joda.time.DateTime;
//import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.*;

/**
 *
 * @author Dragos
 */
public class ParallelConversationProcessingPipeline {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParallelConversationProcessingPipeline.class);

    private static final String INDIVIDUAL_STATS_FILENAME = "individualStats.csv";
    private static final String INITIATION_FILENAME = "initiation.csv";
    private static final String TEXTUAL_COMPLEXITY = "textualComplexity.csv";
    private static final String TIME_ANALYSIS = "timeAnalysis.csv";
    private static final String DISCUSSED_TOPICS = "discussedTopics.csv";
    private static final String INDIVIDUAL_THREAD_STATISTICS = "individualThreadStatistics.csv";
    private static final String PARTICIPANT_VIEW_D3_FILE = "particiantViewD3.json";
    private static final String PATH = "C:\\Users\\Dorinela\\Desktop\\catalin\\out";
    private static final String ELASTICSEARCH_INDEX_PARTICIPANTS = "community-participant";
    private static final String ELASTICSEARCH_INDEX_DIRECTED_GRAPH = "community-dr";
    private static final String ELASTICSEARCH_INDEX_EDGEBUNDLING = "community-eb";
    private static final String ELASTICSEARCH_TYPE_PARTICIPANTS = "statistics";
    private static final String ELASTICSEARCH_TYPE_DIRECTED_GRAPH = "directedGraph";
    private static final String ELASTICSEARCH_TYPE_EDGEBUNDLING = "edgeBundling";

    private static final String ELASTICSEARCH_INDEX_KEYWORDS = "community-k";
    private static final String ELASTICSEARCH_TYPE_KEYWORDS = "keywords";

    private String communityName;
    private Lang lang;
    private List<SemanticModel> models;
    private List<Annotators> annotators;
    private Date startDate;
    private Date endDate;
    private int monthIncrement;
    private int dayIncrement;
    private static ElasticsearchService elasticsearchService = new ElasticsearchService();

    public ParallelConversationProcessingPipeline(String communityName, Lang lang, List<SemanticModel> models, List<Annotators> annotators, int monthIncrement, int dayIncrement) {
        this.communityName = communityName;
        this.lang = lang;
        this.models = models;
        this.annotators = annotators;
        this.monthIncrement = monthIncrement;
        this.dayIncrement = dayIncrement;
    }

    private ArrayList<JSONObject> getThreads(String path) {
	File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
	ArrayList<JSONObject> jsonList = new ArrayList<JSONObject>();

	JSONParser parser = new JSONParser();

        for (int i = 0; i < listOfFiles.length; ++i) {
            if (listOfFiles[i].isFile()) {
		String fPath = path + "/" + listOfFiles[i].getName();
		try {
			JSONObject obj = new JSONObject(parser.parse(new FileReader(fPath)).toString());
               		jsonList.add(obj);
			//System.out.println(obj.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
            }
        }

	return jsonList;
    }
    
    public List<Conversation> extractConvTemplateFromEs(String path) {
        ConversationProcessingPipeline pipeline = new ConversationProcessingPipeline(lang, models, annotators);
        List<AbstractDocumentTemplate> templates = new ArrayList<>();

        //ArrayList<org.json.simple.JSONObject> discussionThreads = elasticsearchService.getDiscussionThreads(communityName, communityType);
	ArrayList<JSONObject> discussionThreads = getThreads(path);

        try {
            for (JSONObject threadSimple : discussionThreads) {
                JSONObject thread = new  JSONObject(threadSimple.toString());
                AbstractDocumentTemplate template = pipeline.extractConvTemplateFromEsJson(thread);
                if (template != null) {
                    templates.add(template);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        List<Conversation> listOfProcessedConversations = new ArrayList<>();
        for (AbstractDocumentTemplate template : templates) {
            Conversation conversation = pipeline.createConversationFromTemplate(template);
            pipeline.processConversation(conversation);

            listOfProcessedConversations.add(conversation);
        }

        return listOfProcessedConversations;   
    }

    public List<Conversation> loadXMLsFromDirectory(String directoryPath) {
        ConversationProcessingPipeline pipeline = new ConversationProcessingPipeline(lang, models, annotators);
        List<AbstractDocumentTemplate> templates = new ArrayList<>();

        File folder = new File(directoryPath);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; ++i) {
            if (listOfFiles[i].isFile()) {
                LOGGER.info("--------- Load xml file: " + listOfFiles[i].getName() + " ------------");
                AbstractDocumentTemplate template = pipeline.extractConvTemplateFromXML(directoryPath + "/" + listOfFiles[i].getName());
                if (template != null) {
                    templates.add(template);
                }
            }
        }

        /*ProcessDocumentsInitMessage initMsg = new ProcessDocumentsInitMessage(templates, lang, models, annotators);

        Future<Object> future = Patterns.ask(ConversationActorSystem.PROCESSING_MASTER, initMsg, ConversationActorSystem.TIMEOUT);

        List<Conversation> listOfProcessedConversations = null;
        try {
            Object result = Await.result(future, Duration.Inf());
            listOfProcessedConversations = (List<Conversation>) result;
        } catch (Exception e) {
            LOGGER.error("Error in processing the conversations in a parallel manner. Error message: " + e.getMessage());
        }

        ConversationActorSystem.ACTOR_SYSTEM.terminate();*/

        List<Conversation> listOfProcessedConversations = new ArrayList<>();
        for (AbstractDocumentTemplate template : templates) {
            Conversation conversation = pipeline.createConversationFromTemplate(template);
            pipeline.processConversation(conversation);

            listOfProcessedConversations.add(conversation);
        }

        return listOfProcessedConversations;
    }

    public void processCommunity(String communityName, String path) {
//        String eDate="2019.06.12";
//        try {
//            endDate = new SimpleDateFormat("yyyy.MM.dd").parse(eDate);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        String sDate="2019.04.09";
//        try {
//            startDate = new SimpleDateFormat("yyyy.MM.dd").parse(sDate);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        CommunityProcessingPipeline pipeline = new CommunityProcessingPipeline(lang, models, annotators);

        List<Conversation> conversations = extractConvTemplateFromEs(path);
	    //List<Conversation> conversations = loadXMLsFromDirectory("/home/fetoiucatalinemil/Licenta/RedditCrawling/xml_posts");

        Community community = pipeline.createCommunityFromConversations(communityName, conversations, models, startDate, endDate);
        pipeline.processCommunity(community);
	    pipeline.processTimeSeries(community, monthIncrement, dayIncrement);

        CommunityUtils.hierarchicalClustering(community, PATH + "/clustered_results_" + communityName + "_week_" + 0 + ".csv");
        ExportCommunityToES ec = new ExportCommunityToES(community);

        List<Map<String, Object>> contributionsForTrend = ec.getContributionsForTrend();
        System.out.println("\n----------------- contributionsForTrend ------------------- ");
        System.out.println(contributionsForTrend);

        List<Map<String, Object>> globalTimelineEvolution = ec.getGlobalTimelineEvolution();
        System.out.println("\n----------------- globalTimelineEvolution ------------------- ");
        for (Map<String, Object> globalTimeline : globalTimelineEvolution) {
            System.out.println(globalTimeline);
        }

        Map<String, List<Integer>> keywordsSimilarity = ec.getKeywordsSimilarity(0.7, 20);
        System.out.println("\n----------------- keywordsSimilarity ------------------- ");
        System.out.println(keywordsSimilarity);

        List<Map<String, Object>> participantsStats = ec.writeIndividualStatsToElasticsearch(0);
        LOGGER.info("participantsStats: " + participantsStats);
        elasticsearchService.indexParticipantsStats(ELASTICSEARCH_INDEX_PARTICIPANTS, ELASTICSEARCH_TYPE_PARTICIPANTS, participantsStats);
        /**
         * index participant interaction results
         */
        LOGGER.info("Start generating participants directed graph representation.");
        org.json.simple.JSONObject participantInteraction = ec.generateParticipantViewD3(0);
        LOGGER.info("participantInteraction: " + participantInteraction);
        elasticsearchService.indexParticipantGraphRepresentation(ELASTICSEARCH_INDEX_DIRECTED_GRAPH, ELASTICSEARCH_TYPE_DIRECTED_GRAPH, participantInteraction);

        LOGGER.info("Start generating hierarchical edge bundling.");
        org.json.simple.JSONObject hierarchicalEdgeBundling = ec.generateHierarchicalEdgeBundling(0);
        LOGGER.info("hierarchicalEdgeBundling: " + hierarchicalEdgeBundling);
        elasticsearchService.indexParticipantGraphRepresentation(ELASTICSEARCH_INDEX_EDGEBUNDLING, ELASTICSEARCH_TYPE_EDGEBUNDLING, hierarchicalEdgeBundling);

        LOGGER.info("Start generating keywords for heapMap");
        org.json.simple.JSONObject keywords = ec.buildKeywordsForHeapMap(0, community);
        LOGGER.info("keywords: " + keywords);
        elasticsearchService.indexParticipantGraphRepresentation(ELASTICSEARCH_INDEX_KEYWORDS, ELASTICSEARCH_TYPE_KEYWORDS, keywords);

        LOGGER.info("\n----------- Subcommunities stats -------- \n");
        LOGGER.info("\n----------- The number of communities are: " + community.getTimeframeSubCommunities().size() + "\n");
        for (int i = 0; i < community.getTimeframeSubCommunities().size(); i++) {
            LOGGER.info("Start extracting statistics for community " + (i + 1));
            Community subCommunity = community.getTimeframeSubCommunities().get(i);
            CommunityUtils.hierarchicalClustering(subCommunity, PATH + "/" + communityName + "_clustered_results_week_" + (i + 1) + ".csv");
            ec = new ExportCommunityToES(subCommunity);

            List<Map<String, Object>> participantsStatsSubCommunity = ec.writeIndividualStatsToElasticsearch(i + 1);
            LOGGER.info("participantsStatsSubCommunity: " + participantsStatsSubCommunity);
            elasticsearchService.indexParticipantsStats(ELASTICSEARCH_INDEX_PARTICIPANTS, ELASTICSEARCH_TYPE_PARTICIPANTS, participantsStatsSubCommunity);

            /**
             * index participant interaction results
             */
            LOGGER.info("Start generating participants directed graph representation.");
            org.json.simple.JSONObject participantInteractionSubcommunity = ec.generateParticipantViewD3(i + 1);
            LOGGER.info("participantInteractionSubcommunity: " + participantInteractionSubcommunity);
            elasticsearchService.indexParticipantGraphRepresentation(ELASTICSEARCH_INDEX_DIRECTED_GRAPH, ELASTICSEARCH_TYPE_DIRECTED_GRAPH, participantInteractionSubcommunity);

            LOGGER.info("Start generating hierarchical edge bundling.");
            org.json.simple.JSONObject hierarchicalEdgeBundlingSubcommunity = ec.generateHierarchicalEdgeBundling(i + 1);
            LOGGER.info("hierarchicalEdgeBundlingSubcommunity: " + hierarchicalEdgeBundlingSubcommunity);
            elasticsearchService.indexParticipantGraphRepresentation(ELASTICSEARCH_INDEX_EDGEBUNDLING, ELASTICSEARCH_TYPE_EDGEBUNDLING, hierarchicalEdgeBundlingSubcommunity);
        }

        LOGGER.info("\n------- Ending subcommunities processing -------\n");
        LOGGER.info("---------- Starting export community statistics to files --------\n");
        ExportCommunity export = new ExportCommunity(community);

	    export.exportIndividualStatsAndInitiation(PATH + "/" + communityName + "_" + INDIVIDUAL_STATS_FILENAME, PATH + "/" + communityName + "_" + INITIATION_FILENAME);
        export.exportTextualComplexity(PATH + "/" + communityName + "_" + TEXTUAL_COMPLEXITY);
        export.exportTimeAnalysis(PATH + "/" + communityName + "_" + TIME_ANALYSIS);
        export.exportDiscussedTopics(PATH + "/" + communityName + "_" + DISCUSSED_TOPICS);
        export.exportIndividualThreadStatistics(PATH + "/" + communityName + "_" + INDIVIDUAL_THREAD_STATISTICS);
    }

    public static void main(String[] args) {
        //processBarnesMOOC();

        //processMathEqualsLove();

        //processEDMMooc();

        processRedditCommunity();
        
        //processUsoData();
    }

    private static void processMathEqualsLove() {
        Lang lang = Lang.en;
        List<SemanticModel> models = SemanticModel.loadModels("coca", lang);
        List<Annotators> annotators = Arrays.asList(Annotators.NLP_PREPROCESSING, Annotators.DIALOGISM, Annotators.TEXTUAL_COMPLEXITY);
        String communityName = "Math Equals Love";

        ParallelConversationProcessingPipeline processingPipeline = new ParallelConversationProcessingPipeline(
                communityName, lang, models, annotators, 0, 7 );

        //processingPipeline.processCommunity("C:\\Users\\Administrator\\Desktop\\projects\\mathequalslove.blogspot.ro\\mathequalslove.blogspot.ro");
    }

    private static void processRedditCommunity() {
        Lang lang = Lang.en;
        List<SemanticModel> models = SemanticModel.loadModels("coca", lang);
        List<Annotators> annotators = Arrays.asList(Annotators.NLP_PREPROCESSING, Annotators.DIALOGISM, Annotators.TEXTUAL_COMPLEXITY);
        String communityName = "debatecommunism";

        ParallelConversationProcessingPipeline processingPipeline = new ParallelConversationProcessingPipeline(
                communityName, lang, models, annotators, 0, 7 );

	String threadsPath = "C:\\Users\\Dorinela\\Desktop\\catalin\\threads";
        processingPipeline.processCommunity(communityName, threadsPath);
    }

    private static void processEDMMooc() {
        Lang lang = Lang.en;
        List<SemanticModel> models = SemanticModel.loadModels("coca", lang);
        List<Annotators> annotators = Arrays.asList(Annotators.NLP_PREPROCESSING, Annotators.DIALOGISM, Annotators.TEXTUAL_COMPLEXITY);
        String communityName = "Education Data Mining MOOC";

        ParallelConversationProcessingPipeline processingPipeline = new ParallelConversationProcessingPipeline(
                communityName, lang, models, annotators, 0, 7 );

        //processingPipeline.startDate = new Date(1382630400);
        //processingPipeline.endDate = new Date(1387472400);
        //processingPipeline.processCommunity("C:\\Users\\Administrator\\ownCloud\\ReaderBench\\in\\MOOC\\forum_posts&comments");
    }

    private static void processBarnesMOOC() {
        Lang lang = Lang.en;
        List<SemanticModel> models = SemanticModel.loadModels("coca", lang);
        List<Annotators> annotators = Arrays.asList(Annotators.NLP_PREPROCESSING, Annotators.DIALOGISM, Annotators.TEXTUAL_COMPLEXITY);
        String communityName = "Online Math Course";

        ParallelConversationProcessingPipeline processingPipeline = new ParallelConversationProcessingPipeline(
                communityName, lang, models, annotators, 0, 7 );

        //processingPipeline.processCommunity("C:\\Users\\Administrator\\Nextcloud\\ReaderBench\\in\\Barnes_MOOC");
    }

    private static void processUsoData() {
        Lang lang = Lang.ro;
        List<SemanticModel> models = SemanticModel.loadModels("readme", lang);
        List<Annotators> annotators = Arrays.asList(Annotators.NLP_PREPROCESSING, Annotators.DIALOGISM, Annotators.TEXTUAL_COMPLEXITY);
        String communityName = "USO";

        ParallelConversationProcessingPipeline processingPipeline = new ParallelConversationProcessingPipeline(
                communityName, lang, models, annotators, 0, 7 );

        //processingPipeline.processCommunity("C:\\Users\\Administrator\\Nextcloud\\ReaderBench\\in\\uso");
        //processingPipeline.processCommunity("C:\\Users\\Administrator\\Nextcloud\\ReaderBench\\in\\uso\\uso_files_no_tags");
    }


}
