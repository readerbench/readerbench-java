/**
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
package com.readerbench.textualcomplexity.cohesion.semantic;

import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.coreservices.semanticmodels.SimilarityType;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndices;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;
import java.util.Arrays;

/**
 *
 * @author Stefan Ruseti
 */
public class AvgSentenceBlockCohesion extends ComplexityIndex {

    public AvgSentenceBlockCohesion(SimilarityType simType, Lang lang) {
        super(ComplexityIndicesEnum.AVERAGE_SENTENCE_BLOCK_COHESION, lang, simType);
    }

    @Override
    public double compute(AbstractDocument d) {
        if (!d.canUseSimType(simType)) {
            return ComplexityIndices.IDENTITY;
        }
        return d.getBlocks().parallelStream()
                .filter(b -> b != null)
                .filter(b -> b.getSentenceBlockDistances() != null)
                .mapToDouble(b -> Arrays.stream(b.getSentenceBlockDistances())
                        .filter(coh -> coh != null && coh.getCohesion() > 0)
                        .mapToDouble(coh -> coh.getSemanticSimilarities().get(simType))
                        .average().orElse(-1))
                .filter(avg -> avg > 0)
                .average().orElse(ComplexityIndices.IDENTITY);
    }

}
