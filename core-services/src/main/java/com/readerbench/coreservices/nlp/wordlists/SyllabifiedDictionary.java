/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.coreservices.nlp.wordlists;

import com.readerbench.coreservices.data.Syllable;
import com.readerbench.datasourceprovider.commons.ReadProperty;
import com.readerbench.datasourceprovider.pojo.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 *
 * @author valentin.sergiu.cioaca@gmail.com
 */
public class SyllabifiedDictionary {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyllabifiedDictionary.class);
    private static final Properties PROPERTIES = ReadProperty.getProperties("paths.properties");
    private static final String PROPERTY_SYLLABIFIED_DICTIONARY_NAME = "SYLLABIFIED_DICTIONARY_%s_PATH";
    private static final Map<Lang, Map<String, List<Syllable>>> DICTIONARIES = new TreeMap<>();
    private static final List<Lang> SUPPORTED_LANGUAGES = Arrays.asList(Lang.en);

    private static void initialize(String path, Lang lang) {
        LOGGER.info("Loading file {} ...", path);
        Map<String, List<Syllable>> newDict = new TreeMap<>();

        try (BufferedReader in = new BufferedReader(new InputStreamReader(SyllabifiedDictionary.class.getClassLoader().getResourceAsStream(path), "UTF-8"))) {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.equals("")) {
                    continue;
                }
                String[] paStrings = line.split("\\s", 2);
                if (paStrings.length < 2) {
                    continue;
                }
                String key = paStrings[0].toLowerCase();
                List<Syllable> value = new ArrayList<>();
                for (String s : paStrings[1].split("-")) {
                    value.add(new Syllable(s.toLowerCase()));
                }
                newDict.put(key, value);
            }
            DICTIONARIES.put(lang, newDict);
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public static Map<String, List<Syllable>> getDictionary(Lang lang) {
        if (!SUPPORTED_LANGUAGES.contains(lang)) {
            return null;
        }
        if (!DICTIONARIES.containsKey(lang)) {
            initialize(PROPERTIES.getProperty(String.format(PROPERTY_SYLLABIFIED_DICTIONARY_NAME, lang.name().toUpperCase())), lang);
        }
        return DICTIONARIES.get(lang);
    }
}
