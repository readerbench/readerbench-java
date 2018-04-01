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
package services.complexity.cohesion.lexical;

import data.AbstractDocument;
import data.Sentence;
import java.util.Arrays;
import services.complexity.ComplexityIndicesEnum;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndices;
import services.semanticModels.SimilarityType;

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

