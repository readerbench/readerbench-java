/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.complexity.readability;

import data.AbstractDocument;
import services.complexity.ComplexityIndicesEnum;
import services.complexity.ComplexityIndices;

/**
 *
 * @author Stefan
 */
public class ReadabilityFlesch extends ReadabilityIndex{

    public ReadabilityFlesch() {
        super(ComplexityIndicesEnum.READABILITY_FLESCH);
    }

    @Override
    public double compute(AbstractDocument d) {
        Fathom.Stats stats = Fathom.analyze(d.getProcessedText());
        return calcFlesch(stats);
    }
    
}
