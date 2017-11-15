/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.complexity.rhythm.indices;

import data.AbstractDocument;
import data.Sentence;
import data.Syllable;
import data.Word;
import java.util.ArrayList;
import java.util.List;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndices;
import services.complexity.ComplexityIndicesEnum;

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
                    cnt += services.complexity.readability.Syllable.syllable(w.getText());
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
