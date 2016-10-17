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
package services.complexity.cohesion.lexicalChains;

import data.AbstractDocument;
import services.complexity.ComplexityIndecesEnum;
import services.complexity.ComplexityIndex;
import services.complexity.ComplexityIndices;

/**
 *
 * @author Stefan Ruseti
 */
public class LexicalChainsMaxSpan extends ComplexityIndex {

    public LexicalChainsMaxSpan() {
        super(ComplexityIndecesEnum.LEXICAL_CHAINS_MAX_SPAN);
    }

    @Override
    public double compute(AbstractDocument d) {
        int max = d.getLexicalChains().parallelStream()
                .mapToInt(c -> c.getLinks().size())
                .max().orElse(ComplexityIndices.IDENTITY);
        return max;
    }
}
