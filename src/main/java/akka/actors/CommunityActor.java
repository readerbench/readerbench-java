package akka.actors;

import akka.AkkaActorSystem;
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
import data.AbstractDocumentTemplate;
import data.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import services.nlp.parsing.Parsing;

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
    private static String INVOCATION_FILENAME = "invocation.csv";
    private static String PATH = "resources/out";

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof CommunityMessage) {
            LOGGER.info("Received CommunityMessage ...");
            String communityName = ((CommunityMessage) message).getCommunity();
            //FILENAME = communityName + ".csv";

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

            List<Conversation> conv = new ArrayList<>();
            conv.add(conversations.get(0));
            //conv.add(conversations.get(1));
            CONVERSATION_NUMBER = conv.size();
            List<ConversationMessage> messages = new ArrayList<>();
            conv.forEach(m -> {
                messages.add(new ConversationMessage(m, PATH));
            });
            LOGGER.info("The number of conversations for community {} are {}", communityName, CONVERSATION_NUMBER);


            Long start = System.currentTimeMillis();

//            for (Conversation conversation : conv) {
//                ConversationMessage conversationMessage = new ConversationMessage(conversation, "resources/out");
//                /**
//                 * send ConversationMessage to ConversationActor to process it
//                 */
//                LOGGER.info("Send ConversationMessage to ConversationActor to process it ... ");
//                AkkaActorSystem.conversationActor.tell(conversationMessage, self());
//            }

            List<Future<Object>> futures = new LinkedList<>();
            messages.forEach(msg -> {
                Timeout timeout = new Timeout(Duration.create(10, TimeUnit.MINUTES));
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
                        processDocumentCollection(abstractDocuments, Lang.en, false, false, null, null, 0, 7);
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
//                processDocumentCollection(abstractDocuments, Lang.en, false, false, null, null, 0, 7);
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
        community.setPath(PATH);
        if (community != null) {
            community.computeMetrics(useTextualComplexity, true, true);
            //community.export(PATH + "/" + FILENAME, true, true);

            community.exportIndividualStatsAndInvocation(PATH + "/" + INDIVIDUAL_STATS_FILENAME, PATH + "/" + INVOCATION_FILENAME);


            //dc.generateParticipantView(rootPath + "/" + f.getName() + "_participants.pdf");
            //dc.generateParticipantViewD3(rootPath + "/" + f.getName() + "_d3.json");
            //community.generateParticipantViewSubCommunities("D:\\Facultate\\MASTER\\ReaderBench\\ReaderBench\\resources\\out\\" + "CallOfDuty_d3_");
            //community.generateConceptView("D:\\Facultate\\MASTER\\ReaderBench\\ReaderBench\\resources\\out\\" + "CallOfDuty_concepts.pdf");
        }
    }
}
