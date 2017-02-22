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
package services.complexity.surface;

import data.AbstractDocument;
import org.apache.commons.lang3.StringUtils;
import services.complexity.ComplexityIndicesEnum;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndices;

/**
 *
 * @author Stefan Ruseti
 */
public class AvgCommasPerBlock extends ComplexityIndex {

    public AvgCommasPerBlock() {
        super(ComplexityIndicesEnum.AVERAGE_COMMAS_PER_BLOCK);
    }

    @Override
    public double compute(AbstractDocument d) {
        return d.getBlocks().parallelStream()
                .filter(b -> b != null)
                .mapToInt(b -> b.getSentences().stream()
                        .mapToInt(s -> StringUtils.countMatches(s.getText(), ","))
                        .sum())
                .average().orElse(ComplexityIndices.IDENTITY);
    }

}
