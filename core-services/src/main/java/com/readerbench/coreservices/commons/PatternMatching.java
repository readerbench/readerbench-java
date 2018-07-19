package com.readerbench.coreservices.commons;

import com.readerbench.coreservices.nlp.wordlists.ListOfWords;
import com.readerbench.datasourceprovider.commons.ReadProperty;
import com.readerbench.datasourceprovider.pojo.Lang;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dorinela Sirbu <dorinela.92@gmail.com>
 */
public class PatternMatching {

    private static final Logger LOGGER = LoggerFactory.getLogger(PatternMatching.class);
    private static final String PROPERTY_CAUSALITY_NAME = "CAUSALITY_%s_PATH";
    private static final String PROPERTY_METACOGNITION_NAME = "METACOGNITION_%s_PATH";
    private static final Properties PROPERTIES = ReadProperty.getProperties("paths.properties");
    
    private static final Map<Lang, ListOfWords> PATTERNS_CAUSALITY = new HashMap<>();
    private static final Map<Lang, ListOfWords> PATTERNS_METACOGNITION = new HashMap<>();

    public static ListOfWords getCausality(Lang lang) {
        if (PATTERNS_CAUSALITY.get(lang) == null) {
            PATTERNS_CAUSALITY.put(lang, new ListOfWords(PROPERTIES.getProperty(String.format(PROPERTY_CAUSALITY_NAME, lang.name().toUpperCase()))));
        }
        return PATTERNS_CAUSALITY.get(lang);
    }

    public static ListOfWords getMetacognition(Lang lang) {
        if (PATTERNS_METACOGNITION.get(lang) == null) {
            PATTERNS_METACOGNITION.put(lang, new ListOfWords(PROPERTIES.getProperty(String.format(PROPERTY_METACOGNITION_NAME, lang.name().toUpperCase()))));
        }
        return PATTERNS_METACOGNITION.get(lang);
    }

}
