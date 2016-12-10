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
package services.complexity.cohesion.semantic;

import data.AbstractDocument;
import data.discourse.SemanticCohesion;
import services.complexity.ComplexityIndicesEnum;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndices;
import services.semanticModels.SimilarityType;

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
        if (!d.getModelVectors().keySet().contains(simType)) {
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
