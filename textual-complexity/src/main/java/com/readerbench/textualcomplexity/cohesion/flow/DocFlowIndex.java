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
package com.readerbench.textualcomplexity.cohesion.flow;

import com.readerbench.data.AbstractDocument;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;
import com.readerbench.coreservices.semanticModels.SimilarityType;

import java.util.function.Function;

/**
 *
 * @author Stefan Ruseti
 */
public class DocFlowIndex extends ComplexityIndex {
    private final DocFlowCriteria crit;
    private transient final Function<DocumentFlow, Double> op;

    public DocFlowIndex(ComplexityIndicesEnum index, DocFlowCriteria crit, SimilarityType simType, Function<DocumentFlow, Double> op) {
        super(index, null, simType, crit.getAcronym());
        this.crit = crit;
        this.op = op;
    }

    @Override
    public double compute(AbstractDocument d) {
        DocumentFlow df = new DocumentFlow(d, simType, crit);
        return op.apply(df);
    }
    
}
