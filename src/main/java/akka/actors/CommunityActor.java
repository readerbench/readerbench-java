package akka.actors;

import akka.AkkaActorSystem;
import akka.ConversationProcessing;
import akka.actor.UntypedActor;
import akka.dispatch.Futures;
import akka.dispatch.OnSuccess;
import akka.messages.CommunityMessage;
import akka.messages.ConversationMessage;
import akka.messages.ConversationResponseMessage;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.readerbench.solr.entities.cscl.Contribution;
import com.readerbench.solr.entities.cscl.Conversation;
import com.readerbench.solr.services.SolrService;
import data.AbstractDocument;
import data.Lang;
import data.cscl.Community;
import data.cscl.CommunityUtils;
import data.cscl.Participant;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import services.elasticsearch.ElasticsearchService;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dorinela on 3/17/2017.
 */
public class CommunityActor extends UntypedActor{

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunityActor.class);

    private static final String SOLR_ADDRESS = "http://141.85.232.56:8983/solr/";
    private static final String SOLR_COLLECTION = "community";
    SolrService solrService = new SolrService(SOLR_ADDRESS, SOLR_COLLECTION, Integer.MAX_VALUE);

    List<AbstractDocument> abstractDocuments = new ArrayList<>();
    private static int CONVERSATION_NUMBER = 0;
    private static String INDIVIDUAL_STATS_FILENAME = "individualStats.csv";
    private static String INITIATION_FILENAME = "initiation.csv";
    private static String TEXTUAL_COMPLEXITY = "textualComplexity.csv";
    private static String TIME_ANALYSIS = "timeAnalysis.csv";
    private static String DISCUSSED_TOPICS = "discussedTopics.csv";
    private static String INDIVIDUAL_THREAD_STATISTICS = "individualThreadStatistics.csv";
    private static String PARTICIPANT_VIEW_D3_FILE = "particiantViewD3.json";
    private static String PATH = "resources/out";
    private static String COMMUNITY_NAME;

    private ElasticsearchService elasticsearchService = new ElasticsearchService();

    private ConversationProcessing CONVERSATION_PROCESSING = new ConversationProcessing();

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof CommunityMessage) {
            LOGGER.info("Received CommunityMessage ...");
            String communityName = ((CommunityMessage) message).getCommunity();
            COMMUNITY_NAME = communityName;

            /**
             * get conversations for a community
             */
            List<Conversation> conversations = solrService.getConversationsForCommunity(communityName);

            //todo - delete this because the participantAliasName should be in SOLR.
            int i = 1;
            for (Conversation conversation : conversations) {
                i = 1;
                for (Contribution c : conversation.getContributions()) {
                    c.setParticipantAliasName("Test " + i++);
                }

            }

            ConversationMessage cm = new ConversationMessage(conversations.get(0), PATH);
            LOGGER.info("Start processing first conversation.");
            AbstractDocument abstractDocument = CONVERSATION_PROCESSING.loadGenericDocumentFromConversation(cm, CONVERSATION_PROCESSING.MODELS,
                    CONVERSATION_PROCESSING.LANGUAGE,CONVERSATION_PROCESSING.USE_POS_TAGGING, CONVERSATION_PROCESSING.COMPUTE_DIALOGISM);
            LOGGER.info("End processing first conversation.");
            abstractDocuments.add(abstractDocument);


            List<Conversation> conv = new ArrayList<>();
            for (int j = 1; j < 10; j++) {
                conv.add(conversations.get(j));
            }
            CONVERSATION_NUMBER = conv.size() + 1;
            List<ConversationMessage> messages = new ArrayList<>();
            conv.forEach(m -> {
                messages.add(new ConversationMessage(m, PATH));
            });
            LOGGER.info("The number of conversations for community {} are {}", communityName, CONVERSATION_NUMBER);


            Long start = System.currentTimeMillis();

//            for (Conversation conversation : conv) {
//                ConversationMessage conversationMessage = new ConversationMessage(conversation, PATH);
//                /**
//                 * send ConversationMessage to ConversationActor to process it
//                 */
//                LOGGER.info("Send ConversationMessage to ConversationActor to process it ... ");
//                AkkaActorSystem.conversationActor.tell(conversationMessage, self());
//            }

            List<Future<Object>> futures = new LinkedList<>();
            messages.forEach(msg -> {
                Timeout timeout = new Timeout(Duration.create(5, TimeUnit.MINUTES));
                Future<Object> future = Patterns.ask(AkkaActorSystem.conversationActor, msg, timeout);
                futures.add(future);
            });

            Future<Iterable<Object>> futureResult = Futures.sequence(futures, AkkaActorSystem.ACTOR_SYSTEM.dispatcher());

            futureResult.onSuccess(new OnSuccess<Iterable<Object>>(){
                @Override
                public void onSuccess(Iterable<Object> conversationResponseMessages) throws Throwable {
                    conversationResponseMessages.forEach(msg -> {
                        LOGGER.info("Received ConversationResponseMessage ...");
                        ConversationResponseMessage cm = (ConversationResponseMessage) msg;
                        abstractDocuments.add(cm.getAbstractDocument());
                    });

                    if (abstractDocuments.size() == CONVERSATION_NUMBER) {
                        LOGGER.info("Start processing document collection ...");
                        processDocumentCollection(abstractDocuments, Lang.en, false, true, null, null, 0, 7);
                        LOGGER.info("End processing collection ...Took {} millis", System.currentTimeMillis() - start);
                    }
                }
            }, AkkaActorSystem.ACTOR_SYSTEM.dispatcher());
        }
