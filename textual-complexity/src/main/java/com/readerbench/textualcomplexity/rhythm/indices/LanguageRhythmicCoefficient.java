/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.textualcomplexity.rhythm.indices;

import com.readerbench.data.AbstractDocument;
import com.readerbench.data.Block;
import com.readerbench.data.Sentence;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndices;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;
import com.readerbench.textualcomplexity.rhythm.tools.RhythmTool;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

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
        if (d.getBlocks().isEmpty()) {
            return ComplexityIndices.IDENTITY;
        }
        Map<Integer, Integer> cntSyllables = new TreeMap<>();
        int deviations = 0;

        for (Block b : d.getBlocks()) {
            if (null == b) {
                continue;
            }
            for (Sentence s : b.getSentences()) {
                String[] units = s.getText().split("[\\p{Punct}]+");
                for (String str : units) {
                    List<String> unit = Arrays.asList(str.trim().split("\\s+"));
                    List<Integer> repr = RhythmTool.testNewUnitDefinition(unit);
                    if (repr.isEmpty()) {
                        continue;
                    }
                    repr.stream().filter((nr) -> !(nr == 0)).forEachOrdered((nr) -> {
                        cntSyllables.put(nr, cntSyllables.containsKey(nr) ? cntSyllables.get(nr) + 1 : 1);
                    });
                    deviations += RhythmTool.calcDeviations(repr);
                }
            }
        }
        int totalNumber = cntSyllables.values().stream().reduce(0, Integer::sum);
        if (totalNumber == 0) {
            return ComplexityIndices.IDENTITY;
        }
        int dominantInd = RhythmTool.getDominantIndex(cntSyllables.values().stream()
                .collect(Collectors.toList()));
        if (dominantInd == -1) {
            return ComplexityIndices.IDENTITY;
        }
        int keyOfMaxVal = cntSyllables.keySet().stream()
                .collect(Collectors.toList()).get(dominantInd);
        int sum = cntSyllables.get(keyOfMaxVal);
        sum += (cntSyllables.containsKey(keyOfMaxVal - 1)) ? cntSyllables.get(keyOfMaxVal - 1) : 0;
        sum += (cntSyllables.containsKey(keyOfMaxVal + 1)) ? cntSyllables.get(keyOfMaxVal + 1) : 0;
        double coeff = 1.0 * (deviations + totalNumber - sum) / totalNumber;

        return coeff;
    }

}
