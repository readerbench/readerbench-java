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
package services.semanticModels.WordNet;

/**
 *
 * @author Stefan
 */
public enum SimilarityType {
    LEACOCK_CHODOROW("LeackockChodorow", "Leackock-Chodorow semantic distance in WordNet"),
    WU_PALMER("WuPalmer", "Wu-Palmer semantic distance in WordNet"),
    PATH_SIM("Path", "Inverse path length in WordNet"),
    LSA("LSA", "Cosine similarity in LSA vector space"),
    LDA("LDA", "Inverse JSH in LDA probability distribution"),
    WORD2VEC("W2V", "Cosine similarity in word2vec space");

    private final String acronym;
    private final String name;

    private SimilarityType(String acronym, String name) {
        this.acronym = acronym;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getAcronym() {
        return acronym;
    }
}
