/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.complexity.rhythm.indices;

import data.AbstractDocument;
import data.Block;
import data.Sentence;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndices;
import services.complexity.ComplexityIndicesEnum;
import services.complexity.rhythm.tools.RhythmTool;

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