//       else if (message instanceof ConversationResponseMessage) {
//            LOGGER.info("Received ConversationResponseMessage ...");
//            ConversationResponseMessage conversationResponseMessage = (ConversationResponseMessage) message;
//
//            abstractDocuments.add(conversationResponseMessage.getAbstractDocument());
//            if (abstractDocuments.size() == CONVERSATION_NUMBER) {
//                LOGGER.info("End processing all conversations ...");
//                LOGGER.info("Start processing document collection ...");
//                processDocumentCollection(abstractDocuments, Lang.en, false, true, null, null, 0, 7);
//                LOGGER.info("------------- End processing document collection --------- ");
//            }
//        }
        else {
            LOGGER.warn("Unhandled message.");
            unhandled(message);
        }

    }

    public void processDocumentCollection(List<AbstractDocument> abstractDocumentList, Lang lang,
                                                 boolean needsAnonymization, boolean useTextualComplexity, Date startDate,
                                                 Date endDate, int monthIncrement, int dayIncrement) {
        LOGGER.info("The number of abstract documents to process: {}", abstractDocumentList.size());
        data.cscl.Community community = new data.cscl.Community(lang, needsAnonymization, startDate,
                endDate);
        community = community.loadMultipleConversations(abstractDocumentList, lang, needsAnonymization, startDate,
                endDate, monthIncrement, dayIncrement, PATH);

        if (community != null) {
            community.computeMetrics(useTextualComplexity, true, true);

            System.out.println("\n----------- Subcommunities stats -------- \n");
//            for (Community c : community.getTimeframeSubCommunities()) {
//                CommunityUtils.hierarchicalClustering(c, PATH + "/clustered_results.csv", PATH);
//                System.out.println(c.getStartDate() + " - " + c.getEndDate());
//                System.out.println(c.getParticipants().size());
//                for (Participant p : c.getParticipants()) {
//                    if (p.getParticipantGroup() != null) {
//                        System.out.println(p.getName() + " - " + p.getParticipantGroup().name());
//                    }
//                }
//                System.out.println("\n\n");
//            }


            for (int i = 0; i < community.getTimeframeSubCommunities().size(); i++) {
                Community subCommunity = community.getTimeframeSubCommunities().get(i);
//                CommunityUtils.hierarchicalClustering(subCommunity, PATH + "/clustered_results_week_" + i + ".csv", PATH);
//                System.out.println(subCommunity.getStartDate());
//                System.out.println(subCommunity.getEndDate());
//
//                List<Map<String, Object>> participantsStats = subCommunity
//                        .writeIndividualStatsToElasticsearch(COMMUNITY_NAME, i + 1);
//                System.out.println(participantsStats);
//                elasticsearchService.indexParticipantsStats(participantsStats);

                /**
                 * index participant interaction results
                 */
                JSONObject participantInteraction = subCommunity.generateParticipantViewD3(COMMUNITY_NAME, i + 1);
                elasticsearchService.indexParticipantGraphRepresentation("participants", "directedGraph", participantInteraction);
                System.out.println(participantInteraction);

                JSONObject hierarchicalEdgeBundling = subCommunity.generateHierarchicalEdgeBundling(COMMUNITY_NAME, i + 1);
                elasticsearchService.indexParticipantGraphRepresentation("participants", "edgeBundling", hierarchicalEdgeBundling);
                System.out.println(hierarchicalEdgeBundling);
            }


//            community.exportIndividualStatsAndInitiation(PATH + "/" + COMMUNITY_NAME + "_" + INDIVIDUAL_STATS_FILENAME,
//                    PATH + "/" + COMMUNITY_NAME + "_" + INITIATION_FILENAME);
//            community.exportTextualComplexity(PATH + "/" + COMMUNITY_NAME + "_" + TEXTUAL_COMPLEXITY);
//            community.exportTimeAnalysis(PATH + "/" + COMMUNITY_NAME + "_" + TIME_ANALYSIS);
//            community.exportDiscussedTopics(PATH + "/" + COMMUNITY_NAME + "_" + DISCUSSED_TOPICS);
//            community.exportIndividualThreadStatistics(PATH + "/" + COMMUNITY_NAME + "_" + INDIVIDUAL_THREAD_STATISTICS);

        }
    }
}
