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

import data.Lang;
import java.util.ArrayList;
import java.util.List;
import services.complexity.ComplexityIndecesFactory;
import services.complexity.ComplexityIndex;

/**
 *
 * @author Stefan Ruseti
 */
public class SurfaceFactory implements ComplexityIndecesFactory{

    @Override
    public List<ComplexityIndex> build(Lang lang) {
        List<ComplexityIndex> result = new ArrayList<>();
        result.add(new AvgBlockLength());
        result.add(new AvgCommasPerBlock());
        result.add(new AvgCommasPerSentence());
        result.add(new AvgSentenceLength());
        result.add(new AvgSentencesInBlock());
        result.add(new AvgUniqueWordsInBlock());
        result.add(new AvgUniqueWordsInSentence());
        result.add(new AvgWordLength());
        result.add(new AvgWordsInBlock());
        result.add(new AvgWordsInSentence());
        result.add(new CharEntropy());
        result.add(new SDSentencesInBlock());
        result.add(new SDUniqueWordsInBlock());
        result.add(new SDUniqueWordsInSentence());
        result.add(new SDWordsInBlock());
        result.add(new SDWordsInSentence());
        result.add(new WordEntropy());
        result.add(new WordLettersSD());
        return result;
    }
    
}
