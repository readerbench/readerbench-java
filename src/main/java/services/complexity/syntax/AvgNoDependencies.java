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
package services.complexity.syntax;

import data.AbstractDocument;
import services.complexity.ComplexityIndicesEnum;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndices;

/**
 *
 * @author Stefan Ruseti
 */
public class AvgNoDependencies extends ComplexityIndex {

    public AvgNoDependencies() {
        super(ComplexityIndicesEnum.AVERAGE_NO_SEMANTIC_DEPENDENCIES);
    }

    @Override
    public double compute(AbstractDocument d) {
        return d.getSentencesInDocument().parallelStream()
                .filter(s -> s.getWords().size() > 0)
                .mapToInt(s -> {
                    if (s.getDependencies() == null) return 0;
                    return s.getDependencies().typedDependencies().size();
                })
                .average().orElse(ComplexityIndices.IDENTITY);
    }
    
}
