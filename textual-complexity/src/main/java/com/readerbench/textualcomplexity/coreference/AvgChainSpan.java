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
package com.readerbench.textualcomplexity.coreference;

import com.readerbench.datasourceprovider.data.AbstractDocument;
import com.readerbench.textualcomplexity.ComplexityIndices;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;

/**
 *
 * @author Stefan Ruseti
 */
public class AvgChainSpan extends CoreferenceIndex {

    public AvgChainSpan() {
        super(ComplexityIndicesEnum.AVERAGE_CHAIN_SPAN);
    }

    @Override
    public double compute(AbstractDocument d) {
        CoreferenceResolutionData data = analyse(d.getBlocks());
        if (data.getNoChains() != 0)
			return 1. * data.getTotalSizeOfSpan() / data.getNoChains();
		return ComplexityIndices.IDENTITY;
    }

}

