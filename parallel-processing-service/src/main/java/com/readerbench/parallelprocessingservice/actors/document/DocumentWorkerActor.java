/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.parallelprocessingservice.actors.document;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.routing.RoundRobinPool;
import com.readerbench.coreservices.data.AbstractDocumentTemplate;
import com.readerbench.coreservices.data.document.Document;
import com.readerbench.parallelprocessingservice.messages.ProcessDocumentMessage;
import com.readerbench.processingservice.document.DocumentProcessingPipeline;

/**
 *
 * @author Dragos
 */
public class DocumentWorkerActor extends AbstractActor {

    public static Props props(int numberOfActors) {
        return Props.create(DocumentWorkerActor.class,
                () -> new DocumentWorkerActor()).withRouter(new RoundRobinPool(numberOfActors));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ProcessDocumentMessage.class, message -> {
                    AbstractDocumentTemplate template = message.getTemplate();
                    DocumentProcessingPipeline pipeline = new DocumentProcessingPipeline(message.getLang(), message.getModels(), message.getAnnotators());
                    Document document = pipeline.createDocumentFromTemplate(template);
                    pipeline.processDocument(document);

                    getSender().tell(document, getSelf());
                })
                .build();
    }
}
