/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.parallelprocessingservice.actors.cscl;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.routing.RoundRobinPool;
import com.readerbench.coreservices.cscl.data.Conversation;
import com.readerbench.coreservices.data.AbstractDocumentTemplate;
import com.readerbench.parallelprocessingservice.messages.ProcessDocumentMessage;
import com.readerbench.processingservice.cscl.ConversationProcessingPipeline;

/**
 *
 * @author Dragos
 */
public class ConversationWorkerActor extends AbstractActor {

    public static Props props(int numberOfActors) {
        return Props.create(ConversationWorkerActor.class,
                () -> new ConversationWorkerActor()).withRouter(new RoundRobinPool(numberOfActors));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ProcessDocumentMessage.class, message -> {
                    AbstractDocumentTemplate template = message.getTemplate();
                    ConversationProcessingPipeline pipeline = new ConversationProcessingPipeline(message.getLang(), message.getModels(), message.getAnnotators());
                    Conversation conversation = pipeline.createConversationFromTemplate(template);
                    pipeline.processDocument(conversation);

                    getSender().tell(conversation, getSelf());
                })
                .build();
    }
}
