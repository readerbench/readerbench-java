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
package com.readerbench.textualcomplexity.surface;

import com.readerbench.coreservices.commons.DoubleStatistics;
import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndices;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;

/**
 *
 * @author Stefan Ruseti
 */
public class SDWordsInBlock extends ComplexityIndex {

    public SDWordsInBlock(Lang lang) {
        super(ComplexityIndicesEnum.BLOCK_STANDARD_DEVIATION_NO_WORDS, lang);
    }

    @Override
    public double compute(AbstractDocument d) {
        return d.getBlocks().parallelStream()
                .filter(b -> b != null)
                .map(b -> b.getSentences().stream()
                .mapToInt(s -> s.getWords().size())
                .sum() * 1.)
                .collect(DoubleStatistics.collector())
                .getStandardDeviation(ComplexityIndices.IDENTITY);
    }

}
