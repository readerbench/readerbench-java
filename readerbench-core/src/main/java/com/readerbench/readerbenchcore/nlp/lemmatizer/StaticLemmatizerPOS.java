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
package com.readerbench.readerbenchcore.nlp.lemmatizer;

import com.readerbench.data.Lang;
import org.openide.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class StaticLemmatizerPOS {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaticLemmatizerPOS.class);

    private static Map<String, String> lemmas_fr = null;
    private static Map<String, String> lemmas_it = null;
    private static Map<String, String> lemmas_es = null;

    private static Map<String, String> initialize(String path, Lang lang) {
        LOGGER.info("Initializing lemmas from {} ...", path);
        Map<String, String> lemmas = new TreeMap<>();
        BufferedReader in;
        try {
            FileInputStream inputFile = new FileInputStream(path);
            InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
            in = new BufferedReader(ir);
            String line;
            StringTokenizer strk;
            while ((line = in.readLine()) != null) {
                strk = new StringTokenizer(line, "|");
                lemmas.put(strk.nextToken().toLowerCase(), strk.nextToken().toLowerCase());
            }
            in.close();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return lemmas;
    }

    public static String lemmaStatic(String word, String pos, Lang lang) {
        String w = word.toLowerCase();
        Map<String, String> lemmas;
        switch (lang) {
            case fr:
                lemmas = getLemmasFr();
                break;
            case it:
                lemmas = getLemmasIt();
                break;
            case es:
                lemmas = getLemmasEs();
                break;
            default:
                return StaticLemmatizer.lemmaStatic(w, lang);
        }
        if (lemmas == null) {
            return w;
        }
        String lemma = null;
        if (pos != null) {
            lemma = lemmas.get((w + "_" + pos).toLowerCase());
            if (lemma != null) {
                return lemma;
            }
        }
        // try each significant POS
        String[] possiblePOSs = {"NN", "VB", "JJ", "RB", "PR", "DT", "IN", "UH", "CC"};
        for (String possiblePOS : possiblePOSs) {
            String concept = (w + "_" + possiblePOS).toLowerCase();
            if (lemmas.containsKey(concept)) {
                lemma = lemmas.get(concept);
                break;
            }
        }
        if (lemma != null) {
            return lemma;
        }
        return w;
    }

    public static Map<String, String> getLemmasFr() {
        if (lemmas_fr == null) {
            lemmas_fr = initialize("resources/config/FR/word lists/lemmas_pos_fr.txt", Lang.fr);
        }
        return lemmas_fr;
    }

    public static Map<String, String> getLemmasIt() {
        if (lemmas_it == null) {
            lemmas_it = initialize("resources/config/IT/word lists/lemmas_pos_it.txt", Lang.it);
        }
        return lemmas_it;
    }

    public static Map<String, String> getLemmasEs() {
        if (lemmas_es == null) {
            lemmas_es = initialize("resources/config/ES/word lists/lemmas_pos_es.txt", Lang.es);
        }
        return lemmas_es;
    }

    public static void main(String[] args) {
        System.out.println(StaticLemmatizerPOS.lemmaStatic("pointés", null, Lang.fr));
        System.out.println(StaticLemmatizerPOS.lemmaStatic("mangio", "VB", Lang.it));
    }
}
