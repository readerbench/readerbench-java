/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.parallelprocessingservice.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.dispatch.Futures;
import akka.dispatch.Mapper;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;
import akka.routing.RoundRobinPool;
import com.readerbench.coreservices.data.AbstractDocumentTemplate;
import com.readerbench.coreservices.data.document.Document;
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
public class AkkaMasterActor extends AbstractActor {
    
    private final List<Document> resultDocuments = Collections.synchronizedList(new ArrayList<>());
    
    public static Props props(int numberOfWorkers) {
        return Props.create(AkkaMasterActor.class, () -> new AkkaMasterActor()).withRouter(new RoundRobinPool(numberOfWorkers));
    }
    
    public Receive createReceive() {
        return receiveBuilder()
            .match(ProcessDocumentsInitMessage.class, message -> {
                List<AbstractDocumentTemplate> docs = message.getTemplates();
                ExecutionContextExecutor dispatcher = AkkaActorSystem.ACTOR_SYSTEM.dispatcher();

                ActorRef outsideCaller = getSender();
                
                List<Future<Object>> futureList = new ArrayList<>();
                for (AbstractDocumentTemplate doc : docs) {
                   
                    Future<Object> job = Patterns.ask(AkkaActorSystem.PROCESSING_WORKER,
                        new ProcessDocumentMessage(doc, message.getLang(), message.getModels(), message.getAnnotators()), AkkaActorSystem.TIMEOUT);
                    
                    futureList.add(job);
                }
                
                Future<Iterable<Object>> futureListOfDocuments = Futures.sequence(futureList, dispatcher);
                
                Future<Object> mapFuture = futureListOfDocuments.map(new Mapper<Iterable<Object>, Object>(){
                    public Object apply(Iterable<Object> documents) {
                        for (Object doc : documents) {
                            resultDocuments.add((Document)doc);
                        }
                        return resultDocuments;
                    }
                }, dispatcher);
                
                mapFuture.onComplete(new OnComplete<Object>() {
                    @Override
                    public void onComplete(Throwable throwable, Object obj) throws Throwable {
                        
                        outsideCaller.tell(resultDocuments, getSelf());
                    }
                }, dispatcher);
            })
            .build();
    }
}
