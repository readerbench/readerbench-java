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
package com.readerbench.textualcomplexity.entityDensity;

import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.textualcomplexity.AbstractComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndices;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;
import com.readerbench.textualcomplexity.IndexLevel;

import java.util.Map;

/**
 *
 * @author Stefan Ruseti
 */
public class AvgNamedEntities extends AbstractComplexityIndex {

    public AvgNamedEntities(ComplexityIndicesEnum index, Lang lang, IndexLevel level) {
        super(index, lang, level);
    }

    @Override
    public double compute(AbstractDocument d) {
        return normalize(d, streamFunction.apply(d)
                .mapToInt(b -> b.getWordOccurences().entrySet().stream()
                        .filter(e -> e.getKey().getNE() != null && !e.getKey().getNE().equals("O"))
                        .mapToInt(Map.Entry::getValue)
                        .sum())
                .average().orElse(ComplexityIndices.IDENTITY));
    }

}
