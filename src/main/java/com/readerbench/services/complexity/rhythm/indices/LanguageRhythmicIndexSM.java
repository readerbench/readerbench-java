/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.services.complexity.rhythm.indices;

import com.readerbench.data.AbstractDocument;
import com.readerbench.data.Block;
import com.readerbench.data.Sentence;
import com.readerbench.services.complexity.ComplexityIndex;
import com.readerbench.services.complexity.ComplexityIndicesEnum;
import com.readerbench.services.complexity.rhythm.tools.RhythmTool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author valentin.sergiu.cioaca@gmail.com
 */
public class LanguageRhythmicIndexSM extends ComplexityIndex {

    public LanguageRhythmicIndexSM() {
        super(ComplexityIndicesEnum.LANGUAGE_RHYTHM_INDEX);
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
//                System.out.println("Rhythmic index: " + unitRhythmicIndex);
//                System.out.println();
                if (unitRhythmicIndex != RhythmTool.UNDEFINED)
                    rhythmicIndices.add(unitRhythmicIndex);
            }
        }
//        Map<Integer, Long> counts = rhythmicIndices.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
//        System.out.println(counts);
        return Collections.max(rhythmicIndices);
    }
}
