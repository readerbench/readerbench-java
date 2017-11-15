/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.complexity.rhythm.indices;

import data.AbstractDocument;
import data.Block;
import data.Sentence;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndicesEnum;
import services.complexity.rhythm.tools.RhythmTool;

/**
 *
 * @author valentin.sergiu.cioaca@gmail.com
 */
public class LanguageRhythmicCoefficient extends ComplexityIndex {

    public LanguageRhythmicCoefficient() {
        super(ComplexityIndicesEnum.LANGUAGE_RHYTHM_COEFFICIENT);
    }

    @Override
    public double compute(AbstractDocument d) {
        Map<Integer, Integer> cntSyllables = new TreeMap<>();
        int deviations = 0;
        
        for (Block b : d.getBlocks()) {
            if (null == b) {
                continue;
            }
            for (Sentence s : b.getSentences()) {
//                System.out.println("Sentence: " + s.getText());
                String[] units = s.getText().split("[\\p{Punct}]+");
                for (String str : units) {
                    List<String> unit = Arrays.asList(str.trim().split("\\s+"));
//                    System.out.println(u + "    " + u.size());
                
//                    List<Word> unit = s.getAllWords();
//                    List<Integer> repr = RhythmTool.getNumericalRepresentation(unit);
                    List<Integer> repr = RhythmTool.testNewUnitDefinition(unit);
                    if (repr.isEmpty()) {
                        continue;
                    }
                    for (Integer nr : repr) {
                        if (nr == 0) continue;
                        cntSyllables.put(nr,
                        cntSyllables.containsKey(nr) ? cntSyllables.get(nr)+1 : 1);
                    }
                    deviations += RhythmTool.calcDeviations(repr);
//                    System.out.println("Deviations: " + deviations);
//                    System.out.println();
                }
            }
        }
//        DecimalFormat df = new DecimalFormat("#.##");
        int totalNumber = cntSyllables.values().stream().reduce(0, Integer::sum);
//        for (Map.Entry<Integer, Integer> entry : cntSyllables.entrySet()) {
//                double syllFreq = 1.0 * entry.getValue() / totalNumber;
//                System.out.println(entry.getKey() + "\t" + totalNumber +
//                                                    "\t" + entry.getValue() + 
//                                                    "\t" + df.format(syllFreq));
//        }
//        Integer keyOfMaxVal = Collections.max(cntSyllables.entrySet(), Map.Entry.comparingByValue()).getKey();
        int dominantInd = RhythmTool.getDominantIndex(cntSyllables.values().stream()
                .collect(Collectors.toList()));
//        System.out.println("Dominant ind: " + dominantInd);
        int keyOfMaxVal = cntSyllables.keySet().stream()
                .collect(Collectors.toList()).get(dominantInd);
//        System.out.println("Key of max val: " + keyOfMaxVal);
        int sum = cntSyllables.get(keyOfMaxVal);
        sum += (cntSyllables.containsKey(keyOfMaxVal-1)) ? cntSyllables.get(keyOfMaxVal-1) : 0;
        sum += (cntSyllables.containsKey(keyOfMaxVal+1)) ? cntSyllables.get(keyOfMaxVal+1) : 0;
        double coeff = 1.0 * (deviations + totalNumber - sum) / totalNumber;
//        System.out.println("Deviations: " + deviations);
//        System.out.println("Coefficient: " + df.format(coeff));
        
        return coeff;
    }
    
}
