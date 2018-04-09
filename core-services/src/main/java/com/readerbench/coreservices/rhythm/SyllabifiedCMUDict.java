/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.coreservices.rhythm;

import com.readerbench.datasourceprovider.data.Syllable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author valentin.sergiu.cioaca@gmail.com
 */
public class SyllabifiedCMUDict {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyllabifiedCMUDict.class);

    private static SyllabifiedCMUDict instance = null;
    private String path = "resources/config/EN/word lists/syllabified_cmudict.txt";
    //private Map<String, List<SyllableContainer>> dict;
    private Map<String, List<Syllable>> newDict;
    
    private SyllabifiedCMUDict() {
        LOGGER.info("Loading file {} ...", path);
        newDict = new TreeMap<>();
//        dict = new TreeMap<>();
        
        try {
            FileInputStream inputFile = new FileInputStream(path);
            InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
            try (BufferedReader in = new BufferedReader(ir)) {
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.equals(""))
                	continue;
                    String[] paStrings = line.split("\\s", 2);
                    if (paStrings.length < 2)
                	continue;
                    String key = paStrings[0].toLowerCase();
//                    List<SyllableContainer> value = new ArrayList<>();
                    List<Syllable> value = new ArrayList<>();
                    for (String s : paStrings[1].split("-")) {
//                        value.add(new SyllableContainer(s.toLowerCase()));
                        value.add(new Syllable(s.toLowerCase()));
                    }
//                    dict.put(key, value);
                    newDict.put(key, value);
                }
            }
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
	}
    }
    
    public static SyllabifiedCMUDict getInstance() {
        if (instance == null) {
           instance = new SyllabifiedCMUDict();
        }
        return instance;
    }
    
    public Map<String, List<Syllable>> getDict() {
        return newDict;
    }

//    public Map<String, List<SyllableContainer>> getDict() {
//        return dict;
//    }
    
    public static void main(String[] args) {
        SyllabifiedCMUDict syllabifiedCMUDict = SyllabifiedCMUDict.getInstance();
        List<Syllable> syllables = syllabifiedCMUDict.getDict().get("abacus");
//        List<SyllableContainer> syllables = syllabifiedCMUDict.getDict().get("abacus");
//        System.out.println(syllables + " " + syllables.size());
//        for (Syllable syll : syllables) {
//            System.out.println(syll + " " + syll.getSymbols().size());
//        }
//        for (int i = 0; i < syllables.size(); ++i) {
//            SyllableContainer syllable = syllables.get(i);
//            System.out.println(syllable + " " + syllable.getSymbols().size());
//        }
//        for (List<String> unit : units) {
//            System.out.println(unit);
//            Map<String, List<String>> alliterations = new TreeMap<>();
////            Map<Syllable, String> map = new TreeMap<>();
//            for (String w : unit) {
//                if (StopWords.isStopWord(w.toLowerCase(), Lang.en)) {
//                    continue;
//                }
//                List<Syllable> sylls = SyllabifiedCMUDict.getInstance().getDict().get(w.toLowerCase());
//                if (sylls == null) continue;
//                System.out.print(sylls);
//                for (Syllable s : sylls) {
//                    for (int i = 0; i < s.getSymbols().size(); ++i) {
//                        if (s.getSymbols().get(i).contains("1") && i > 0) {
//                            if (!alliterations.containsKey(s.getSymbols().get(0))) {
//                                alliterations.put(s.getSymbols().get(0), new ArrayList<>());
//                            }
//                            alliterations.get(s.getSymbols().get(0)).add(w);
//                        }
//                    }
////                    if (s.isPrimaryStressed()) {
////                        map.put(s, w);
////                    }
//                }
//            }
//            System.out.println();
////            for (Syllable s : map.keySet()) {
////               System.out.println(s.getSymbols());
////            }
//            System.out.println("Alliterations: " + alliterations);
//            System.out.println();
//        }
    }
}
