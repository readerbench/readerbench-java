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

import com.readerbench.datasourceprovider.commons.ReadProperty;
import com.readerbench.datasourceprovider.pojo.Lang;
import java.util.Map;
import java.util.Properties;

import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Mihai Dascalu
 */
public class Dictionary {

    private static final String PROPERTY_DICTIONARY_NAME = "DICTIONARY_%s_PATH";
    private static final Properties PROPERTIES = ReadProperty.getProperties("paths.properties");
    public static Map<Lang, ListOfWords> DICTIONARIES = new TreeMap<>();

    public static Set<String> getDictionaryWords(Lang lang) {
        if (lang == null) {
            return null;
        }
        if (DICTIONARIES.containsKey(lang)) {
            return DICTIONARIES.get(lang).getWords();
        }
        ListOfWords dictionary = new ListOfWords(PROPERTIES.getProperty(String.format(PROPERTY_DICTIONARY_NAME, lang.name().toUpperCase())));
        DICTIONARIES.put(lang, dictionary);
        return dictionary.getWords();
    }

    public static boolean isDictionaryWord(String s, Lang lang) {
        if (lang == null) {
            return true;
        }
        return getDictionaryWords(lang).contains(s);
    }
}
