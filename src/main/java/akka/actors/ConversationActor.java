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
    private static String LSA_PATH = "resources/config/EN/LSA/TASA_LAK";
    private static String LDA_PATH = "resources/config/EN/LDA/TASA_LAK";
    private static String PATH = "resources/out";
    private static Lang LANGUAGE = Lang.en;

    public void onReceive(Object message) throws Exception {

        if (message instanceof ConversationMessage) {
            LOGGER.info("Received ConversationMessage ...");
            ConversationMessage conversationMessage = (ConversationMessage) message;

            /**
             * Process conversation
             */
            LOGGER.info("Start processing conversation.");
            AbstractDocument abstractDocument = processConversationMessage(conversationMessage, LSA_PATH,
                    LDA_PATH, LANGUAGE, USE_POS_TAGGING, COMPUTE_DIALOGISM);
            LOGGER.info("End processing conversation.");

            /**
             * Send message response to CommunityActor
             */
            LOGGER.info("Send ConversationResponseMessage to Community actor ... ");
            ConversationResponseMessage conversationResponseMessage = new ConversationResponseMessage(abstractDocument);
            sender().tell(conversationResponseMessage, self());

        }
    }

    /**
     * Process conversation
     * @param conversationMessage - conversationMessage
     * @param pathToLSA - path to LSA resources
     * @param pathToLDA - path to LDA resources
     * @param lang - language
     * @param usePOSTagging - use or not POSTagging
     * @param computeDialogism - compute or not dialogism
     * @return - AbstractDocument
     */
    public AbstractDocument processConversationMessage(ConversationMessage conversationMessage, String pathToLSA, String pathToLDA, Lang lang,
                                    boolean usePOSTagging, boolean computeDialogism) {
        Map<SimilarityType, String> modelPaths = new EnumMap<>(SimilarityType.class);
        modelPaths.put(SimilarityType.LSA, pathToLSA);
        modelPaths.put(SimilarityType.LDA, pathToLDA);
        //modelPaths.put(SimilarityType.WORD2VEC, pathToWORD2VEC);

        try {
            Long start = System.currentTimeMillis();

            LOGGER.info("Load vector models.");
            List<ISemanticModel> models = SimilarityType.loadVectorModels(modelPaths, lang);

            LOGGER.info("Load generic document.");
            AbstractDocument abstractDocument = loadGenericDocument(conversationMessage, models, lang, usePOSTagging,
                    computeDialogism);
            Long end = System.currentTimeMillis();

            LOGGER.info("Successfully finished processing conversation");
            return abstractDocument;
        } catch (Exception ex) {
            LOGGER.info("Error in process conversation");
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    /**
     * Load generic document
     * @param conversationMessage - conversationMessage
     * @param models - semantic models
     * @param lang - language
     * @param usePOSTagging - use or not POSTagging
     * @param computeDialogism - compute or not dialogism
     * @return - AbstractDocument
     */
    public AbstractDocument loadGenericDocument(ConversationMessage conversationMessage, List<ISemanticModel> models, Lang lang,
                                                boolean usePOSTagging, boolean computeDialogism) {

        data.cscl.Conversation c = data.cscl.Conversation.loadConversation(conversationMessage.getConversation(), models, lang, usePOSTagging);
        if (c != null) {
            c.setPath(conversationMessage.getPath());
            computeAll(computeDialogism, c);
        }

        return c;

    }

    public void computeAll(boolean computeDialogism, data.cscl.Conversation conversation) {
        //super.computeAll(computeDialogism, conversation);
        computeDiscourseAnalysisForAbstractDocument(computeDialogism, conversation);
        ComplexityIndices.computeComplexityFactors(conversation);

        conversation.getParticipants().stream().forEach((p) -> {
            KeywordModeling.determineKeywords(p.getContributions());
        });

        Collaboration.evaluateSocialKB(conversation);
        conversation.setVoicePMIEvolution(DialogismMeasures.getCollaborationEvolution(conversation));
        // Collaboration.printIntenseCollabZones(this);

        DialogismComputations.determineParticipantInterAnimation(conversation);

        // evaluate participants
        ParticipantEvaluation.evaluateInteraction(conversation);
        ParticipantEvaluation.evaluateInvolvement(conversation);
        ParticipantEvaluation.performSNA(conversation);
        ParticipantEvaluation.evaluateUsedConcepts(conversation);
    }

    public void computeDiscourseAnalysisForAbstractDocument(boolean computeDialogism, data.cscl.Conversation conversation) {
        if (computeDialogism) {
            // build disambiguisation graph and lexical chains
            DisambiguisationGraphAndLexicalChains.buildDisambiguationGraph(conversation);
            DisambiguisationGraphAndLexicalChains.pruneDisambiguationGraph(conversation);
            // System.out.println(d.disambiguationGraph);

            DisambiguisationGraphAndLexicalChains.buildLexicalChains(conversation);
            // for (LexicalChain chain : lexicalChains) {
            // System.out.println(chain);
            // }

            DisambiguisationGraphAndLexicalChains.computeWordDistances(conversation);
            // System.out.println(LexicalCohesion.getDocumentCohesion(this));

            // determine semantic chains / voices
            DialogismComputations.determineVoices(conversation);

            // determine voice distributions & importance
            DialogismComputations.determineVoiceDistributions(conversation);
        }

        // build coherence graph
        CohesionGraph.buildCohesionGraph(conversation);

//        t1 = System.currentTimeMillis();
//        // build coherence graph
//        CohesionGraph.buildCohesionGraphOld(this);
//        t2 = System.currentTimeMillis();
//        System.out.println("old cohesion time: " + ((t2 - t1) / 1000.) + " sec");
        // determine topics
        KeywordModeling.determineKeywords(conversation);
        // TopicModel.determineTopicsLDA(this);

        Scoring.score(conversation);
        // assign sentiment values
        SentimentAnalysis.weightSemanticValences(conversation);

        LOGGER.info("Finished all discourse analysis processes ...");
    }
}
