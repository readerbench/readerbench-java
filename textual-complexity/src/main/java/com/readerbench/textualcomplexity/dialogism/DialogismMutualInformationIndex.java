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
package com.readerbench.textualcomplexity.dialogism;

import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.coreservices.data.discourse.SemanticChain;
import com.readerbench.coreservices.commons.VectorAlgebra;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndices;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 *
 * @author Stefan Ruseti
 */
public class DialogismMutualInformationIndex extends ComplexityIndex {

    private transient final Function<Stream<? extends Number>, Double> combine;
    private transient final Function<SemanticChain, double[]> listFunction;

    public DialogismMutualInformationIndex(ComplexityIndicesEnum index,
            Function<SemanticChain, double[]> listFunction,
            Function<Stream<? extends Number>, Double> combine) {
        super(index);
        this.listFunction = listFunction;
        this.combine = combine;
    }

    @Override
    public double compute(AbstractDocument d) {
        List<SemanticChain> voices = d.getVoices();
        if (voices == null || voices.isEmpty()) {
            return ComplexityIndices.IDENTITY;
        }

        int no = 0;
        double[] evolution = new double[listFunction.apply(voices.get(0)).length];
        for (int i = 1; i < voices.size(); i++) {
            for (int j = 0; j < i; j++) {
                double[] mi = VectorAlgebra.discreteMutualInformation(
                        listFunction.apply(voices.get(i)),
                        listFunction.apply(voices.get(j)));
                for (int k = 0; k < evolution.length; k++) {
                    evolution[k] += mi[k];
                }
                no++;
            }
        }
        if (no > 0) {
            final int factor = no;
            return combine.apply(Arrays.stream(evolution)
                    .mapToObj(x -> x / factor));
        }
        return ComplexityIndices.IDENTITY;
    }

}
