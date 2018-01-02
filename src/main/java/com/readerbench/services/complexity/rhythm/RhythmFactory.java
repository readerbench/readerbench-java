/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.services.complexity.rhythm;

import com.readerbench.data.Lang;
import com.readerbench.services.complexity.ComplexityIndex;
import com.readerbench.services.complexity.ComplexityIndicesFactory;
import com.readerbench.services.complexity.rhythm.indices.*;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author valentin.sergiu.cioaca@gmail.com
 */
public class RhythmFactory extends ComplexityIndicesFactory {

    @Override
    public List<ComplexityIndex> build(Lang lang) {
        List<ComplexityIndex> result = new ArrayList<>();
        if (lang != Lang.en) return result;
        result.add(new AvgSyllablesInSentence());
        result.add(new AvgStressedSyllablesInSentence());
        result.add(new AvgRhythmicUnits());
        result.add(new AvgSyllablesInRhythmicUnit());
        result.add(new AvgStressedSyllablesInRhythmicUnit());
        result.add(new LanguageRhythmicCoefficient());
        result.add(new LanguageRhythmicIndexSM());
        result.add(new FrequencyOfRhythmicIndex());
        result.add(new LanguageRhythmicDiameter());
        result.add(new CountAlliterations());
        result.add(new CountAssonances());
        return result;
    }
    
}
