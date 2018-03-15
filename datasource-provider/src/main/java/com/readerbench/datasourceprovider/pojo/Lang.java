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
package com.readerbench.datasourceprovider.pojo;

import java.io.Serializable;
import java.util.Locale;

public enum Lang implements Serializable {
    en("English", Locale.ENGLISH),
    fr("French", Locale.FRENCH),
    it("Italian", Locale.ENGLISH),
    ro("Romanian", Locale.ENGLISH),
    es("Spanish", new Locale("es", "ES")),
    de("German", Locale.ENGLISH),
    nl("Dutch", Locale.ENGLISH),
    la("Latin", Locale.ENGLISH);

    private final String description;
    private final Locale defaultLocale;

    private Lang(String description, Locale locale) {
        this.description = description;
        this.defaultLocale = locale;
    }

    public String getDescription() {
        return description;
    }

    public Locale getLocale() {
        return defaultLocale;
    }

    public static Lang getLang(String language) {
        for (Lang l : Lang.values()) {
            if (l.getDescription().equals(language)) {
                return l;
            }
        }

        try {
            Lang l = Lang.valueOf(language);
            return l;
        } catch (IllegalArgumentException ex) {
            //default English
            return Lang.en;
        }
    }
}
