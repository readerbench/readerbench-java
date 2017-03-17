package akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actors.CommunityActor;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Dorinela on 3/17/2017.
 */
public class AkkaActorSystem {

    private static final Logger LOGGER = LoggerFactory.getLogger(AkkaActorSystem.class);

    public static ActorSystem actorSystem;

    public ActorRef communityActor;

    /**
     * Actors' initialization
     */
    public void init() {
        LOGGER.info("Create Akka Actor System ...");
        actorSystem = ActorSystem.create("readerBenchActorSystem", ConfigFactory.load("akka.conf"));

        LOGGER.info("Init actors ...");
        communityActor = this.actorSystem.actorOf(Props.create(CommunityActor.class), "community-actor");
        //TODO
    }

    /**
     * Stop Akka Actor System
     */
    public static void stop() {
        actorSystem.shutdown();
        actorSystem.awaitTermination();
        actorSystem = null;

        LOGGER.info("Akka Actor System shutdown ...");
    }

}
