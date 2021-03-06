/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.textualcomplexity;

import com.readerbench.coreservices.commons.Clustering;
import com.readerbench.coreservices.commons.VectorAlgebra;
import com.readerbench.coreservices.data.AbstractDocument;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class ComplexityClustering extends Clustering {

    @Override
    public double compareDocs(AbstractDocument d1, AbstractDocument d2) {
        if (d1 == null || d2 == null || d1.getComplexityIndices() == null
                || d2.getComplexityIndices() == null) {
            return -1;
        }
        return VectorAlgebra.cosineSimilarity(ComplexityIndices.getComplexityIndicesArray(d1), ComplexityIndices.getComplexityIndicesArray(d2));
    }
}
