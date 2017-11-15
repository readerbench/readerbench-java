/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.complexity.rhythm.indices;

import data.AbstractDocument;
import data.Lang;
import data.Sentence;
import data.Syllable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndices;
import services.complexity.ComplexityIndicesEnum;
import services.complexity.rhythm.tools.SyllabifiedCMUDict;
import services.nlp.listOfWords.StopWords;

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
