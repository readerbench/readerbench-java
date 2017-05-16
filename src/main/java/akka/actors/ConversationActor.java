package akka.actors;

import akka.ConversationProcessing;
import akka.actor.UntypedActor;
import akka.messages.ConversationMessage;
import akka.messages.ConversationResponseMessage;
import data.AbstractDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by Dorinela on 5/3/2017.
 */
public class ConversationActor extends UntypedActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConversationActor.class);

    private ConversationProcessing CONVERSATION_PROCESSING = new ConversationProcessing();

    public void onReceive(Object message) throws Exception {

        if (message instanceof ConversationMessage) {
            LOGGER.info("Received ConversationMessage ...");
            ConversationMessage conversationMessage = (ConversationMessage) message;

            /**
             * Process conversation
             */
            LOGGER.info("Start processing conversation.");
            //ConversationProcessing conversationProcessing = new ConversationProcessing();
            AbstractDocument abstractDocument = CONVERSATION_PROCESSING.loadGenericDocumentFromConversation(conversationMessage,
                    CONVERSATION_PROCESSING.MODELS, CONVERSATION_PROCESSING.LANGUAGE, CONVERSATION_PROCESSING.USE_POS_TAGGING,
                    CONVERSATION_PROCESSING.COMPUTE_DIALOGISM);

            LOGGER.info("End processing conversation.");

            /**
             * Send message response to CommunityActor
             */
            LOGGER.info("Send ConversationResponseMessage to Community actor ... ");
            ConversationResponseMessage conversationResponseMessage = new ConversationResponseMessage(abstractDocument);
            sender().tell(conversationResponseMessage, self());

        }
    }
}
