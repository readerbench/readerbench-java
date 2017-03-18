package akka.actors;

import akka.actor.UntypedActor;
import akka.messages.CommunityMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Dorinela on 3/18/2017.
 */
public class DataProcessingActor extends UntypedActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataProcessingActor.class);

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof CommunityMessage) {
            LOGGER.info("Received CommunityMessage message.");
            CommunityMessage communityMessage = (CommunityMessage) message;

            // TODO - processing data

        } else {
            LOGGER.warn("Unhandled message.");
            unhandled(message);
        }

    }
}
