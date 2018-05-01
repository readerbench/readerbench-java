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
package com.readerbench.textualcomplexity.cohesion.semantic;

import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.coreservices.semanticmodels.data.SimilarityType;
import com.readerbench.coreservices.data.discourse.SemanticCohesion;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndices;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;

/**
 *
 * @author Stefan
 */
public class AvgBlockDocCohesion extends ComplexityIndex {

    public AvgBlockDocCohesion(SimilarityType simType) {
        super(ComplexityIndicesEnum.AVERAGE_BLOCK_DOC_COHESION, simType);
    }

    @Override
    public double compute(AbstractDocument d) {
        if (!d.canUseSimType(simType)) {
            return ComplexityIndices.IDENTITY;
        }
        int no = 0;
        double sum = 0;
        if (d != null && d.getBlockDocDistances() != null) {
            for (SemanticCohesion coh : d.getBlockDocDistances()) {
                if (coh != null && coh.getCohesion() > 0) {
                    sum += coh.getSemanticSimilarities().get(simType);
                    no++;
                }
            }
        }
        if (no == 1) {
            return ComplexityIndices.IDENTITY;
        }
        if (no != 0) {
            return sum / no;
        }
        return ComplexityIndices.IDENTITY;
    }
}
