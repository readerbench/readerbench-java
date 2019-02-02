/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.parallelprocessingservice.actors.cscl;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.ConfigFactory;

/**
 *
 * @author Dragos
 */
public class ConversationActorSystem {

    public static final Integer NUMBER_OF_WORKERS_ACTORS = 4;
    public static final Integer NUMBER_OF_MASTER_ACTORS = 1;

    public static final ActorSystem ACTOR_SYSTEM = ActorSystem.create("ActorSystem", ConfigFactory.load("akka.conf"));
    public static final ActorRef PROCESSING_WORKER = ACTOR_SYSTEM.actorOf(ConversationWorkerActor.props(NUMBER_OF_WORKERS_ACTORS), "convert-document-worker");
    public static final ActorRef PROCESSING_MASTER = ACTOR_SYSTEM.actorOf(ConversationMasterActor.props(NUMBER_OF_MASTER_ACTORS), "convert-document-master");

    public static final long TIMEOUT = 20000000;
}
