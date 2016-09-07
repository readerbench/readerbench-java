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
package utils.localization;

import data.Lang;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import org.openide.util.Exceptions;

public class LocalizationUtils {

    public static final int TITLE = 0;
    public static final int TEXT = 1;

    public static void saveLocaleInBundle(Locale locale) {
        String localeToSave = "";
        for (Lang l : Lang.values()) {
            if (l.getLocale().equals(locale)) {
                localeToSave = l.getDescription();
            }
        }

        try {
            Properties props;
            try (FileInputStream in = new FileInputStream("src/main/resources/utils/localization/settings.properties")) {
                props = new Properties();
                props.load(in);
            }

            try (FileOutputStream out = new FileOutputStream("src/main/resources/utils/localization/settings.properties")) {
                props.setProperty("Language", localeToSave);
                props.store(out, null);
            }

            Locale.setDefault(locale);
            ResourceBundle.clearCache();
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    public static String getLocalizedString(Class<?> inClass, int type, String key) {
        if (key == null || key.length() == 0) {
            return "";
        }

        String fullKey = inClass.getSimpleName() + "." + key + ".";
        switch (type) {
            case TITLE:
                fullKey += "title";
                break;
            case TEXT:
                fullKey += "text";
                break;
            default:
                break;
        }

        String text = ResourceBundle.getBundle("utils.localization.messages").getString(fullKey);
        if (text == null || text.length() == 0) {
            return key;
        }
        return text;
    }

    public static String getTranslation(String text) {
        try {
            return ResourceBundle.getBundle("utils.localization.translations").getString(text);
        } catch (Exception ex) {
            return null;
        }
    }
}
