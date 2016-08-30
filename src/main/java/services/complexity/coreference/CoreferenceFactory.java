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
package services.complexity.coreference;

import data.Lang;
import java.util.ArrayList;
import java.util.List;
import services.complexity.ComplexityIndecesFactory;
import services.complexity.ComplexityIndex;

/**
 *
 * @author Stefan Ruseti
 */
public class CoreferenceFactory implements ComplexityIndecesFactory {

    @Override
    public List<ComplexityIndex> build(Lang lang) {
        List<ComplexityIndex> result = new ArrayList<>();
        if (lang != Lang.eng) return result;
        result.add(new TotalCorefChains());
        result.add(new AvgCorefsPerChain());
        result.add(new AvgChainSpan());
        result.add(new AvgInferenceDistPerChain());
        result.add(new ActiveCorefChainsPerWord());
        result.add(new CorefChainsWithBigSpan());
        return result;
    }
    
}

