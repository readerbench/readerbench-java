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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Class containing all pronoun lists for supported languages that makes use of
 * lazy initialization
 *
 * @author Mihai Dascalu
 */
public class Pronouns {

    private static final String PROPERTY_PRONOUNS_NAME = "PRONOUNS_%s_PATH";
    private static final Properties PROPERTIES = ReadProperty.getProperties("paths.properties");
    private static final Map<Lang, ClassesOfWords> PRONOUNS = new TreeMap<>();
    private static final List<Lang> SUPPORTED_LANGUAGES = Arrays.asList(Lang.en, Lang.fr, Lang.la, Lang.nl, Lang.ro);

    public static boolean isConnective(String s, Lang lang) {
        if (lang == null || (!SUPPORTED_LANGUAGES.contains(lang))) {
            return false;
        }
        ClassesOfWords pronouns = getPronouns(lang);
        if (pronouns == null) {
            return false;
        }
        return pronouns.getAllWords().contains(s);
    }

    public static ClassesOfWords getPronouns(Lang lang) {
        if (!SUPPORTED_LANGUAGES.contains(lang)) {
            return null;
        }
        if (PRONOUNS.containsKey(lang)) {
            return PRONOUNS.get(lang);
        }
        ClassesOfWords pronouns = new ClassesOfWords(PROPERTIES.getProperty(String.format(PROPERTY_PRONOUNS_NAME, lang.name().toUpperCase())));

        PRONOUNS.put(lang, pronouns);
        return pronouns;
    }
}
