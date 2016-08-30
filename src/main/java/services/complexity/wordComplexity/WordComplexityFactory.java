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
package services.complexity.wordComplexity;

import data.Lang;
import java.util.ArrayList;
import java.util.List;
import services.complexity.ComplexityIndecesEnum;
import services.complexity.ComplexityIndecesFactory;
import services.complexity.ComplexityIndex;

/**
 *
 * @author Stefan Ruseti
 */
public class WordComplexityFactory extends ComplexityIndecesFactory {

    @Override
    public List<ComplexityIndex> build(Lang lang) {
        List<ComplexityIndex> result = new ArrayList<>();
        result.add(new WordComplexity(
                ComplexityIndecesEnum.WORD_DIFF_LEMMA_STEM, lang,
                WordComplexity::getDifferenceBetweenLemmaAndStem));
        result.add(new WordComplexity(
                ComplexityIndecesEnum.WORD_DIFF_WORD_STEM, lang,
                WordComplexity::getDifferenceBetweenWordAndStem));
        result.add(new WordComplexity(
                ComplexityIndecesEnum.WORD_MAX_DEPTH_HYPERNYM_TREE, lang,
                WordComplexity::getMaxDistanceToHypernymTreeRoot));
        result.add(new WordComplexity(
                ComplexityIndecesEnum.WORD_AVERAGE_DEPTH_HYPERNYM_TREE, lang,
                WordComplexity::getAverageDistanceToHypernymTreeRoot));
        result.add(new WordComplexity(
                ComplexityIndecesEnum.WORD_PATH_COUNT_HYPERNYM_TREE, lang,
                WordComplexity::getPathCountToHypernymTreeRoot));
        result.add(new WordComplexity(
                ComplexityIndecesEnum.WORD_POLYSEMY_COUNT, lang,
                WordComplexity::getPolysemyCount));
        if (lang != Lang.fr) {
            result.add(new WordComplexity(
                    ComplexityIndecesEnum.WORD_SYLLABLE_COUNT, lang,
                    WordComplexity::getSyllables));
        }
        return result;
    }

}
