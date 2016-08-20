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
package services.nlp.listOfWords;

import data.Lang;

public class Pronouns {

    public static final ClassesOfWords PRONOUNS_EN = new ClassesOfWords(
            "resources/config/EN/word lists/pronouns_en.txt");
    public static final ClassesOfWords PRONOUNS_FR = new ClassesOfWords(
            "resources/config/FR/word lists/pronouns_fr.txt");
    public static final ClassesOfWords PRONOUNS_RO = new ClassesOfWords(
            "resources/config/RO/word lists/pronouns_ro.txt");
    public static final ClassesOfWords PRONOUNS_NL = new ClassesOfWords(
            "resources/config/NL/word lists/pronouns_nl.txt");
    public static final ClassesOfWords PRONOUNS_LA = new ClassesOfWords(
            "resources/config/LA/word lists/pronouns_la.txt");
    public static final int NO_PRONOUN_TYPES = PRONOUNS_EN.getClasses().size();

    public static boolean isConnective(String s, Lang lang) {
        if (lang == null) {
            return false;
        }
        switch (lang) {
            case fr:
                return PRONOUNS_FR.getAllWords().contains(s);
            case ro:
                return PRONOUNS_RO.getAllWords().contains(s);
            case nl:
                return PRONOUNS_NL.getAllWords().contains(s);
            case la:
                return PRONOUNS_LA.getAllWords().contains(s);
            default:
                return PRONOUNS_EN.getAllWords().contains(s);
        }
    }
}
