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

import com.readerbench.data.AbstractDocument;
import com.readerbench.readerbenchcore.data.discourse.SemanticChain;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndices;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;

import java.util.function.Function;

/**
 *
 * @author Stefan Ruseti
 */
public class DialogismIndex extends ComplexityIndex {

    private transient final Function<SemanticChain, Double> mapper;

    public DialogismIndex(ComplexityIndicesEnum index, Function<SemanticChain, Double> mapper) {
        super(index);
        this.mapper = mapper;
    }

    @Override
    public double compute(AbstractDocument d) {
        if (d.getVoices() == null) {
            return ComplexityIndices.IDENTITY;
        }
        return d.getVoices().parallelStream()
                .mapToDouble(mapper::apply)
                .average().orElse(ComplexityIndices.IDENTITY);
    }

}
