package akka.actors;

import akka.actor.UntypedActor;
import akka.messages.DialogMessage;
import org.slf4j.LoggerFactory;

/**
 * Created by Dorinela on 3/21/2017.
 */
public class DialogProcessingActor extends UntypedActor {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DialogProcessingActor.class);

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof DialogMessage) {
            LOGGER.info("Received DialogMessage message.");
            DialogMessage dialogMessage = (DialogMessage) message;

            LOGGER.info("The number of dialogs for processing are: " + dialogMessage.getDialogs().size());

            //TODO - process dialogs

        } else {
            LOGGER.warn("Unhandled message.");
            unhandled(message);
        }

    }

}
