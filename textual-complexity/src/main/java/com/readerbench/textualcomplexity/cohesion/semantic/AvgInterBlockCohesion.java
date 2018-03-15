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

import com.readerbench.data.AbstractDocument;
import com.readerbench.data.Block;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndices;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;
import com.readerbench.readerbenchcore.semanticModels.SimilarityType;

/**
 *
 * @author Stefan Ruseti
 */
public class AvgInterBlockCohesion extends ComplexityIndex {

    public AvgInterBlockCohesion(SimilarityType simType) {
        super(ComplexityIndicesEnum.AVERAGE_INTER_BLOCK_COHESION, simType);
    }

    @Override
    public double compute(AbstractDocument d) {
        if (!d.canUseSimType(simType)) {
            return ComplexityIndices.IDENTITY;
        }
        int no = 0;
        double sum = 0;
        for (int i = 0; i < d.getBlocks().size(); i++) {
            Block b = d.getBlocks().get(i);
            if (b != null) {
                for (int j = 0; j < d.getBlocks().size(); j++) {
                    if (i != j && d.getPrunnedBlockDistances() != null && d.getPrunnedBlockDistances()[i][j] != null
                            && d.getPrunnedBlockDistances()[i][j].getCohesion() > 0) {
                        sum += d.getPrunnedBlockDistances()[i][j].getSemanticSimilarities().get(simType);
                        no++;
                    }
                }
            }
        }
        if (no != 0) {
            return sum / no;
        }
        return ComplexityIndices.IDENTITY;
    }
}
