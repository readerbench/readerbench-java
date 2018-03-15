/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.textualcomplexity.rhythm.indices;

import com.readerbench.data.AbstractDocument;
import com.readerbench.data.Sentence;
import com.readerbench.data.Syllable;
import com.readerbench.data.Word;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndices;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author valentin.sergiu.cioaca@gmail.com
 */
public class AvgSyllablesInSentence extends ComplexityIndex {

    public AvgSyllablesInSentence() {
        super(ComplexityIndicesEnum.AVG_SYLLABLES_SEN);
    }

    @Override
    public double compute(AbstractDocument d) {
        List<Integer> countSyllables = new ArrayList<>();
        
        for (Sentence s : d.getSentencesInDocument()) {
//            System.out.println("Sentence: " + s);
            int cnt = 0;
            for (Word w : s.getAllWords()) {
                List<Syllable> syllables = w.getSyllables();
//                System.out.print(syllables + " ");
                if (syllables == null) {
                    cnt += com.readerbench.textualcomplexity.readability.Syllable.syllable(w.getText());
                } else {
                    cnt += syllables.size();
                }
            }
//            System.out.println();
//            System.out.println("Nr syllables: " + cnt);
            countSyllables.add(cnt);
        }
        
//        System.out.println(countSyllables);
        double avg = countSyllables.parallelStream()
                .mapToInt(a -> a)
                .average().orElse(ComplexityIndices.IDENTITY);
//        System.out.println("Avg: " + avg);
        return avg;
    }
}
