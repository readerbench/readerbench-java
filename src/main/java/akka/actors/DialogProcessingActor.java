package akka.actors;

import akka.actor.UntypedActor;
import akka.messages.DialogMessage;
import data.AbstractDocument;
import data.Lang;
import data.cscl.Community;
import org.slf4j.LoggerFactory;
import services.converters.lifeConverter.Dialog;
import services.processing.SerialProcessing;

import java.io.File;
import java.util.Date;

/**
 * Created by Dorinela on 3/21/2017.
 */
public class DialogProcessingActor extends UntypedActor {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DialogProcessingActor.class);
    private static Lang LANGUAGE = Lang.en;
    private static boolean NEEDS_ANONYMIZATION = false;
    private static boolean USE_TEXTUAL_COMPLEXITY = false;
    private static Date START_DATE = null;
    private static Date END_DATE = null;
    private static int MONTH_INCREMENT = 0;
    private static int DAY_INCREMENT = 7;
    private static boolean USE_POS_TAGGING = true;
    private static boolean COMPUTE_DIALOGISM = true;
    private static String LSA_PATH = "resources/config/EN/LSA/TASA_LAK";
    private static String LDA_PATH = "resources/config/EN/LDA/TASA_LAK";
    private static String ROOT_PATH = "C:\\Users\\Dorinela\\Desktop\\results_test";

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof DialogMessage) {
            LOGGER.info("Received DialogMessage message.");
            DialogMessage dialogMessage = (DialogMessage) message;

            LOGGER.info("The number of dialogs for processing are: " + dialogMessage.getDialogs().size());

            //TODO - test process dialogs
//            SerialProcessing.processCorpusDialogs(dialogMessage.getDialogs(),"resources/config/EN/LSA/TASA_LAK",
//                    "resources/config/EN/LDA/TASA_LAK", Lang.en, true, true);
            Community community = new Community().loadMultipleDialogs(dialogMessage.getDialogs(), LANGUAGE,
                    NEEDS_ANONYMIZATION, START_DATE, END_DATE, MONTH_INCREMENT, DAY_INCREMENT, LSA_PATH, LDA_PATH,
                    USE_POS_TAGGING, COMPUTE_DIALOGISM);

            if (community != null) {
                community.computeMetrics(USE_TEXTUAL_COMPLEXITY, true, true);
                File f = new File(ROOT_PATH);
                community.export(ROOT_PATH + "/" + f.getName() + ".csv", true, true);
                //dc.generateParticipantView(rootPath + "/" + f.getName() + "_participants.pdf");
                //dc.generateParticipantViewD3(rootPath + "/" + f.getName() + "_d3.json");
                community.generateParticipantViewSubCommunities(ROOT_PATH + "/" + f.getName() + "_d3_");
                community.generateConceptView(ROOT_PATH + "/" + f.getName() + "_concepts.pdf");
            }

        } else {
            LOGGER.warn("Unhandled message.");
            unhandled(message);
        }

    }

}
