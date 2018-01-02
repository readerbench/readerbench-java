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
package com.readerbench.services.nlp.listOfWords;

import com.readerbench.data.Lang;

public class Connectives {

    public static ClassesOfWords connectives_en = null;
    public static ClassesOfWords connectives_fr = null;
    public static ClassesOfWords connectives_ro = null;
    public static ClassesOfWords connectives_nl = null;
    public static ClassesOfWords connectives_la = null;

    public static boolean isConnective(String s, Lang lang) {
        if (lang == null) {
            return false;
        }
        ClassesOfWords connectives = getConnectives(lang);
        if (connectives == null) {
            return false;
        }
        return connectives.getAllWords().contains(s);
    }

    public static ClassesOfWords getConnectives(Lang lang) {
        switch (lang) {
            case en:
                return Connectives.getConnectivesEn();
            case fr:
                return Connectives.getConnectivesFr();
            case ro:
                return Connectives.getConnectivesRo();
            case nl:
                return Connectives.getConnectivesNl();
            case la:
                return Connectives.getConnectivesLa();
            default:
                return null;
        }
    }

    public static ClassesOfWords getConnectivesEn() {
        if (connectives_en == null) {
            connectives_en = new ClassesOfWords("resources/config/EN/word lists/connectives_en.txt");
        }
        return connectives_en;
    }

    public static ClassesOfWords getConnectivesFr() {
        if (connectives_fr == null) {
            connectives_fr = new ClassesOfWords("resources/config/FR/word lists/connectives_fr.txt");
        }
        return connectives_fr;
    }

    public static ClassesOfWords getConnectivesRo() {
        if (connectives_ro == null) {
            connectives_ro = new ClassesOfWords("resources/config/RO/word lists/connectives_ro.txt");
        }
        return connectives_ro;
    }

    public static ClassesOfWords getConnectivesNl() {
        if (connectives_nl == null) {
            connectives_nl = new ClassesOfWords("resources/config/NL/word lists/connectives_nl.txt");
        }
        return connectives_nl;
    }

    public static ClassesOfWords getConnectivesLa() {
        if (connectives_la == null) {
            connectives_la = new ClassesOfWords("resources/config/LA/word lists/connectives_la.txt");
        }
        return connectives_la;
    }
}
