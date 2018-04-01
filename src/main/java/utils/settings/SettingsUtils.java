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
package utils.settings;

import data.Lang;
import java.util.ResourceBundle;

public class SettingsUtils {
    
    public static String getReaderBenchVersion() {
        String text = ResourceBundle.getBundle("utils.localization.settings").getString("ReaderBenchVersion");
        if (text == null || text.length() == 0) {
            return "";
        }
        return text;
    }
    
    public static Lang getReaderBenchRungimeLanguage() {
        String text = ResourceBundle.getBundle("utils.localization.settings").getString("Language");
        if (text == null || text.length() == 0) {
            return Lang.en;
        }
        return Lang.getLang(text);
    }
}
