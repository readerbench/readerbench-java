package akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actors.CommunityActor;
import akka.actors.ConversationActor;
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

    public static ActorRef communityActor;
    public static ActorRef conversationActor;

    /**
     * Actors' initialization
     */
    public static void init() {
        LOGGER.info("Create Akka Actor System ...");
        ACTOR_SYSTEM = ActorSystem.create("readerBenchActorSystem", ConfigFactory.load("akka.conf"));

        LOGGER.info("Init actors ...");
        communityActor = ACTOR_SYSTEM.actorOf(Props.create(CommunityActor.class), "community-actor");
        conversationActor = ACTOR_SYSTEM.actorOf(new RoundRobinPool(1).props(Props.create(ConversationActor.class)),
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
