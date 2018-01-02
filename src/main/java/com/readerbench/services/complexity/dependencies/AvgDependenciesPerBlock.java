/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.services.complexity.dependencies;

import com.readerbench.data.AbstractDocument;
import com.readerbench.services.complexity.ComplexityIndex;
import com.readerbench.services.complexity.ComplexityIndicesEnum;

import java.util.Objects;
import java.util.stream.StreamSupport;

/**
 *
 * @author stefan
 */
public class AvgDependenciesPerBlock extends ComplexityIndex{

    public AvgDependenciesPerBlock(ComplexityIndicesEnum index, String dep) {
        super(index, dep);
    }

    @Override
    public double compute(AbstractDocument d) {
        return d.getBlocks().stream()
            .filter(Objects::nonNull)
            .mapToDouble(b -> b.getSentences().stream()
                    .filter(s -> s.getDependencies() != null)
                    .flatMap(s -> StreamSupport.stream(
                        s.getDependencies().edgeIterable().spliterator(), false))
                    .map(edge -> edge.getRelation().getShortName())
                    .filter(dep -> dep.equals(param))
                    .count())
            .average().orElse(0);
    }
}