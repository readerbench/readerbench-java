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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;



import data.Lang;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

public class StaticLemmatizer {

    static Logger logger = Logger.getLogger("");

    private static Map<String, String> lemmas_en;
    private static Map<String, String> lemmas_ro;
    private static Map<String, String> lemmas_es;
    private static Map<String, String> lemmas_nl;
    private static Map<String, String> lemmas_la;

    private static Map<String, String> initialize(String path, Lang lang) {
        logger.info("Initializing lemmas from " + path);
        Map<String, String> lemmas = new TreeMap<>();
        BufferedReader in;
        try {
            FileInputStream inputFile = new FileInputStream(path);
            // InputStreamReader ir = new InputStreamReader(inputFile,
            // "ISO-8859-1");
            InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
            in = new BufferedReader(ir);
            String str_line;
            StringTokenizer strk;
            while ((str_line = in.readLine()) != null) {
                strk = new StringTokenizer(str_line, "\t");
                String lemma = strk.nextToken().replaceAll("[0-9]*", "");
                String inflected = strk.nextToken().replaceAll("[0-9]*", "");
                String existing = lemmas.get(inflected);
                if (existing == null || lemma.length() < existing.length()) {
                    lemmas.put(inflected, lemma);
                }
                if (existing != null) {
                    logger.severe("Duplicate entry: " + inflected);
                }
            }
            in.close();
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        return lemmas;
    }

    public static void writeLemmas(String fileName, Map<String, String> lemmas)
            throws FileNotFoundException, UnsupportedEncodingException {
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"))) {
            for (Map.Entry<String, String> e : lemmas.entrySet()) {
                out.println(e.getValue() + "\t" + e.getKey());
            }
        }
    }

    public static String lemmaStatic(String w, Lang lang) {
        String lemma;
        switch (lang) {
            case en:
                lemma = getLemmasEn().get(w);
                break;
            case fr:
                lemma = StaticLemmatizerPOS.lemmaStatic(w, null, Lang.fr);
                break;
            case it:
                lemma = StaticLemmatizerPOS.lemmaStatic(w, null, Lang.it);
                break;
            case ro:
                lemma = getLemmasRo().get(w);
                break;
            case es:
                lemma = getLemmasEs().get(w);
                break;
            case nl:
                lemma = getLemmasNl().get(w);
                break;
            case la:
                lemma = getLemmasLa().get(w);
                break;
            default:
                lemma = null;
        }

        if (lemma != null) {
            return lemma;
        }
        return w;
    }

    public static Map<String, String> getLemmasEn() {
        if (lemmas_en == null) {
            lemmas_en = initialize("resources/config/EN/word lists/lemmas_en.txt", Lang.en);
        }
        return lemmas_en;
    }

    public static Map<String, String> getLemmasRo() {
        if (lemmas_ro == null) {
            lemmas_ro = initialize("resources/config/RO/word lists/lemmas_ro.txt", Lang.ro);
        }
        return lemmas_ro;
    }

    public static Map<String, String> getLemmasEs() {
        if (lemmas_es == null) {
            lemmas_es = initialize("resources/config/ES/word lists/lemmas_es.txt", Lang.es);
        }
        return lemmas_es;
    }

    public static Map<String, String> getLemmasNl() {
        if (lemmas_nl == null) {
            lemmas_nl = initialize("resources/config/NL/word lists/lemmas_nl.txt", Lang.nl);
        }
        return lemmas_nl;
    }

    public static Map<String, String> getLemmasLa() {
        if (lemmas_la == null) {
            lemmas_la = initialize("resources/config/LA/word lists/lemmas_la.txt", Lang.la);
        }
        return lemmas_la;
    }
}
