/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.textualcomplexity.rhythm.indices;

import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.coreservices.data.Block;
import com.readerbench.coreservices.data.Sentence;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;

/**
 *
 * @author valentin.sergiu.cioaca@gmail.com
 */
public class AvgRhythmicUnits extends ComplexityIndex {

    public AvgRhythmicUnits() {
        super(ComplexityIndicesEnum.AVG_RHYTHMIC_UNITS);
    }

    @Override
    public double compute(AbstractDocument d) {
        int totalNoSentences = 0;
        int totalNoUnits = 0;
        
        for (Block b : d.getBlocks()) {
            if (null == b) {
                continue;
            }
            totalNoSentences += b.getSentences().size();
            for (Sentence s : b.getSentences()) {
                totalNoUnits += s.getText().split("[\\p{Punct}]+").length;
            }
        }
        return 1.0 * totalNoUnits / totalNoSentences;
    }
}
