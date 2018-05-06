/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.parallelprocessingservice.actors.cscl;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.dispatch.Futures;
import akka.dispatch.Mapper;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;
import akka.routing.RoundRobinPool;
import com.readerbench.coreservices.cscl.data.Conversation;
import com.readerbench.coreservices.data.AbstractDocumentTemplate;
import com.readerbench.parallelprocessingservice.messages.ProcessDocumentMessage;
import com.readerbench.parallelprocessingservice.messages.ProcessDocumentsInitMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import scala.concurrent.ExecutionContextExecutor;
import scala.concurrent.Future;

/**
 *
 * @author Dragos
 */
public class ConversationMasterActor extends AbstractActor {

    private final List<Conversation> conversations = Collections.synchronizedList(new ArrayList<>());

    public static Props props(int numberOfWorkers) {
        return Props.create(ConversationMasterActor.class, () -> new ConversationMasterActor()).withRouter(new RoundRobinPool(numberOfWorkers));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ProcessDocumentsInitMessage.class, message -> {
                    List<AbstractDocumentTemplate> docs = message.getTemplates();
                    ExecutionContextExecutor dispatcher = ConversationActorSystem.ACTOR_SYSTEM.dispatcher();

                    ActorRef outsideCaller = getSender();

                    List<Future<Object>> futureList = new ArrayList<>();
                    for (AbstractDocumentTemplate doc : docs) {
                        Future<Object> job = Patterns.ask(ConversationActorSystem.PROCESSING_WORKER,
                                new ProcessDocumentMessage(doc, message.getLang(), message.getModels(), message.getAnnotators()), ConversationActorSystem.TIMEOUT);

                        futureList.add(job);
                    }

                    Future<Iterable<Object>> futureListOfConversations = Futures.sequence(futureList, dispatcher);

                    Future<Object> mapFuture = futureListOfConversations.map(new Mapper<Iterable<Object>, Object>() {
                        @Override
                        public Object apply(Iterable<Object> documents) {
                            for (Object doc : documents) {
                                conversations.add((Conversation) doc);
                            }
                            return conversations;
                        }
                    }, dispatcher);

                    mapFuture.onComplete(new OnComplete<Object>() {
                        @Override
                        public void onComplete(Throwable throwable, Object obj) throws Throwable {
                            outsideCaller.tell(conversations, getSelf());
                        }
                    }, dispatcher);
                })
                .build();
    }
}
