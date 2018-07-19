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
import java.util.TreeMap;

/**
 *
 * @author Mihai Dascalu
 */
public class StopWords {

    private static final String PROPERTY_STOPWORS_NAME = "STOPWORDS_%s_PATH";
    private static final Properties PROPERTIES = ReadProperty.getProperties("paths.properties");
    public static Map<Lang, ListOfWords> STOPWORDS = new TreeMap<>();

    public static boolean isStopWord(String s, Lang lang) {
        if (lang == null) {
            return false;
        }
        if (!STOPWORDS.containsKey(lang)) {
            ListOfWords stopwords = new ListOfWords(PROPERTIES.getProperty(String.format(PROPERTY_STOPWORS_NAME, lang.name().toUpperCase())));
            STOPWORDS.put(lang, stopwords);
        }
        return STOPWORDS.get(lang).getWords().contains(s);
    }
}
