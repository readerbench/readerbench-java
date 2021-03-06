/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.textualcomplexity.dependencies;

import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;

import java.util.Objects;
import java.util.stream.StreamSupport;

/**
 *
 * @author stefan
 */
public class AvgDependenciesPerSentence extends ComplexityIndex{

    public AvgDependenciesPerSentence(ComplexityIndicesEnum index, String dep) {
        super(index, dep);
    }

    @Override
    public double compute(AbstractDocument d) {
        return d.getBlocks().stream()
            .filter(Objects::nonNull)
            .flatMap(b -> b.getSentences().stream())
            .filter(s -> s.getDependencies() != null)
            .mapToDouble(s -> s.getDependencies().stream()
                    .map(triple -> triple.getRight())
                    .filter(dep -> dep.equals(param))
                    .count())
            .average().orElse(0);
    }
}
