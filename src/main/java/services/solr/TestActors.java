package services.solr;

import akka.AkkaActorSystem;

/**
 * Created by Dorinela on 3/18/2017.
 */
public class TestActors {

    public static AkkaActorSystem akkaActorSystem;

    public static void main(String[] args) {

        akkaActorSystem = new AkkaActorSystem();
        akkaActorSystem.init();

    }

}
