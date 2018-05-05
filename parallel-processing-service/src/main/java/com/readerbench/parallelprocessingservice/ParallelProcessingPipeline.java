/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.parallelprocessingservice;

import akka.pattern.Patterns;
import com.readerbench.coreservices.data.AbstractDocumentTemplate;
import com.readerbench.coreservices.data.document.Document;
import com.readerbench.coreservices.semanticmodels.data.ISemanticModel;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.parallelprocessingservice.actors.AkkaActorSystem;
import com.readerbench.parallelprocessingservice.messages.ProcessDocumentsInitMessage;
import com.readerbench.processingservice.Annotators;
import com.readerbench.processingservice.document.DocumentProcessingPipeline;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

/**
 *
 * @author Dragos
 */
public class ParallelProcessingPipeline {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ParallelProcessingPipeline.class);
    
    public List<Document> loadXMLsFromDirectory(String directoryPath, Lang lang,
                                                List<ISemanticModel> models, List<Annotators> annotators) {
        DocumentProcessingPipeline normalPipeline = new DocumentProcessingPipeline(lang, models, annotators);
        List<AbstractDocumentTemplate> templates = new ArrayList<>();
        
        File folder = new File(directoryPath);
        File [] listOfFiles = folder.listFiles();
        
        for (int i = 0; i < listOfFiles.length; ++i) {
            if (listOfFiles[i].isFile()) {
                AbstractDocumentTemplate template = normalPipeline.extractDocTemplateFromXML(
                        directoryPath + "/" + listOfFiles[i].getName());
                templates.add(template);
            }
        }
        
        ProcessDocumentsInitMessage initMsg = new ProcessDocumentsInitMessage(templates, lang, models, annotators);

        Future<Object> future = Patterns.ask(AkkaActorSystem.PROCESSING_MASTER, initMsg, AkkaActorSystem.TIMEOUT * 100);
        
        List<Document> listOfProcessedDocuments = null;
        try {
            Object result = Await.result(future, Duration.Inf());
            listOfProcessedDocuments = (List<Document>) result;
        }
        catch (Exception e) {
            LOGGER.error("Error in processing the document in a parallel manner. Error message: " + e.getMessage());
        }
        
        AkkaActorSystem.ACTOR_SYSTEM.terminate();
        
        return listOfProcessedDocuments;
    }
    
}
