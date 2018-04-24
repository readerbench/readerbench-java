/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.textualcomplexity.readability;

import com.readerbench.datasourceprovider.data.AbstractDocument;
import com.readerbench.textualcomplexity.ComplexityIndices;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;

/**
 *
 * @author Stefan
 */
public class ReadabilityFlesch extends ReadabilityIndex {

    public ReadabilityFlesch() {
        super(ComplexityIndicesEnum.READABILITY_FLESCH);
    }

    @Override
    public double compute(AbstractDocument d) {
        if (d.getText() == null || d.getText().length() == 0) {
            return ComplexityIndices.IDENTITY;
        }
        Fathom.Stats stats = Fathom.analyze(d.getText());
        return calcFlesch(stats);
    }

}
