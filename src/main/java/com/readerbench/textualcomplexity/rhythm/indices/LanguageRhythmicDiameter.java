/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.textualcomplexity.rhythm.indices;

import com.readerbench.datasourceprovider.data.AbstractDocument;
import com.readerbench.datasourceprovider.data.Block;
import com.readerbench.datasourceprovider.data.Sentence;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;
import com.readerbench.coreservices.rhythm.RhythmTool;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author valentin.sergiu.cioaca@gmail.com
 */
public class LanguageRhythmicDiameter extends ComplexityIndex {

    public LanguageRhythmicDiameter() {
        super(ComplexityIndicesEnum.LANGUAGE_RHYTHM_DIAMETER);
    }

    @Override
    public double compute(AbstractDocument d) {
        int infRhythmicLimit, supRhythmicLimit, rhythmicDiameter;
        
        infRhythmicLimit = Integer.MAX_VALUE;
        rhythmicDiameter = supRhythmicLimit = 0;
        for (Block b : d.getBlocks()) {
            if (null == b) {
                continue;
            }
            for (Sentence s : b.getSentences()) {
                List<Integer> rhythmicStructure = RhythmTool.getRhythmicStructureSM(s.getAllWords());
                if (rhythmicStructure.isEmpty()) {
                    continue;
                }
                int min = Collections.min(rhythmicStructure);
                int max = Collections.max(rhythmicStructure);
                infRhythmicLimit = Math.min(infRhythmicLimit, min);
                supRhythmicLimit = Math.max(supRhythmicLimit, max);
                rhythmicDiameter = Math.max(rhythmicDiameter, max - min);
            }
        }
        
        return rhythmicDiameter;
    }
    
}
