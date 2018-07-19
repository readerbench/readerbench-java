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

/**
 *
 * @author Stefan Ruseti
 */
public class AvgIntraBlockCohesion extends ComplexityIndex {

    public AvgIntraBlockCohesion(SimilarityType simType, Lang lang) {
        super(ComplexityIndicesEnum.AVERAGE_INTRA_BLOCK_COHESION, lang, simType);
    }

    @Override
    public double compute(AbstractDocument d) {
        if (!d.canUseSimType(simType)) {
            return ComplexityIndices.IDENTITY;
        }
        return d.getBlocks().parallelStream()
                .filter(b -> b != null)
                .mapToDouble(b -> {
                    int no = 0;
                    double sum = 0;
                    for (int i = 0; i < b.getSentences().size(); i++) {
                        if (b.getSentences().get(i) != null) {
                            for (int j = 0; j < b.getSentences().size(); j++) {
                                if (i != j && b.getSentences().get(j) != null && b.getPrunnedSentenceDistances() != null
                                        && b.getPrunnedSentenceDistances()[i][j] != null
                                        && b.getPrunnedSentenceDistances()[i][j].getCohesion() > 0) {
                                    sum += b.getPrunnedSentenceDistances()[i][j].getSemanticSimilarities().get(simType);
                                    no++;
                                }
                            }
                        }
                    }
                    if (no != 0) {
                        return sum / no;
                    }
                    return -1;
                })
                .filter(avg -> avg > 0)
                .average().orElse(ComplexityIndices.IDENTITY);
    }
}
