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

import com.readerbench.datasourceprovider.data.AbstractDocument;
import com.readerbench.datasourceprovider.data.Block;
import com.readerbench.datasourceprovider.data.discourse.SemanticCohesion;
import com.readerbench.datasourceprovider.data.semanticmodels.SimilarityType;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndices;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;

/**
 *
 * @author Stefan Ruseti
 */
public class AvgStartMiddleCohesion extends ComplexityIndex {

    public AvgStartMiddleCohesion(SimilarityType simType) {
        super(ComplexityIndicesEnum.AVERAGE_START_MIDDLE_COHESION, simType);
    }

    @Override
    public double compute(AbstractDocument d) {
        if (!d.canUseSimType(simType)) {
            return ComplexityIndices.IDENTITY;
        }
        double no = 0;
        double sum = 0;
        Block startBlock = null;
        int startIndex = 0;
        int endIndex = d.getBlocks().size() - 1;
        for (; startIndex < d.getBlocks().size(); startIndex++) {
            Block b = d.getBlocks().get(startIndex);
            if (b != null) {
                startBlock = b;
                break;
            }
        }
        for (; endIndex >= 0; endIndex--) {
            Block b = d.getBlocks().get(endIndex);
            if (b != null) {
                break;
            }
        }

        for (int i = startIndex + 1; i < endIndex; i++) {
            Block b = d.getBlocks().get(i);
            if (b != null) {
                SemanticCohesion coh = new SemanticCohesion(startBlock, b);
                sum += coh.getSemanticSimilarities().get(simType) / (i - startIndex);
                no += 1D / (i - startIndex);
            }
        }

        if (no != 0) {
            return sum / no;
        }
        return ComplexityIndices.IDENTITY;
    }

}
