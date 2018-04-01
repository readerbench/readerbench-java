/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.semanticModels;

import data.AbstractDocument;
import services.commons.Clustering;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class DocumentClustering extends Clustering {

    private final ISemanticModel model;

    public DocumentClustering(ISemanticModel model) {
        this.model = model;
    }

    @Override
    public double compareDocs(AbstractDocument d1, AbstractDocument d2) {
        return model.getSimilarity(d1, d2);
    }
}
