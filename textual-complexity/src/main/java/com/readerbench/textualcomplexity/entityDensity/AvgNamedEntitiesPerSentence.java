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
package com.readerbench.textualcomplexity.entityDensity;

import com.readerbench.datasourceprovider.data.AbstractDocument;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndices;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;

/**
 *
 * @author Stefan Ruseti
 */
public class AvgNamedEntitiesPerSentence extends ComplexityIndex {

    public AvgNamedEntitiesPerSentence() {
        super(ComplexityIndicesEnum.AVERAGE_NO_NAMED_ENT_PER_SENTENCE);
    }

    @Override
    public double compute(AbstractDocument d) {
        return d.getSentencesInDocument().parallelStream()
                .mapToLong(s -> s.getAllWords().stream()
                        .filter(w -> w.getNE() != null && !w.getNE().equals("O"))
                        .count())
                .average().orElse(ComplexityIndices.IDENTITY);
    }

}
