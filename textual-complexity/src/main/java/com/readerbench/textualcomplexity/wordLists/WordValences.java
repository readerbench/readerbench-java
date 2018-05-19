/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.textualcomplexity.wordLists;

import com.readerbench.coreservices.data.Word;
import com.readerbench.datasourceprovider.commons.ReadProperty;
import com.readerbench.datasourceprovider.pojo.Lang;

import java.io.*;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author stefan
 */
public class WordValences {

    private static final Map<Lang, Map<String, Map<String, Double>>> map = new EnumMap<>(Lang.class);
    private static final Map<Lang, List<String>> valencesForLang  = new EnumMap<>(Lang.class);
    private static final Properties PROPERTIES = ReadProperty.getProperties("textual_complexity_paths.properties");
    private static final String PROPERTY_VALENCES_NAME = "VALENCES_%s_PATH";
    
    private static void initLang(Lang lang) {
        map.put(lang, new HashMap<>());
        String fileName = PROPERTIES.getProperty(String.format(PROPERTY_VALENCES_NAME, lang.name().toUpperCase()));
        try (InputStream input = WordValences.class.getClassLoader().getResourceAsStream(fileName); BufferedReader in = new BufferedReader(new InputStreamReader(input, "UTF-8"))) {
            String header = in.readLine();
            if (header.startsWith("sep")) {
                header = in.readLine();
            }
            String[] splitHeader = header.split(";");
            ArrayList<String> valences = new ArrayList<>();
            for (int i = 1; i < splitHeader.length; i++) {
                valences.add(splitHeader[i]);
            }
            valencesForLang.put(lang, valences);
            String line;
            while ((line = in.readLine()) != null) {
                String[] values = line.split(";");
                Map<String, Double> wordValues = new HashMap<>();
                for (int i = 1; i < values.length; i++) {
                    wordValues.put(splitHeader[i], Double.parseDouble(values[i]));
                }
                map.get(lang).put(values[0], wordValues);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(WordValences.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WordValences.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static Double getValenceForWord(Word word, String valence) {
        if (!map.containsKey(word.getLanguage())) {
            initLang(word.getLanguage());
        }
        return map.get(word.getLanguage())
                .getOrDefault(word.getText(), new HashMap<>())
                .getOrDefault(valence, 0.);
    }
    
    public static List<String> getValences(Lang lang) {
        if (!valencesForLang.containsKey(lang)) {
            initLang(lang);
        }
        return valencesForLang.get(lang);
    }
    
    public static void main(String[] args) {
        System.out.println(getValenceForWord(new Word("hate", null, null, null, null, Lang.en), "Valence_ANEW"));
    }
}
