/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.textualcomplexity.rhythm.indices;

import com.readerbench.data.AbstractDocument;
import com.readerbench.data.Block;
import com.readerbench.data.Sentence;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndices;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;
import com.readerbench.coreservices.rhythm.RhythmTool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author valentin.sergiu.cioaca@gmail.com
 */
public class FrequencyOfRhythmicIndex extends ComplexityIndex {

    public FrequencyOfRhythmicIndex() {
        super(ComplexityIndicesEnum.FREQ_RHYTHM_INDEX);
    }

    @Override
    public double compute(AbstractDocument d) {
        List<Integer> rhythmicIndices = new ArrayList<>();
        
        for (Block b : d.getBlocks()) {
            if (null == b) {
                continue;
            }
            for (Sentence s : b.getSentences()) {
                int unitRhythmicIndex = RhythmTool.calcRhythmicIndexSM(s.getAllWords());
                if (unitRhythmicIndex != RhythmTool.UNDEFINED)
                    rhythmicIndices.add(unitRhythmicIndex);
            }
        }
        
        if (rhythmicIndices.isEmpty()) {
            return ComplexityIndices.IDENTITY;
        }
        
        int maxInd = Collections.max(rhythmicIndices);
        return 1.0 * Collections.frequency(rhythmicIndices, maxInd) / rhythmicIndices.size();
    }
}
