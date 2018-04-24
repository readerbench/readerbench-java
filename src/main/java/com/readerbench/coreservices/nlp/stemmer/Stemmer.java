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
package com.readerbench.coreservices.nlp.stemmer;

import com.readerbench.coreservices.nlp.lemmatizer.StaticLemmatizer;
import com.readerbench.datasourceprovider.pojo.Lang;

public class Stemmer {

    /**
     *
     * @param word
     * @param lang
     * @return
     */
    public static synchronized String stemWord(String word, Lang lang) {
        String w = word.toLowerCase();
        if (lang == null) {
            return w;
        }
        switch (lang) {
            case fr:
                return Stemmer_FR.stemWord(w);
            case ro:
                return Stemmer_RO.stemWord(w);
            case it:
                return Stemmer_IT.stemWord(w);
            case es:
                return Stemmer_ES.stemWord(w);
            case nl:
                return Stemmer_NL.stemWord(w);
            // TODO implement latin stemmer, for now rely on lemmas
            case la:
                return StaticLemmatizer.lemmaStatic(w, Lang.la);
            default:
                return Stemmer_EN.stemWord(w);
        }
    }

    public static void main(String[] args) {
        System.out.println(stemWord("information", Lang.en));
    }
}
