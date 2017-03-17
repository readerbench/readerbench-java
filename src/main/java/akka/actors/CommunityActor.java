package akka.actors;

import akka.actor.UntypedActor;
import akka.messages.CommunityMessage;

/**
 * Created by Dorinela on 3/17/2017.
 */
public class CommunityActor extends UntypedActor{

    @Override
    public void onReceive(Object o) throws Exception {
        if (o instanceof CommunityMessage) {
            CommunityMessage message = (CommunityMessage) o;
            //TODO
        }

    }
}
