package akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actors.CommunityActor;
import akka.actors.ConversationActor;
import akka.actors.DialogProcessingActor;
import akka.actors.SolrDataProcessingActor;
import akka.routing.RoundRobinPool;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Dorinela on 3/17/2017.
 */
public class AkkaActorSystem {

    private static final Logger LOGGER = LoggerFactory.getLogger(AkkaActorSystem.class);

    public static ActorSystem ACTOR_SYSTEM;

    public ActorRef communityActor;
    public ActorRef solrDataProcessingActor;
    public ActorRef dialogProcessingActor;
    public ActorRef conversationActor;

    /**
     * Actors' initialization
     */
    public void init() {
        LOGGER.info("Create Akka Actor System ...");
        ACTOR_SYSTEM = ActorSystem.create("readerBenchActorSystem", ConfigFactory.load("akka.conf"));

        LOGGER.info("Init actors ...");
        communityActor = this.ACTOR_SYSTEM.actorOf(Props.create(CommunityActor.class), "community-actor");
//        solrDataProcessingActor = this.ACTOR_SYSTEM.actorOf(Props.create(SolrDataProcessingActor.class),
//                "solr-data-processing-actor");
//        dialogProcessingActor = this.ACTOR_SYSTEM.actorOf(Props.create(DialogProcessingActor.class)
//                        .withDispatcher("dialog-processing-dispatcher"), "dialog-processing-actor");

        conversationActor = this.ACTOR_SYSTEM.actorOf(new RoundRobinPool(1).props(Props.create(ConversationActor.class)),
                "conversation-actor");

        //TODO - add other actors
    }

    /**
     * Stop Akka Actor System
     */
    public static void stop() {
        ACTOR_SYSTEM.shutdown();
        ACTOR_SYSTEM.awaitTermination();
        ACTOR_SYSTEM = null;

        LOGGER.info("Akka Actor System shutdown ...");
    }

}
