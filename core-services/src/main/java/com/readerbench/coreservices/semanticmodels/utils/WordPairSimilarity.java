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
package com.readerbench.coreservices.semanticmodels.utils;

public class WordPairSimilarity {

    private final String word1;
    private final String word2;
    private final double similarity;

    public WordPairSimilarity(String word1, String word2, double similarity) {
        this.word1 = word1;
        this.word2 = word2;
        this.similarity = similarity;
    }

    public double getSimilarity() {
        return this.similarity;
    }

    public String getWord1() {
        return this.word1;
    }

    public String getWord2() {
        return this.word2;
    }
}
