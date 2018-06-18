/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.parallelprocessingservice;

import akka.pattern.Patterns;
import com.readerbench.coreservices.data.cscl.Community;
import com.readerbench.coreservices.data.cscl.Conversation;
import com.readerbench.coreservices.data.AbstractDocumentTemplate;
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
import java.io.File;
import java.util.*;

import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

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
    private static final String PATH = "resources/out";

    private String communityName;
    private Lang lang;
    private List<SemanticModel> models;
    private List<Annotators> annotators;
    private Date startDate;
    private Date endDate;
    private int monthIncrement;
    private int dayIncrement;
    private ElasticsearchService elasticsearchService = new ElasticsearchService();

    public ParallelConversationProcessingPipeline(String communityName, Lang lang, List<SemanticModel> models, List<Annotators> annotators, int monthIncrement, int dayIncrement) {
        this.communityName = communityName;
        this.lang = lang;
        this.models = models;
        this.annotators = annotators;
        this.monthIncrement = monthIncrement;
        this.dayIncrement = dayIncrement;
    }

    public List<Conversation> loadXMLsFromDirectory(String directoryPath) {
        ConversationProcessingPipeline pipeline = new ConversationProcessingPipeline(lang, models, annotators);
        List<AbstractDocumentTemplate> templates = new ArrayList<>();

        File folder = new File(directoryPath);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; ++i) {
            if (listOfFiles[i].isFile()) {
                AbstractDocumentTemplate template = pipeline.extractConvTemplateFromXML(directoryPath + "/" + listOfFiles[i].getName());
                templates.add(template);
            }
        }

        ProcessDocumentsInitMessage initMsg = new ProcessDocumentsInitMessage(templates, lang, models, annotators);

        Future<Object> future = Patterns.ask(ConversationActorSystem.PROCESSING_MASTER, initMsg, ConversationActorSystem.TIMEOUT * 10000);

        List<Conversation> listOfProcessedConversations = null;
        try {
            Object result = Await.result(future, Duration.Inf());
            listOfProcessedConversations = (List<Conversation>) result;
        } catch (Exception e) {
            LOGGER.error("Error in processing the conversations in a parallel manner. Error message: " + e.getMessage());
        }

        ConversationActorSystem.ACTOR_SYSTEM.terminate();

        return listOfProcessedConversations;
    }

    public void processCommunity(String directoryPath) {
        CommunityProcessingPipeline pipeline = new CommunityProcessingPipeline(lang, models, annotators);
        List<Conversation> conversations = loadXMLsFromDirectory(directoryPath);
        Community community = pipeline.createCommunityFromConversations(communityName, conversations, startDate, endDate);
        pipeline.processCommunity(community);
        pipeline.processTimeSeries(community, monthIncrement, dayIncrement);

        CommunityUtils.hierarchicalClustering(community, PATH + "/clustered_results_" + communityName + "_week_" + 0 + ".csv");
        ExportCommunityToES ec = new ExportCommunityToES(community);

        List<Map<String, Object>> participantsStats = ec.writeIndividualStatsToElasticsearch(0);
        LOGGER.info("participantsStats: " + participantsStats);
        //elasticsearchService.indexParticipantsStats(participantsStats);
        /**
         * index participant interaction results
         */
        LOGGER.info("Start generating participants directed graph representation.");
        JSONObject participantInteraction = ec.generateParticipantViewD3(0);
        LOGGER.info("participantInteraction: " + participantInteraction);
        //elasticsearchService.indexParticipantGraphRepresentation("participants", "directedGraph", participantInteraction);

        LOGGER.info("Start generating hierarchical edge bundling.");
        JSONObject hierarchicalEdgeBundling = ec.generateHierarchicalEdgeBundling(0);
        LOGGER.info("hierarchicalEdgeBundling: " + hierarchicalEdgeBundling);
        //elasticsearchService.indexParticipantGraphRepresentation("participants", "edgeBundling", hierarchicalEdgeBundling);

        LOGGER.info("\n----------- Subcommunities stats -------- \n");
        LOGGER.info("\n----------- The number of communities are: " + community.getTimeframeSubCommunities().size() + "\n");
        for (int i = 0; i < community.getTimeframeSubCommunities().size(); i++) {
            LOGGER.info("Start extracting statistics for community " + (i + 1));
            Community subCommunity = community.getTimeframeSubCommunities().get(i);
            CommunityUtils.hierarchicalClustering(subCommunity, PATH + "/" + communityName + "_clustered_results_week_" + (i + 1) + ".csv");
            ec = new ExportCommunityToES(subCommunity);

            List<Map<String, Object>> participantsStatsSubCommunity = ec.writeIndividualStatsToElasticsearch(i + 1);
            LOGGER.info("participantsStatsSubCommunity: " + participantsStatsSubCommunity);
            //elasticsearchService.indexParticipantsStats(participantsStatsSubCommunity);

            /**
             * index participant interaction results
             */
            LOGGER.info("Start generating participants directed graph representation.");
            JSONObject participantInteractionSubcommunity = ec.generateParticipantViewD3(i + 1);
            LOGGER.info("participantInteractionSubcommunity: " + participantInteractionSubcommunity);
            //elasticsearchService.indexParticipantGraphRepresentation("participants", "directedGraph", participantInteractionSubcommunity);

            LOGGER.info("Start generating hierarchical edge bundling.");
            JSONObject hierarchicalEdgeBundlingSubcommunity = ec.generateHierarchicalEdgeBundling(i + 1);
            LOGGER.info("hierarchicalEdgeBundlingSubcommunity: " + hierarchicalEdgeBundlingSubcommunity);
            //elasticsearchService.indexParticipantGraphRepresentation("participants", "edgeBundling", hierarchicalEdgeBundlingSubcommunity);
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
        processMathCourse();

    }

    private static void processMathCourse() {
        Lang lang = Lang.en;
        List<SemanticModel> models = SemanticModel.loadModels("coca", lang);
        List<Annotators> annotators = Arrays.asList(Annotators.NLP_PREPROCESSING, Annotators.DIALOGISM, Annotators.TEXTUAL_COMPLEXITY);
        String communityName = "Online Math Course";

        ParallelConversationProcessingPipeline processingPipeline = new ParallelConversationProcessingPipeline(
                communityName, lang, models, annotators, 0, 7 );

        processingPipeline.processCommunity("C:\\Users\\Dorinela\\Desktop\\mooc");
    }

    private static void processEDMMooc() {
        Lang lang = Lang.en;
        List<SemanticModel> models = SemanticModel.loadModels("coca", lang);
        List<Annotators> annotators = Arrays.asList(Annotators.NLP_PREPROCESSING, Annotators.DIALOGISM, Annotators.TEXTUAL_COMPLEXITY);
        String communityName = "EDM MOOC";

        ParallelConversationProcessingPipeline processingPipeline = new ParallelConversationProcessingPipeline(
                communityName, lang, models, annotators, 0, 7 );

        processingPipeline.startDate = new Date(1382630400);
        processingPipeline.endDate = new Date(1387472400);
        processingPipeline.processCommunity("C:\\Users\\Dorinela\\Desktop\\mooc");
    }


}
