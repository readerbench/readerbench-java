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
public class ProcessDocumentMessage {
    
    private AbstractDocumentTemplate template;
    private final Lang lang;
    private final List<SemanticModel> models;
    private final List<Annotators> annotators;

    public ProcessDocumentMessage(AbstractDocumentTemplate template, Lang lang, List<SemanticModel> models, List<Annotators> annotators) {
        this.template = template;
        this.lang = lang;
        this.models = models;
        this.annotators = annotators;
    }

    public AbstractDocumentTemplate getTemplate() {
        return template;
    }

    public void setTemplate(AbstractDocumentTemplate template) {
        this.template = template;
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
