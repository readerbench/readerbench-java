package akka;

import akka.messages.CommunityMessage;
import webService.ReaderBenchServer;

/**
 * Created by Dorinela on 5/13/2017.
 */
public class CommunityProcessing {

    public static void main(String[] args) {
        /**
         * initialize ReaderBench DB
         */
        ReaderBenchServer.initializeDB();

        /**
         * init akka actor system
         */
        AkkaActorSystem.init();

        CommunityMessage communityMessage = new CommunityMessage("leagueoflegends");
        AkkaActorSystem.communityActor.tell(communityMessage, null);

    }
}
