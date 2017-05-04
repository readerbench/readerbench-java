package akka.actors;

import akka.actor.UntypedActor;
import akka.messages.ConversationMessage;
import akka.messages.ConversationResponseMessage;
import com.readerbench.solr.entities.cscl.Conversation;
import data.AbstractDocument;
import data.Lang;
import org.openide.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.semanticModels.ISemanticModel;
import services.semanticModels.SimilarityType;

import java.io.File;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Created by Dorinela on 5/3/2017.
 */
public class ConversationActor extends UntypedActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConversationActor.class);

    private static boolean USE_POS_TAGGING = true;
    private static boolean COMPUTE_DIALOGISM = true;
    private static String LSA_PATH = "resources/config/EN/LSA/TASA_LAK";
    private static String LDA_PATH = "resources/config/EN/LDA/TASA_LAK";
    private static Lang LANGUAGE = Lang.en;

    public void onReceive(Object message) throws Exception {

        if (message instanceof ConversationMessage) {
            LOGGER.info("Received ConversationMessage ...");
            ConversationMessage conversationMessage = (ConversationMessage) message;

            /**
             * Process conversation
             */
            LOGGER.info("Start processing conversation {} ", conversationMessage.getConversation());
            AbstractDocument abstractDocument = processConversation(conversationMessage.getConversation(), LSA_PATH,
                    LDA_PATH, LANGUAGE, USE_POS_TAGGING, COMPUTE_DIALOGISM);
            LOGGER.info("End processing conversation {} ", conversationMessage.getConversation());

            /**
             * Send message response to CommunityActor
             */
            ConversationResponseMessage conversationResponseMessage = new ConversationResponseMessage(abstractDocument);
            sender().tell(conversationResponseMessage, self());

        }
    }

    /**
     * Process conversation
     * @param conversation - conversation
     * @param pathToLSA - path to LSA resources
     * @param pathToLDA - path to LDA resources
     * @param lang - language
     * @param usePOSTagging - use or not POSTagging
     * @param computeDialogism - compute or not dialogism
     * @return - AbstractDocument
     */
    public AbstractDocument processConversation(Conversation conversation, String pathToLSA, String pathToLDA, Lang lang,
                                    boolean usePOSTagging, boolean computeDialogism) {
        Map<SimilarityType, String> modelPaths = new EnumMap<>(SimilarityType.class);
        modelPaths.put(SimilarityType.LSA, pathToLSA);
        modelPaths.put(SimilarityType.LDA, pathToLDA);

        try {
            LOGGER.info("Processing conversation {}", conversation);
            Long start = System.currentTimeMillis();

            List<ISemanticModel> models = SimilarityType.loadVectorModels(modelPaths, lang);

            AbstractDocument abstractDocument = loadGenericDocument(conversation, models, lang, usePOSTagging,
                    computeDialogism);
            Long end = System.currentTimeMillis();

            LOGGER.info("Successfully finished processing conversation {}", conversation);
            return abstractDocument;
        } catch (Exception ex) {
            LOGGER.info("Error in process conversation {}", conversation);
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    /**
     * Load generic document
     * @param conversation - conversation
     * @param models - semantic models
     * @param lang - language
     * @param usePOSTagging - use or not POSTagging
     * @param computeDialogism - compute or not dialogism
     * @return - AbstractDocument
     */
    public AbstractDocument loadGenericDocument(Conversation conversation, List<ISemanticModel> models, Lang lang,
                                                boolean usePOSTagging, boolean computeDialogism) {

        data.cscl.Conversation c = data.cscl.Conversation.loadConversation(conversation, models, lang, usePOSTagging);
        c.computeAll(computeDialogism);

        return c;

    }
}
