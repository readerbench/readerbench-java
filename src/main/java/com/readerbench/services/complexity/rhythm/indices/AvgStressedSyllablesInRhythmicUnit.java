/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.services.complexity.rhythm.indices;

import com.readerbench.data.AbstractDocument;
import com.readerbench.data.Lang;
import com.readerbench.data.Sentence;
import com.readerbench.data.Syllable;
import com.readerbench.services.complexity.ComplexityIndex;
import com.readerbench.services.complexity.ComplexityIndices;
import com.readerbench.services.complexity.ComplexityIndicesEnum;
import com.readerbench.services.complexity.rhythm.tools.SyllabifiedCMUDict;
import com.readerbench.services.nlp.listOfWords.StopWords;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author valentin.sergiu.cioaca@gmail.com
 */
public class AvgStressedSyllablesInRhythmicUnit extends ComplexityIndex {

    public AvgStressedSyllablesInRhythmicUnit() {
        super(ComplexityIndicesEnum.AVG_STRESSED_SYLLABLES_RHYTHMIC_UNIT);
    }

    @Override
    public double compute(AbstractDocument d) {
        List<Integer> nrOfStressedSyllables = new ArrayList<>();
        for (Sentence s : d.getSentencesInDocument()) {
            String[] units = s.getText().split("[\\p{Punct}]+");
            for (String str : units) {
                int cntStessedSyll = 0;
                List<String> unit = Arrays.asList(str.trim().split("\\s+"));
                for (String w : unit) {
                    if (StopWords.isStopWord(w.toLowerCase(), Lang.en)) {
                        continue;
                    }
                    List<Syllable> syllables = SyllabifiedCMUDict.getInstance()
                            .getDict().get(w.toLowerCase());
                    if (syllables == null) continue;
                    cntStessedSyll++;
                }
                nrOfStressedSyllables.add(cntStessedSyll);
            }
        }
        double avg = nrOfStressedSyllables.parallelStream()
                .mapToInt(a -> a)
                .average().orElse(ComplexityIndices.IDENTITY);
        return avg;
    }
    
}
