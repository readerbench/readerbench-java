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
package com.readerbench.coreservices.nlp.lemmatizer;

import com.readerbench.datasourceprovider.commons.ReadProperty;
import java.util.Properties;
import com.readerbench.datasourceprovider.pojo.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class StaticLemmatizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaticLemmatizer.class);

    private static final Properties PROPERTIES = ReadProperty.getProperties("paths.properties");
    private static final String PROPERTY_LEMMAS_NAME = "LEMMAS_%s_PATH";
    private static final String PROPERTY_LEMMAS_POS_NAME = "LEMMAS_POS_%s_PATH";

    private static final Map<Lang, Map<String, String>> LISTS_OF_LEMMAS = new TreeMap<>();
    private static final List<Lang> LANGUAGES_WITHOUT_POS = Arrays.asList(Lang.en, Lang.ro, Lang.nl, Lang.la);
    private static final List<Lang> LANGUAGES_WITH_POS = Arrays.asList(Lang.fr, Lang.it, Lang.es);

    private static synchronized void initialize(String path, Lang lang) {
        LOGGER.info("Initializing lemmas from {} ...", path);
        Map<String, String> lemmas = new TreeMap<>();
        String str_line;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(StaticLemmatizer.class.getClassLoader().getResourceAsStream(path), "UTF-8"))) {
            StringTokenizer strk;
            while ((str_line = in.readLine()) != null) {
                strk = new StringTokenizer(str_line, "\t");
                String inflected = strk.nextToken().replaceAll("[0-9]*", "").toLowerCase();
                String lemma = strk.nextToken().replaceAll("[0-9]*", "").toLowerCase();
                String existing = lemmas.get(inflected);
                if (existing == null || lemma.length() < existing.length()) {
                    lemmas.put(inflected, lemma);
                }
                if (existing != null) {
                    LOGGER.warn("Duplicate entry: {}", inflected);
                }
            }
            in.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        LISTS_OF_LEMMAS.put(lang, lemmas);
    }

    public static synchronized void writeLemmas(String fileName, Map<String, String> lemmas) throws FileNotFoundException, UnsupportedEncodingException {
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"))) {
            for (Map.Entry<String, String> e : lemmas.entrySet()) {
                out.println(e.getValue() + "\t" + e.getKey());
            }
        }
    }

    public static synchronized String lemmaStatic(String w, Lang lang) {
        String lemma = null;
        if (LANGUAGES_WITHOUT_POS.contains(lang)) {
            if (!LISTS_OF_LEMMAS.containsKey(lang)) {
                initialize(PROPERTIES.getProperty(String.format(PROPERTY_LEMMAS_NAME, lang.name().toUpperCase())), lang);
            }
            lemma = LISTS_OF_LEMMAS.get(lang).get(w);
        }
        if (LANGUAGES_WITH_POS.contains(lang)) {
            if (!LISTS_OF_LEMMAS.containsKey(lang)) {
                initialize(PROPERTIES.getProperty(String.format(PROPERTY_LEMMAS_POS_NAME, lang.name().toUpperCase())), lang);
            }
            lemma = lemmaStaticPOS(w, null, lang);
        }

        if (lemma != null) {
            return lemma;
        }
        return w;
    }

    public static synchronized String lemmaStaticPOS(String word, String pos, Lang lang) {
        String w = word.toLowerCase();
        if (LANGUAGES_WITHOUT_POS.contains(lang) || pos == null) {
            return lemmaStatic(w, lang);
        }
        if (LANGUAGES_WITH_POS.contains(lang)) {
            if (!LISTS_OF_LEMMAS.containsKey(lang)) {
                initialize(PROPERTIES.getProperty(String.format(PROPERTY_LEMMAS_POS_NAME, lang.name().toUpperCase())), lang);
            }
        }
        Map<String, String> lemmas = LISTS_OF_LEMMAS.get(lang);
        if (lemmas == null) {
            return w;
        }
        String lemma = null;
        lemma = lemmas.get((w + "_" + pos).toLowerCase());
        if (lemma != null) {
            return lemma;
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
}
