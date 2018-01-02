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
package com.readerbench.services.complexity.cohesion.lexicalChains;

import com.readerbench.data.AbstractDocument;
import com.readerbench.services.complexity.ComplexityIndex;
import com.readerbench.services.complexity.ComplexityIndices;
import com.readerbench.services.complexity.ComplexityIndicesEnum;
import com.readerbench.services.discourse.dialogism.DialogismComputations;

/**
 *
 * @author Stefan Ruseti
 */
public class LexicalChainsCoverage extends ComplexityIndex {

    public LexicalChainsCoverage() {
        super(ComplexityIndicesEnum.PERCENTAGE_LEXICAL_CHAINS_COVERAGE);
    }

    @Override
    public double compute(AbstractDocument d) {
        long noCoveredWords = d.getLexicalChains().parallelStream()
                .filter(c -> c.getLinks().size() >= DialogismComputations.SEMANTIC_CHAIN_MIN_NO_WORDS)
                .count();
        int noWords = d.getLexicalChains().parallelStream()
                .mapToInt(c -> c.getLinks().size())
                .sum();
        if (noWords != 0) {
            return ((double) noCoveredWords) / noWords;
        }
        return ComplexityIndices.IDENTITY;
    }

}
