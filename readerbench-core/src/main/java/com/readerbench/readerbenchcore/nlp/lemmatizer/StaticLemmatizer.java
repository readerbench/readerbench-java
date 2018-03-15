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

import java.io.*;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class StaticLemmatizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaticLemmatizer.class);

    private static Map<String, String> lemmas_en;
    private static Map<String, String> lemmas_ro;
    private static Map<String, String> lemmas_nl;
    private static Map<String, String> lemmas_la;

    private static Map<String, String> initialize(String path, Lang lang) {
        LOGGER.info("Initializing lemmas from {} ...", path);
        Map<String, String> lemmas = new TreeMap<>();
        BufferedReader in;
        String str_line = null;
        try {
            FileInputStream inputFile = new FileInputStream(path);
            // InputStreamReader ir = new InputStreamReader(inputFile,
            // "ISO-8859-1");
            InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
            in = new BufferedReader(ir);
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
                    LOGGER.error("Duplicate entry: {}", inflected);
                }
            }
            in.close();
        } catch (IOException e) {
            LOGGER.info("Error processing line: {}", str_line);
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
                lemma = StaticLemmatizerPOS.lemmaStatic(w, null, Lang.es);
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

    public synchronized static Map<String, String> getLemmasEn() {
        if (lemmas_en == null) {
            lemmas_en = initialize("resources/config/EN/word lists/lemmas_en.txt", Lang.en);
        }
        return lemmas_en;
    }

    public synchronized static Map<String, String> getLemmasRo() {
        if (lemmas_ro == null) {
            lemmas_ro = initialize("resources/config/RO/word lists/lemmas_ro.txt", Lang.ro);
        }
        return lemmas_ro;
    }

    public synchronized static Map<String, String> getLemmasNl() {
        if (lemmas_nl == null) {
            lemmas_nl = initialize("resources/config/NL/word lists/lemmas_nl.txt", Lang.nl);
        }
        return lemmas_nl;
    }

    public synchronized static Map<String, String> getLemmasLa() {
        if (lemmas_la == null) {
            lemmas_la = initialize("resources/config/LA/word lists/lemmas_la.txt", Lang.la);
        }
        return lemmas_la;
    }
}
