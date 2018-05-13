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
package com.readerbench.coreservices.semanticmodels.data;

import com.readerbench.coreservices.semanticmodels.SimilarityType;
import com.readerbench.datasourceprovider.pojo.Lang;

import java.io.Serializable;

/**
 * Enum class to store Semantic Corpora
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public enum SemanticCorpora implements Serializable {
    tasa_en_lsa("TASA", Lang.en, SimilarityType.LSA),
    tasa_en_lda("TASA", Lang.en, SimilarityType.LDA),
    tasa_en_word2vec("TASA", Lang.en, SimilarityType.WORD2VEC),
    tasa_lak_en_lsa("TASA_LAK", Lang.en, SimilarityType.LSA),
    tasa_lak_en_lda("TASA_LAK", Lang.en, SimilarityType.LDA),
    sciref_en_lsa("SciRef", Lang.en, SimilarityType.LSA),
    enea_tasa_en_lsa("ENEA_TASA", Lang.en, SimilarityType.LSA),
    enea_tasa_en_lda("ENEA_TASA", Lang.en, SimilarityType.LDA),
    enea_tasa_en_word2vec("ENEA_TASA", Lang.en, SimilarityType.WORD2VEC),
    le_monde_fr_lsa("Le_Monde", Lang.fr, SimilarityType.LSA),
    le_monde_fr_lda("Le_Monde", Lang.fr, SimilarityType.LDA),
    le_monde_fr_word2vec("Le_Monde", Lang.fr, SimilarityType.WORD2VEC),
    inl_nl_lda("INL", Lang.nl, SimilarityType.LDA),
    inl_nl_word2vec("INL", Lang.nl, SimilarityType.WORD2VEC),
    jose_antonio_es_lsa("Jose_Antonio", Lang.es, SimilarityType.LSA),
    jose_antonio_es_lda("Jose_Antonio", Lang.es, SimilarityType.LDA),
    jose_antonio_es_word2vec("Jose_Antonio", Lang.es, SimilarityType.WORD2VEC);

    private final static String SEMANTIC_CORPORA_ROOT = "resources/config/";
    private final String corpora;
    private final Lang lang;
    private final SimilarityType simType;
    private final String fullPath;

    private SemanticCorpora(String corpora, Lang lang, SimilarityType simType) {
        this.corpora = corpora;
        this.lang = lang;
        this.simType = simType;
        this.fullPath = SEMANTIC_CORPORA_ROOT + lang.toString().toUpperCase() + '/' + simType.getAcronym() + '/' + corpora;
    }

    public String getCorpora() {
        return corpora;
    }

    public Lang getLang() {
        return lang;
    }

    public SimilarityType getSimType() {
        return simType;
    }

    public String getFullPath() {
        return fullPath;
    }

    public static SemanticCorpora getSemanticCorpora(String semanticCorpora, Lang lang, SimilarityType simType) {
        for (SemanticCorpora sm : SemanticCorpora.values()) {
            if (sm.getCorpora().equals(semanticCorpora) && sm.getLang().equals(lang) && sm.getSimType().equals(simType)) {
                return sm;
            }
        }

        try {
            StringBuilder sb = new StringBuilder();
            sb.append(semanticCorpora.toLowerCase()).append('_').append(lang.toString()).append('_').append(simType.getAcronym().toLowerCase());
            SemanticCorpora sm = SemanticCorpora.valueOf(sb.toString());
            return sm;
        } catch (IllegalArgumentException ex) {
            //default TASA EN LSA
            return SemanticCorpora.tasa_en_lsa;
        }
    }
}
