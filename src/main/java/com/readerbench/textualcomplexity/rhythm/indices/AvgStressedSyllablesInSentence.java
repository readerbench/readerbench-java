/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.textualcomplexity.rhythm.indices;

import com.readerbench.datasourceprovider.data.AbstractDocument;
import com.readerbench.datasourceprovider.data.Sentence;
import com.readerbench.datasourceprovider.data.Syllable;
import com.readerbench.datasourceprovider.data.Word;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndices;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;
import com.readerbench.coreservices.nlp.wordlists.StopWords;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author valentin.sergiu.cioaca@gmail.com
 */
public class AvgStressedSyllablesInSentence extends ComplexityIndex {

    public AvgStressedSyllablesInSentence() {
        super(ComplexityIndicesEnum.AVG_STRESSED_SYLLABLES_SEN);
    }

    @Override
    public double compute(AbstractDocument d) {
        List<Integer> nrOfStressedSyllables = new ArrayList<>();
        
        for (Sentence s : d.getSentencesInDocument()) {
//            System.out.println("Sentence: " + s);
            int cntStessedSyll = 0;
            for (Word w : s.getAllWords()) {
                if (StopWords.isStopWord(w.getText().toLowerCase(), Lang.en)) {
                    continue;
                }
                List<Syllable> syllables = w.getSyllables();
//                System.out.print(syllables + " ");
                if (syllables == null) continue;
                for (Syllable syll : syllables) {
                    if (syll.isPrimaryStressed())
                        cntStessedSyll++;
                }
            }
//            System.out.println();
//            System.out.println("Nr stressed syllables: " + cntStessedSyll);
            nrOfStressedSyllables.add(cntStessedSyll);
        }
        
//        System.out.println(nrOfStressedSyllables);
        double avg = nrOfStressedSyllables.parallelStream()
                .mapToInt(a -> a)
                .average().orElse(ComplexityIndices.IDENTITY);
//        System.out.println("Avg: " + avg);
        return avg;
    }
    
}
