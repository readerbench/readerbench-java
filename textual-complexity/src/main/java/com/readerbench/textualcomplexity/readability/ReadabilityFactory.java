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

import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndicesFactory;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Stefan Ruseti
 */
public class ReadabilityFactory extends ComplexityIndicesFactory{

    @Override
    public List<ComplexityIndex> build(Lang lang) {
        List<ComplexityIndex> result = new ArrayList<>();
        if (lang != Lang.en) return result;
        result.add(new ReadabilityFlesch());
        result.add(new ReadabilityFog());
        result.add(new ReadabilityKincaid());
        result.add(new ReadabilityDaleChall());
        return result;
    }
}
