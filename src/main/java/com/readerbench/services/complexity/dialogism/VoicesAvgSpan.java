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
package com.readerbench.services.complexity.dialogism;

import com.readerbench.data.AbstractDocument;
import com.readerbench.services.complexity.ComplexityIndex;
import com.readerbench.services.complexity.ComplexityIndices;
import com.readerbench.services.complexity.ComplexityIndicesEnum;

/**
 *
 * @author Stefan Ruseti
 */
public class VoicesAvgSpan extends ComplexityIndex {

    public VoicesAvgSpan() {
        super(ComplexityIndicesEnum.VOICES_AVERAGE_SPAN);
    }

    @Override
    public double compute(AbstractDocument d) {
        if (d.getVoices() == null) {
            return ComplexityIndices.IDENTITY;
        }
        int noVoices = d.getVoices().size();
        int noWords = d.getVoices().parallelStream()
                .mapToInt(c -> c.getWords().size())
                .sum();
        if (noVoices != 0) {
            return ((double) noWords) / noVoices;
        }
        return ComplexityIndices.IDENTITY;
    }

}
