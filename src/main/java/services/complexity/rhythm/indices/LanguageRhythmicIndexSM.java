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
import services.complexity.ComplexityIndicesEnum;
import services.complexity.rhythm.tools.RhythmTool;

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
