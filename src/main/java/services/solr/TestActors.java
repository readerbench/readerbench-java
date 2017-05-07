package services.solr;

import akka.AkkaActorSystem;
import akka.messages.CommunityMessage;

/**
 * Created by Dorinela on 3/18/2017.
 */
public class TestActors {

    public static AkkaActorSystem akkaActorSystem;

    public static void main(String[] args) {

        akkaActorSystem = new AkkaActorSystem();
        akkaActorSystem.init();

        CommunityMessage communityMessage = new CommunityMessage("CallOfDuty");
        akkaActorSystem.communityActor.tell(communityMessage, null);

    }

}
