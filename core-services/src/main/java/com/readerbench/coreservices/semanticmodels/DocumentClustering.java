/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.coreservices.semanticmodels;

import com.readerbench.coreservices.commons.Clustering;
import com.readerbench.coreservices.data.AbstractDocument;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class DocumentClustering extends Clustering {

    private final SemanticModel model;

    public DocumentClustering(SemanticModel model) {
        this.model = model;
    }

    @Override
    public double compareDocs(AbstractDocument d1, AbstractDocument d2) {
        return model.getSimilarity(d1, d2);
    }
}
