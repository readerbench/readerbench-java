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
package com.readerbench.services.complexity.cohesion.lexical;

import com.readerbench.data.AbstractDocument;
import com.readerbench.services.complexity.ComplexityIndicesEnum;
import com.readerbench.services.semanticModels.SimilarityType;

/**
 *
 * @author Stefan Ruseti
 */
public class AvgLexicalBlockCohesionAdjacentSentences extends LexicalCohesion{

    public AvgLexicalBlockCohesionAdjacentSentences(SimilarityType simType) {
        super(ComplexityIndicesEnum.AVERAGE_LEXICAL_BLOCK_COHESION_ADJACENT_SENTENCES);
    }

    @Override
    public double compute(AbstractDocument d) {
        return d.getBlocks().parallelStream()
                .filter(b -> b != null)
                .mapToDouble(b -> getBlockCohesionAdjacentSentences(b))
                .average().orElse(0.);
    }
}
    