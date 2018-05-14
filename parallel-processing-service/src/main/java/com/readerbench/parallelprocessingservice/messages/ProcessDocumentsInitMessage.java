/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.parallelprocessingservice.messages;


import com.readerbench.coreservices.data.AbstractDocumentTemplate;
import com.readerbench.coreservices.semanticmodels.SemanticModel;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.processingservice.Annotators;
import java.util.List;

/**
 *
 * @author Dragos
 */
public class ProcessDocumentsInitMessage {
    private List<AbstractDocumentTemplate> templates;
    private final Lang lang;
    private final List<SemanticModel> models;
    private final List<Annotators> annotators;

    public ProcessDocumentsInitMessage(List<AbstractDocumentTemplate> templates, Lang lang, List<SemanticModel> models, List<Annotators> annotators) {
        this.templates = templates;
        this.lang = lang;
        this.models = models;
        this.annotators = annotators;
    }

    public List<AbstractDocumentTemplate> getTemplates() {
        return templates;
    }

    public void setTemplates(List<AbstractDocumentTemplate> templates) {
        this.templates = templates;
    }

    public Lang getLang() {
        return lang;
    }

    public List<SemanticModel> getModels() {
        return models;
    }

    public List<Annotators> getAnnotators() {
        return annotators;
    }
    
}
