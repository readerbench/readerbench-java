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
public class AvgIntraSentenceLexicalCohesion extends LexicalCohesion{

    public AvgIntraSentenceLexicalCohesion(SimilarityType simType) {
        super(ComplexityIndicesEnum.AVERAGE_INTRA_SENTENCE_LEXICAL_COHESION);
    }

    @Override
    public double compute(AbstractDocument d) {
        return d.getSentencesInDocument().parallelStream()
                .mapToDouble(s -> getIntraSentenceCohesion(s))
                .average().orElse(0.);
    }
    
    
}

