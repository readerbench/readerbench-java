/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.complexity.wordComplexity;

import data.AbstractDocument;
import data.AnalysisElement;
import java.util.function.Function;
import java.util.stream.Stream;
import services.complexity.AbstractComplexityIndex;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndicesEnum;
import utils.IndexLevel;

/**
 *
 * @author stefan
 */
public class AvgIDFIndex extends AbstractComplexityIndex {

    public AvgIDFIndex(ComplexityIndicesEnum index, IndexLevel level) {
        super(index, level);
    }

    @Override
    public double compute(AbstractDocument d) {
        return streamFunction.apply(d)
                .mapToDouble(b -> b.getWordOccurences().entrySet().stream()
                    .mapToDouble(e -> e.getKey().getIdf() * e.getValue())
                    .sum())
                .average().orElse(0);
    }

}
