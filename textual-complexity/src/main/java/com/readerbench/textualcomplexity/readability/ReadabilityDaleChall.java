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
package com.readerbench.textualcomplexity.readability;

import com.readerbench.coreservices.data.AbstractDocument;
import com.readerbench.textualcomplexity.ComplexityIndices;
import com.readerbench.textualcomplexity.ComplexityIndicesEnum;

/**
 *
 * @author Stefan Ruseti
 */
public class ReadabilityDaleChall extends ReadabilityIndex {
    
    public ReadabilityDaleChall() {
        super(ComplexityIndicesEnum.READABILITY_DALE_CHALL);
    }

    @Override
    public double compute(AbstractDocument d) {
        if (d.getText() == null || d.getText().length() == 0) {
            return ComplexityIndices.IDENTITY;
        }
        return computeDaleChall(d);
    }

}
