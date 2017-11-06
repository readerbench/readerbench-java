/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.complexity.rhythm.indices;

import data.AbstractDocument;
import data.Sentence;
import data.Word;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndicesEnum;
import services.complexity.rhythm.tools.RhythmTool;

/**
 *
 * @author valentin.sergiu.cioaca@gmail.com
 */
public class CountAlliterations extends ComplexityIndex {

    public CountAlliterations() {
        super(ComplexityIndicesEnum.ALLITERATION);
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
        Map<Integer, List<List<String>>> alliterations = RhythmTool.findAlliterations(units);
        double counter = 0;
        for (Map.Entry<Integer, List<List<String>>> entry : alliterations.entrySet()) {
            counter += entry.getValue().size();
        }
        return counter;
    }
    
}
