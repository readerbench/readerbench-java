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
package com.readerbench.textualcomplexity.coreference;

import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;
import org.apache.commons.lang3.NotImplementedException;

/**
 *
 * @author Stefan Ruseti
 */
public class AvgInferenceDistPerChain extends CoreferenceIndex {

    public AvgInferenceDistPerChain() {
        super(ComplexityIndicesEnum.AVERAGE_INFERENCE_DISTANCE_PER_CHAIN);
    }

    @Override
    public double compute(AbstractDocument d) {
        throw new UnsupportedOperationException("Not supported yet.");
//        return d.getBlocks().parallelStream()
//                .filter(b -> b != null)
//                .filter(b -> b.getCorefs() != null)
//                .flatMapToDouble(b -> b.getCorefs().values().stream()
//                        .filter(c -> c.getMentionsInTextualOrder().size() > 1)
//                        .mapToDouble(c -> getInferenceDistance(c, b.getStanfordSentences())))
//                .average().orElse(ComplexityIndices.IDENTITY);
    }

}

