/*
 * Copyright 2016 ReaderBench.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.readerbench.services.complexity.dialogism;

import com.readerbench.data.AbstractDocument;
import com.readerbench.data.discourse.SemanticChain;
import com.readerbench.services.complexity.ComplexityIndex;
import com.readerbench.services.complexity.ComplexityIndices;
import com.readerbench.services.complexity.ComplexityIndicesEnum;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * @author Stefan Ruseti
 */
public class DialogismSynergyIndex extends ComplexityIndex {

    private transient final Function<Stream<? extends Number>, Double> combine;
    private transient final Function<SemanticChain, double[]> listFunction;
    private transient final Function<Double, Double> mapper;

    public DialogismSynergyIndex(ComplexityIndicesEnum index,
            Function<SemanticChain, double[]> listFunction,
            Function<Stream<? extends Number>, Double> combine,
            Function<Double, Double> mapper) {
        super(index);
        this.listFunction = listFunction;
        this.combine = combine;
        this.mapper = mapper;
    }

    public DialogismSynergyIndex(ComplexityIndicesEnum index, Function<SemanticChain, double[]> listFunction, Function<Stream<? extends Number>, Double> combine) {
        this(index, listFunction, combine, Function.identity());
    }

    @Override
    public double compute(AbstractDocument d) {
        List<SemanticChain> voices = d.getVoices();
        if (voices == null || voices.isEmpty()) {
            return ComplexityIndices.IDENTITY;
        }
        return combine.apply(IntStream.range(0, listFunction.apply(voices.get(0)).length)
                .mapToObj(k -> voices.stream()
                        .mapToDouble(v -> mapper.apply(listFunction.apply(v)[k]))
                        .sum()));
    }

}
