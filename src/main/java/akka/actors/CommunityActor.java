package akka.actors;

import akka.actor.UntypedActor;
import akka.messages.CommunityMessage;
import akka.messages.ConversationMessage;
import akka.messages.ConversationResponseMessage;
import com.readerbench.solr.entities.cscl.Community;
import com.readerbench.solr.entities.cscl.Contribution;
import com.readerbench.solr.entities.cscl.Conversation;
import com.readerbench.solr.services.SolrService;
import data.AbstractDocument;
import data.Block;
import data.Lang;
import data.Word;
import data.cscl.CSCLCriteria;
import data.cscl.CSCLIndices;
import data.cscl.Participant;
import data.cscl.Utterance;
import org.apache.solr.client.solrj.SolrServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;
import services.commons.VectorAlgebra;
import services.complexity.ComplexityIndices;
import services.discourse.CSCL.ParticipantEvaluation;
import services.discourse.cohesion.CohesionGraph;
import services.semanticModels.ISemanticModel;
import services.semanticModels.SimilarityType;
import services.solr.TestActors;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Created by Dorinela on 3/17/2017.
 */
public class CommunityActor extends UntypedActor{

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunityActor.class);

    private static final String START_PROCESSING = "START_PROCESSING";

    private static final String SOLR_ADDRESS = "http://141.85.232.56:8983/solr/";
    private static final String SOLR_COLLECTION = "community";
    SolrService solrService = new SolrService(SOLR_ADDRESS, SOLR_COLLECTION, Integer.MAX_VALUE);

    List<AbstractDocument> abstractDocuments = new ArrayList<>();
    private static int CONVERSATION_NUMBER = 0;
    private static String FILENAME;
    private static String PATH = "resources/out";

    @Override
    public void preStart() {

    }

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof CommunityMessage) {
            LOGGER.info("Received CommunityMessage ...");
            String communityName = ((CommunityMessage) message).getCommunity();
            FILENAME = communityName + ".csv";

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

            CONVERSATION_NUMBER = conversations.size();
            LOGGER.info("The number of conversations for community {} are {}", communityName, CONVERSATION_NUMBER);

            try {
                Long start = System.currentTimeMillis();

                for (Conversation conversation : conversations) {
                    ConversationMessage conversationMessage = new ConversationMessage(conversation, "resources/out");
                    /**
                     * send ConversationMessage to ConversationActor to process it
                     */
                    LOGGER.info("Send ConversationMessage to ConversationActor to process it ... ");
                    TestActors.akkaActorSystem.conversationActor.tell(conversationMessage, self());

                }
            } catch (Exception e) {
                LOGGER.info("Error in loading vector models!!!");
            }


        } else if (message instanceof ConversationResponseMessage) {
            LOGGER.info("Received ConversationResponseMessage ...");
            ConversationResponseMessage conversationResponseMessage = (ConversationResponseMessage) message;

            abstractDocuments.add(conversationResponseMessage.getAbstractDocument());
            if (abstractDocuments.size() == CONVERSATION_NUMBER) {
                LOGGER.info("End processing all conversations ...");
                LOGGER.info("Start processing document collection ...");
                processDocumentCollection(abstractDocuments, Lang.en, false, false, null, null, 0, 7);
                LOGGER.info("------------- End processing document collection --------- ");
            }

        } else {
            LOGGER.warn("Unhandled message.");
            unhandled(message);
        }

    }

    public void processDocumentCollection(List<AbstractDocument> abstractDocumentList, Lang lang,
                                                 boolean needsAnonymization, boolean useTextualComplexity, Date startDate,
                                                 Date endDate, int monthIncrement, int dayIncrement) {
        data.cscl.Community community = new data.cscl.Community();
        community.loadMultipleConversations(abstractDocumentList, lang, needsAnonymization, startDate,
                endDate, monthIncrement, dayIncrement);
        community.setPath(PATH);
        if (community != null) {
            community.computeMetrics(useTextualComplexity, true, true);
            community.export(PATH + "/" + FILENAME, true, true);
            //dc.generateParticipantView(rootPath + "/" + f.getName() + "_participants.pdf");
            //dc.generateParticipantViewD3(rootPath + "/" + f.getName() + "_d3.json");
            //community.generateParticipantViewSubCommunities("D:\\Facultate\\MASTER\\ReaderBench\\ReaderBench\\resources\\out\\" + "CallOfDuty_d3_");
            //community.generateConceptView("D:\\Facultate\\MASTER\\ReaderBench\\ReaderBench\\resources\\out\\" + "CallOfDuty_concepts.pdf");
        }
    }

    /**
     * compute the time until the job starts
     *
     * In this case the job will start every day at 12:00 PM
     *
     * @return
     */
    private Long computeDelayOfJob() {
        GregorianCalendar gCalendar = new GregorianCalendar();
        gCalendar.set(GregorianCalendar.HOUR_OF_DAY, 23);
        gCalendar.set(GregorianCalendar.MINUTE, 58);
        gCalendar.set(GregorianCalendar.SECOND, 0);
        Long delay = gCalendar.getTimeInMillis() - System.currentTimeMillis();

        if (delay < 0) {
            gCalendar.add(GregorianCalendar.DAY_OF_MONTH, 1);
            delay = gCalendar.getTimeInMillis() - System.currentTimeMillis();
        }

        return  delay;
    }
}
