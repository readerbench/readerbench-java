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
package com.readerbench.services.complexity.surface;

import com.readerbench.data.AbstractDocument;
import com.readerbench.services.complexity.ComplexityIndex;
import com.readerbench.services.complexity.ComplexityIndices;
import com.readerbench.services.complexity.ComplexityIndicesEnum;

/**
 *
 * @author Stefan Ruseti
 */
public class AvgSentenceLength extends ComplexityIndex {

    public AvgSentenceLength() {
        super(ComplexityIndicesEnum.AVERAGE_SENTENCE_LENGTH);
    }

    @Override
    public double compute(AbstractDocument d) {
        return d.getSentencesInDocument().parallelStream()
                .mapToInt(s -> s.getWords().stream()
                        .mapToInt(w -> w.getText().length())
                        .sum())
                .average().orElse(ComplexityIndices.IDENTITY);
    }

}