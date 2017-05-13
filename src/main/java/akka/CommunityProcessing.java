package akka;

import akka.messages.CommunityMessage;

/**
 * Created by Dorinela on 5/13/2017.
 */
public class CommunityProcessing {

    public static void main(String[] args) {
        /**
         * init akka actor system
         */
        AkkaActorSystem.init();

        CommunityMessage communityMessage = new CommunityMessage("CallOfDuty");
        AkkaActorSystem.communityActor.tell(communityMessage, null);

    }
}
