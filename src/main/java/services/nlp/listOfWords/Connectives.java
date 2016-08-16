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

public class Connectives {

    public static final ClassesOfWords CONNECTIVES_EN = new ClassesOfWords(
            "resources/config/WordLists/connectives_en.txt");
    public static final ClassesOfWords CONNECTIVES_FR = new ClassesOfWords(
            "resources/config/WordLists/connectives_fr.txt");
    public static final ClassesOfWords CONNECTIVES_RO = new ClassesOfWords(
            "resources/config/WordLists/connectives_ro.txt");
    public static final ClassesOfWords CONNECTIVES_NL = new ClassesOfWords(
            "resources/config/WordLists/connectives_nl.txt");
    public static final ClassesOfWords CONNECTIVES_LA = new ClassesOfWords(
            "resources/config/WordLists/connectives_la.txt");

    public static final int NO_CONNECTIVE_TYPES_EN = CONNECTIVES_EN.getClasses().size();
    public static final int NO_CONNECTIVE_TYPES_FR = CONNECTIVES_FR.getClasses().size();
    public static final int NO_CONNECTIVE_TYPES_RO = CONNECTIVES_RO.getClasses().size();
    public static final int NO_CONNECTIVE_TYPES_NL = CONNECTIVES_NL.getClasses().size();
    public static final int NO_CONNECTIVE_TYPES_LA = CONNECTIVES_LA.getClasses().size();

    public static boolean isConnective(String s, Lang lang) {
        if (lang == null) {
            return false;
        }
        switch (lang) {
            case fr:
                return CONNECTIVES_FR.getAllWords().contains(s);
            case ro:
                return CONNECTIVES_RO.getAllWords().contains(s);
            case nl:
                return CONNECTIVES_NL.getAllWords().contains(s);
            case la:
                return CONNECTIVES_LA.getAllWords().contains(s);
            case eng:
                return CONNECTIVES_EN.getAllWords().contains(s);
            default:
                return false;
        }
    }
}
