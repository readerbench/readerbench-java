/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.parallelprocessingservice.actors.document;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.ConfigFactory;

/**
 *
 * @author Dragos
 */
public class DocumentActorSystem {
    public static final Integer NUMBER_OF_WORKERS_ACTORS = 4;
    public static final Integer NUMBER_OF_MASTER_ACTORS = 1;

    public static final ActorSystem ACTOR_SYSTEM = ActorSystem.create("ActorSystem", ConfigFactory.load("akka.conf"));
    public static final ActorRef PROCESSING_WORKER = ACTOR_SYSTEM.actorOf(DocumentWorkerActor.props(NUMBER_OF_WORKERS_ACTORS), "convert-document-worker");
    public static final ActorRef PROCESSING_MASTER = ACTOR_SYSTEM.actorOf(DocumentMasterActor.props(NUMBER_OF_MASTER_ACTORS), "convert-document-master");

    public static final long TIMEOUT = 1000 * 1000l;
}
