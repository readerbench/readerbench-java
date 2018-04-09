/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.textualcomplexity.rhythm.indices;

import com.readerbench.data.AbstractDocument;
import com.readerbench.data.Sentence;
import com.readerbench.data.Word;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;
import com.readerbench.coreservices.rhythm.RhythmTool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author valentin.sergiu.cioaca@gmail.com
 */
public class CountAssonances extends ComplexityIndex {

    public CountAssonances() {
        super(ComplexityIndicesEnum.ASSONANCE);
    }

    @Override
    public double compute(AbstractDocument d) {
        List<List<String>> units = new ArrayList<>();
        for (Sentence s : d.getSentencesInDocument()) {
            List<String> unit = new ArrayList<>();
            for (Word w : s.getAllWords()) {
                unit.add(w.getText());
            }
            units.add(unit);
        }
        Map<Integer, List<List<String>>> assonances = RhythmTool.findAssonances(units);
        double counter = 0;
        for (Map.Entry<Integer, List<List<String>>> entry : assonances.entrySet()) {
            counter += entry.getValue().size();
        }
        return counter;
    }
    
}
