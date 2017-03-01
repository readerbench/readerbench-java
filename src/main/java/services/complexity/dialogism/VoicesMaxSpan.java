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
package services.complexity.dialogism;

import data.AbstractDocument;
import services.complexity.ComplexityIndicesEnum;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndices;

/**
 *
 * @author Stefan Ruseti
 */
public class VoicesMaxSpan extends ComplexityIndex {

    public VoicesMaxSpan() {
        super(ComplexityIndicesEnum.VOICES_MAX_SPAN);
    }

    @Override
    public double compute(AbstractDocument d) {
        if (d.getVoices() == null) {
            return ComplexityIndices.IDENTITY;
        }
        int max = d.getVoices().parallelStream()
                .mapToInt(c -> c.getWords().size())
                .max().orElse(ComplexityIndices.IDENTITY);
        return max;
    }
}
