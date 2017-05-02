package akka.actors;

import akka.actor.UntypedActor;
import akka.messages.ConversationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Dorinela on 5/3/2017.
 */
public class ConversationActor extends UntypedActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConversationActor.class);

    public void onReceive(Object message) throws Exception {

        if (message instanceof ConversationMessage) {
            ConversationMessage conversationMessage = (ConversationMessage) message;

            //todo
        }
    }
}
