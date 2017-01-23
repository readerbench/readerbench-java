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
package services.nlp.lemmatizer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import data.Lang;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

public class StaticLemmatizerPOS {

    static Logger logger = Logger.getLogger("");

    private static Map<String, String> lemmas_fr = null;
    private static Map<String, String> lemmas_it = null;

    private static Map<String, String> initialize(String path, Lang lang) {
        logger.info("Initializing lemmas from " + path + " ...");
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
        } catch (Exception ex) {
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
                return StaticLemmatizer.lemmaStatic(w, Lang.es);
            case en:
                return StaticLemmatizer.lemmaStatic(w, Lang.en);
            // return Morphology.lemmaStatic(w, pos, true);
            default:
                lemmas = null;
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

    public static void main(String[] args) {

        System.out.println(StaticLemmatizerPOS.lemmaStatic("pointés", null, Lang.fr));
        System.out.println(StaticLemmatizerPOS.lemmaStatic("mangio", "VB", Lang.it));
    }
}
