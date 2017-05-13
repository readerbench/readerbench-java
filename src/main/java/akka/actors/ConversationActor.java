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
import services.complexity.ComplexityIndices;
import services.discourse.CSCL.Collaboration;
import services.discourse.CSCL.ParticipantEvaluation;
import services.discourse.cohesion.CohesionGraph;
import services.discourse.cohesion.DisambiguisationGraphAndLexicalChains;
import services.discourse.cohesion.SentimentAnalysis;
import services.discourse.dialogism.DialogismComputations;
import services.discourse.dialogism.DialogismMeasures;
import services.discourse.keywordMining.KeywordModeling;
import services.discourse.keywordMining.Scoring;
import services.semanticModels.ISemanticModel;
import services.semanticModels.SimilarityType;

import java.io.File;
import java.util.ArrayList;
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
    private static String PATH = "resources/out";
    private static Lang LANGUAGE = Lang.en;
    private static String LSA_PATH = "resources/config/EN/LSA/TASA_LAK";
    private static String LDA_PATH = "resources/config/EN/LDA/TASA_LAK";
    private static List<ISemanticModel> MODELS;

    static {
        Map<SimilarityType, String> modelPaths = new EnumMap<>(SimilarityType.class);
        modelPaths.put(SimilarityType.LSA, LSA_PATH);
        modelPaths.put(SimilarityType.LDA, LDA_PATH);
        MODELS = SimilarityType.loadVectorModels(modelPaths, LANGUAGE);
    }

    public void onReceive(Object message) throws Exception {

        if (message instanceof ConversationMessage) {
            LOGGER.info("Received ConversationMessage ...");
            ConversationMessage conversationMessage = (ConversationMessage) message;

            /**
             * Process conversation
             */
            LOGGER.info("Start processing conversation.");
            AbstractDocument abstractDocument = loadGenericDocumentFromConversation(conversationMessage, MODELS, LANGUAGE,
                    USE_POS_TAGGING, COMPUTE_DIALOGISM);
            LOGGER.info("End processing conversation.");

            /**
             * Send message response to CommunityActor
             */
            LOGGER.info("Send ConversationResponseMessage to Community actor ... ");
            ConversationResponseMessage conversationResponseMessage = new ConversationResponseMessage(abstractDocument);
            sender().tell(conversationResponseMessage, self());

        }
    }

    public AbstractDocument loadGenericDocumentFromConversation(ConversationMessage message,
                                                                List<ISemanticModel> models, Lang lang,
                                                                boolean usePOSTagging, boolean computeDialogism) {
        data.cscl.Conversation c = new data.cscl.Conversation().loadConversation(message.getConversation(), models, lang, usePOSTagging);
        c.setPath(message.getPath());
        LOGGER.info("Start computeAll ... ");
        c.computeAll(computeDialogism);
        return c;
    }
}
