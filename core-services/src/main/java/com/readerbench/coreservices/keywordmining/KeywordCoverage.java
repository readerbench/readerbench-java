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
package com.readerbench.coreservices.keywordmining;

import com.readerbench.coreservices.nlp.wordlists.MapOfWordWeights;
import com.readerbench.coreservices.data.AnalysisElement;
import com.readerbench.coreservices.data.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeywordCoverage {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeywordCoverage.class);

    // returns the coverage with the predefined topic class
    public static double coverage(MapOfWordWeights usedMap, AnalysisElement el) {
        double coverage = 0;

        if (usedMap != null && el != null) {
            int noOccurrences = 0, totalOccurrences = 0;
            for (Word w : el.getWordOccurences().keySet()) {
                totalOccurrences += el.getWordOccurences().get(w);
                if (usedMap.getWords().containsKey(w.getLemma())) {
                    noOccurrences += usedMap.getWords().get(w.getLemma()) * el.getWordOccurences().get(w);
                }
            }
            if (totalOccurrences != 0) {
                coverage = ((double) noOccurrences) / totalOccurrences;
            }
        }
        return coverage;
    }
}
