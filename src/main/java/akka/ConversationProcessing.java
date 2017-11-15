package akka;

import akka.messages.ConversationMessage;
import data.AbstractDocument;
import data.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import services.semanticModels.ISemanticModel;
import services.semanticModels.SimilarityType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Dorinela on 5/16/2017.
 */
public class ConversationProcessing {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConversationProcessing.class);

    public static boolean USE_POS_TAGGING = true;
    public static boolean COMPUTE_DIALOGISM = true;
    public static Lang LANGUAGE = Lang.en;
    public static String LSA_PATH = "resources/config/EN/LSA/TASA_LAK";
    public static String LDA_PATH = "resources/config/EN/LDA/TASA_LAK";
    public static List<ISemanticModel> MODELS;

    static {
        Map<SimilarityType, String> modelPaths = new EnumMap<>(SimilarityType.class);
        modelPaths.put(SimilarityType.LSA, LSA_PATH);
        modelPaths.put(SimilarityType.LDA, LDA_PATH);
        MODELS = SimilarityType.loadVectorModels(modelPaths, LANGUAGE);
    }

    public AbstractDocument loadGenericDocumentFromConversation(ConversationMessage message,
                                                                List<ISemanticModel> models, Lang lang,
                                                                boolean usePOSTagging, boolean computeDialogism) {
        data.cscl.Conversation c = new data.cscl.Conversation(models, lang).loadConversation(message.getConversation(), models, lang, usePOSTagging);
        c.setPath(message.getPath());
        LOGGER.info("Start computeAll ... ");
        c.computeAll(computeDialogism, false);
        return c;
    }

}
