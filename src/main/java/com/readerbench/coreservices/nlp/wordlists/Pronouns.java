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
package com.readerbench.coreservices.nlp.wordlists;

import com.readerbench.datasourceprovider.pojo.Lang;

/**
 * Class containing all pronoun lists for supported languages that makes use of
 * lazy initialization
 *
 * @author Mihai Dascalu
 */
public class Pronouns {

    public static ClassesOfWords pronouns_en = null;
    public static ClassesOfWords pronouns_fr = null;
    public static ClassesOfWords pronouns_ro = null;
    public static ClassesOfWords pronouns_nl = null;
    public static ClassesOfWords pronouns_la = null;

    public static boolean isConnective(String s, Lang lang) {
        if (lang == null) {
            return false;
        }
        ClassesOfWords pronouns = getPronouns(lang);
        if (pronouns == null) {
            return false;
        }
        return pronouns.getAllWords().contains(s);
    }

    public static ClassesOfWords getPronouns(Lang lang) {
        switch (lang) {
            case en:
                return getPronounsEn();
            case fr:
                return getPronounsFr();
            case ro:
                return getPronounsRo();
            case nl:
                return getPronounsNl();
            case la:
                return getPronounsLa();
            default:
                return null;
        }
    }

    public static ClassesOfWords getPronounsEn() {
        if (pronouns_en == null) {
            pronouns_en = new ClassesOfWords("resources/config/EN/word lists/pronouns_en.txt");
        }
        return pronouns_en;
    }

    public static ClassesOfWords getPronounsFr() {
        if (pronouns_fr == null) {
            pronouns_fr = new ClassesOfWords("resources/config/FR/word lists/pronouns_fr.txt");
        }
        return pronouns_fr;
    }

    public static ClassesOfWords getPronounsRo() {
        if (pronouns_ro == null) {
            pronouns_ro = new ClassesOfWords("resources/config/RO/word lists/pronouns_ro.txt");
        }
        return pronouns_ro;
    }

    public static ClassesOfWords getPronounsNl() {
        if (pronouns_nl == null) {
            pronouns_nl = new ClassesOfWords("resources/config/NL/word lists/pronouns_nl.txt");
        }
        return pronouns_nl;
    }

    public static ClassesOfWords getPronounsLa() {
        if (pronouns_la == null) {
            pronouns_la = new ClassesOfWords("resources/config/EN/word lists/pronouns_la.txt");
        }
        return pronouns_la;
    }
}
