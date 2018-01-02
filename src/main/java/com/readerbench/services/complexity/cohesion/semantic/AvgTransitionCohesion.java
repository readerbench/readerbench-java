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
package com.readerbench.services.complexity.cohesion.semantic;

import com.readerbench.data.AbstractDocument;
import com.readerbench.data.Block;
import com.readerbench.data.discourse.SemanticCohesion;
import com.readerbench.services.complexity.ComplexityIndex;
import com.readerbench.services.complexity.ComplexityIndices;
import com.readerbench.services.complexity.ComplexityIndicesEnum;
import com.readerbench.services.semanticModels.SimilarityType;

/**
 *
 * @author Stefan Ruseti
 */
public class AvgTransitionCohesion extends ComplexityIndex {

    public AvgTransitionCohesion(SimilarityType simType) {
        super(ComplexityIndicesEnum.AVERAGE_TRANSITION_COHESION, simType);
    }

    @Override
    public double compute(AbstractDocument d) {
        if (!d.canUseSimType(simType)) {
            return ComplexityIndices.IDENTITY;
        }
        int no = 0;
        double sum = 0;
        Block previous = null;
        Block current;
        for (Block b : d.getBlocks()) {
            if (b != null) {
                current = b;
                if (previous != null) {
                    if (!current.getSentences().isEmpty() && !previous.getSentences().isEmpty()) {
                        SemanticCohesion coh = new SemanticCohesion(current.getSentences().get(0),
                                previous.getSentences().get(previous.getSentences().size() - 1));
                        sum += coh.getSemanticSimilarities().get(simType);
                        no++;
                    }
                }
                previous = b;
            }
        }
        if (no != 0) {
            return sum / no;
        }
        return ComplexityIndices.IDENTITY;
    }

}