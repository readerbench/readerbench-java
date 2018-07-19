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
package com.readerbench.textualcomplexity.surface;

import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.textualcomplexity.ComplexityIndex;
import com.readerbench.textualcomplexity.ComplexityIndicesFactory;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Stefan Ruseti
 */
public class SurfaceFactory extends ComplexityIndicesFactory {

    @Override
    public List<ComplexityIndex> build(Lang lang) {
        List<ComplexityIndex> result = new ArrayList<>();
        result.add(new AvgBlockLength(lang));
        result.add(new AvgCommasPerBlock(lang));
        result.add(new AvgCommasPerSentence(lang));
        result.add(new AvgSentenceLength(lang));
        result.add(new AvgSentencesInBlock(lang));
        result.add(new AvgUniqueWordsInBlock(lang));
        result.add(new AvgUniqueWordsInSentence(lang));
        result.add(new AvgWordLength(lang));
        result.add(new AvgWordsInBlock(lang));
        result.add(new AvgWordsInSentence(lang));
        result.add(new CharEntropy(lang));
        result.add(new SDSentencesInBlock(lang));
        result.add(new SDUniqueWordsInBlock(lang));
        result.add(new SDUniqueWordsInSentence(lang));
        result.add(new SDWordsInBlock(lang));
        result.add(new SDWordsInSentence(lang));
        result.add(new WordEntropy(lang));
        result.add(new WordLettersSD(lang));
        return result;
    }

}
