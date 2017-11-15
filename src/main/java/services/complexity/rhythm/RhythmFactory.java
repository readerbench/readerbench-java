/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.complexity.rhythm;

import services.complexity.rhythm.indices.LanguageRhythmicIndexSM;
import services.complexity.rhythm.indices.LanguageRhythmicDiameter;
import services.complexity.rhythm.indices.LanguageRhythmicCoefficient;
import services.complexity.rhythm.indices.FrequencyOfRhythmicIndex;
import services.complexity.rhythm.indices.AvgSyllablesInSentence;
import services.complexity.rhythm.indices.AvgStressedSyllablesInSentence;
import data.Lang;
import java.util.ArrayList;
import java.util.List;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndicesFactory;
import services.complexity.rhythm.indices.AvgRhythmicUnits;
import services.complexity.rhythm.indices.AvgStressedSyllablesInRhythmicUnit;
import services.complexity.rhythm.indices.AvgSyllablesInRhythmicUnit;
import services.complexity.rhythm.indices.CountAlliterations;
import services.complexity.rhythm.indices.CountAssonances;

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
