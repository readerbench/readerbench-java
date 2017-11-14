/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.complexity.rhythm.tools;

import data.Lang;
import data.Syllable;
import data.Word;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import services.nlp.listOfWords.StopWords;

/**
 *
 * @author valentin.sergiu.cioaca@gmail.com
 */
public class RhythmTool {
    public static final int UNDEFINED = -1;
    public static final int MIN_SIZE = 3;
    public static final int DELTA_RHYTHM = 4;
    
    public static List<Integer> getRhythmicStructureSM(List<Word> unit) {
//        System.out.println("Unit: " + unit);
        List<Integer> rhythmicStructure = new ArrayList<>();
        if (unit.isEmpty()) {
            return rhythmicStructure;
        }
        int cnt = 0;
        for (Word w : unit) {
            if (StopWords.isStopWord(w.getText().toLowerCase(), Lang.en)) {
//                System.out.println("Stop word: " + w.getText() + " " +
//                        services.complexity.readability.Syllable.syllable(w.getText()));
                cnt += services.complexity.readability.Syllable.syllable(w.getText());
                continue;
            }
            List<Syllable> syllables = w.getSyllables();
//            System.out.println("Word: " + w.getText() + " " + syllables);
            if (syllables == null) {
                // case when the word was not found in the dictionary
                // count the number of syllables using Syllable.syllable() method
                // the word is considered to be unstressed
//                System.out.println("NULL: " + services.complexity.readability.Syllable.syllable(w.getText()));
                cnt += services.complexity.readability.Syllable.syllable(w.getText());
            } else {
                for (Syllable syll : syllables) {
                    if (syll.isPrimaryStressed()) {
                        rhythmicStructure.add(++cnt);
                        cnt = 0;
                    } else {
                        cnt++;
                    }
                }
            }
        }
        if (cnt != 0) {
            rhythmicStructure.add(cnt);
        }
//        System.out.println("Rhythmic structure: " + rhythmicStructure);
//        System.out.println();
        return rhythmicStructure;
//        return rhythmicStructure.stream().mapToInt(i->i).toArray();
    }
    
    public static int calcRhythmicIndexSM(List<Word> unit) {
        int unitLength = unit.size();
        int rhythmicLength = getRhythmicStructureSM(unit).size();
        if (unitLength == 0 || rhythmicLength == 0)
            return UNDEFINED;
//        System.out.println("Rhythmic Length: " + rhythmicLength);
//        System.out.println("Unit Lengthm: " + unitLength);
        return Math.max((int) Math.ceil(1.0 * unitLength / rhythmicLength),
                        (int) Math.ceil(1.0 * rhythmicLength / unitLength));
    }
    
    public static int calcRhythmicIndexSM(int unitLength, int rhythmicLength) {
        if (unitLength == 0 || rhythmicLength == 0)
            return UNDEFINED;
        return Math.max((int) Math.ceil(1.0 * unitLength / rhythmicLength),
                        (int) Math.ceil(1.0 * rhythmicLength / unitLength));
    }
    
    public static List<Integer> getNumericalRepresentation(List<Word> unit) {
//        System.out.println("Unit: " + unit);
        List<Integer> numRepresentation = new ArrayList<>();
        if (unit.isEmpty()) {
            return numRepresentation;
        }
        int cnt = 0;
        
        for (int i = 0; i < unit.size(); i++) {
            Word w = unit.get(i);
            if (StopWords.isStopWord(w.getText().toLowerCase(), Lang.en)) {
                if (i == 0) {
                    // marks the atonic syllable
                    numRepresentation.add(0);
                }
//                System.out.println("Stop word: " + w.getText() + " " +
//                        services.complexity.readability.Syllable.syllable(w.getText()));
                cnt += services.complexity.readability.Syllable.syllable(w.getText());
                continue;
            }
            List<Syllable> syllables = w.getSyllables();
//            System.out.println("Word: " + w.getText() + " " + syllables);
            if (syllables == null) {
                if (i == 0) {
                    // marks the atonic syllable
                    numRepresentation.add(0);
                }
//                System.out.println("NULL: " + services.complexity.readability.Syllable.syllable(w.getText()));
                cnt += services.complexity.readability.Syllable.syllable(w.getText());
            } else {
                for (int j = 0; j < syllables.size(); j++) {
                    if (syllables.get(j).isPrimaryStressed()) {
                        if (cnt != 0) {
                            numRepresentation.add(cnt);
                        }
                        cnt = 1;
                    } else {
                        if (i == 0 && j == 0) {
                            // marks the atonic syllable
                            numRepresentation.add(0);
                        }
                        cnt++;
                    }
                }
            }
        }
        if (cnt != 0) {
            numRepresentation.add(cnt);
        }
//        System.out.println("Numerical representation: " + numRepresentation);
//        System.out.println();
        return numRepresentation;
    }
    
    public static List<Integer> testNewUnitDefinition(List<String> unit) {
//        System.out.println("Unit: " + unit);
        List<Integer> numRepresentation = new ArrayList<>();
        if (unit.isEmpty()) {
            return numRepresentation;
        }
        int cnt = 0;
        
        for (int i = 0; i < unit.size(); i++) {
            String w = unit.get(i);
            if (StopWords.isStopWord(w.toLowerCase(), Lang.en)) {
                if (i == 0) {
                    // marks the atonic syllable
                    numRepresentation.add(0);
                }
//                System.out.println("Stop word: " + w + " " +
//                        services.complexity.readability.Syllable.syllable(w));
                cnt += services.complexity.readability.Syllable.syllable(w);
                continue;
            }
            List<Syllable> syllables = SyllabifiedCMUDict.getInstance().getDict().get(w.toLowerCase());
//            System.out.println("Word: " + w + " " + syllables);
            if (syllables == null) {
                if (i == 0) {
                    // marks the atonic syllable
                    numRepresentation.add(0);
                }
//                System.out.println("NULL: " + services.complexity.readability.Syllable.syllable(w));
                cnt += services.complexity.readability.Syllable.syllable(w);
            } else {
                for (int j = 0; j < syllables.size(); j++) {
                    if (syllables.get(j).isPrimaryStressed()) {
                        if (cnt != 0) {
                            numRepresentation.add(cnt);
                        }
                        cnt = 1;
                    } else {
                        if (i == 0 && j == 0) {
                            // marks the atonic syllable
                            numRepresentation.add(0);
                        }
                        cnt++;
                    }
                }
            }
        }
        if (cnt != 0) {
            numRepresentation.add(cnt);
        }
//        System.out.println("Numerical representation: " + numRepresentation);
//        System.out.println();
        return numRepresentation;        
    }
    
    public static int calcDeviations(List<Integer> repr) {
        if (repr == null) {
            return 0;
        }
        int nrDeviations = 0;
        int n = repr.size();
        
        for (int i = 0; i < n-1; i++) {
            if (i == 0 && repr.get(i) == 0) {
                i += 1;
                continue;
            }
            if (repr.get(i) == 1)
                nrDeviations++;
        }
    
        return nrDeviations;
    }
    
    public static int getDominantIndex(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            return -1;
        }
        int index, maxVal, maxNeighSum;
        
        index = 0;
        maxVal = values.get(0);
        maxNeighSum = (values.size() > 1) ? values.get(1) : 0;
        for (int i = 1; i < values.size(); i++) {
            if (values.get(i) > maxVal) {
                maxVal = values.get(i);
                index = i;
                maxNeighSum = values.get(i-1);
                maxNeighSum += (i < values.size()-1) ? values.get(i+1) : 0; 
            } else if (values.get(i) == maxVal) {
                int neighSum = values.get(i-1);
                neighSum += (i < values.size()-1) ? values.get(i+1) : 0; 
                if (neighSum > maxNeighSum) {
                    index = i;
                    maxNeighSum = neighSum;
                }
            }
        }
        
        return index;
          
    }
    
    public static List<List<String>> findAlliterations2(List<List<String>> units) {
        List<List<String>> alliterations = new ArrayList<>();
        
        Map<String, Map<Integer, String>> map = new HashMap<>();
        int count = 0;
        for (List<String> unit : units) {
            for (int i = 0; i < unit.size(); i++) {
                String w = unit.get(i).toLowerCase();
                if (StopWords.isStopWord(w, Lang.en)) {
                    count++;
                    continue;
                }
                List<Syllable> sylls = SyllabifiedCMUDict.getInstance().getDict().get(w.toLowerCase());
                if (sylls == null) continue;
                for (Syllable s : sylls) {
                    if (!s.isPrimaryStressed()) {
                        continue;
                    }
                    if (s.getSymbols().size() > 1 && s.getText().contains("1")) {
                        String c = s.getSymbols().get(0);
                        if (!map.containsKey(c))
                            map.put(c, new TreeMap<>());
                        map.get(c).put(count, w);
                    }
                }
                count++;
            }
        }
        
        System.out.println(map);
        
        for (Map.Entry<String, Map<Integer, String>> entry : map.entrySet()) {
            Map<Integer, String> posMap = entry.getValue();
            if (posMap.size() < 3) continue;
            List<Integer> pos = posMap.keySet().stream()
                .collect(Collectors.toList());
            System.out.println("Pos: " + pos);
            List<Integer> ind = new ArrayList<>();
            ind.add(pos.get(0));
            System.out.println("Ind: " + ind);
            for (int i = 1; i < pos.size(); i++) {
                if (pos.get(i) - ind.get(ind.size()-1) <= 3) {
                    ind.add(pos.get(i));
                } else {
                    if (ind.size() >= 3) {
                        List<String> alliteration =  new ArrayList<>();
                        for (Integer k : ind) {
                            alliteration.add(posMap.get(k));
                        }
                        alliterations.add(alliteration);
                        System.out.println(alliteration);
                    }
                    ind = new ArrayList<>();
                    ind.add(pos.get(i));
                }
                System.out.println("ind_x: " + ind);
            }
            if (ind.size() >= 3) {
                List<String> alliteration =  new ArrayList<>();
                for (Integer k : ind) {
                    alliteration.add(posMap.get(k));
                }
                alliterations.add(alliteration);
                System.out.println(alliteration);
            }
        }
        return alliterations;
    }
    
    // better than second version
    public static Map<Integer, List<List<String>>> findAlliterations(List<List<String>> units) {
        Map<Integer, List<List<String>>> alliterations = new TreeMap<>();
        
        int index = 1;
        for (List<String> unit : units) {
            Map<String, Map<Integer, String>> map = new HashMap<>();
            for (int i = 0; i < unit.size(); i++) {
                String w = unit.get(i).toLowerCase();
                if (StopWords.isStopWord(w, Lang.en)) {
                    continue;
                }
                List<Syllable> sylls = SyllabifiedCMUDict.getInstance().getDict().get(w);
                if (sylls == null) continue;
                String c = sylls.get(0).getSymbols().get(0);
                if (!map.containsKey(c))
                    map.put(c, new TreeMap<>());
                map.get(c).put(i, w);
//                for (Syllable s : sylls) {
//                    if (!s.isPrimaryStressed()) {
//                        continue;
//                    }
//                    if (s.getSymbols().size() > 1 && !s.getSymbols().get(0).contains("1")) {
//                        String c = s.getSymbols().get(0);
//                        if (!map.containsKey(c))
//                            map.put(c, new TreeMap<>());
////                        if (!map.get(c).values().contains(w))
//                        map.get(c).put(i, w);
//                    }
//                }
            }
//            System.out.println(map);
            // get alliteration if exists
            alliterations.put(index, new ArrayList<>());
            for (Map.Entry<String, Map<Integer, String>> entry : map.entrySet()) {
                Map<Integer, String> posWords = entry.getValue();
                if (posWords.size() < MIN_SIZE) continue;
                List<Integer> pos = posWords.keySet().stream().collect(Collectors.toList());
                List<Integer> ind = new ArrayList<>();
                ind.add(pos.get(0));
                for (int i = 1; i < pos.size(); i++) {
                    if (pos.get(i) - ind.get(ind.size()-1) <= DELTA_RHYTHM) {
                        ind.add(pos.get(i));
                    } else {
                        if (ind.size() >= MIN_SIZE) {
                            List<String> alliteration =  new ArrayList<>();
                            for (Integer k : ind) {
                                if (!alliteration.contains(posWords.get(k)))
                                    alliteration.add(posWords.get(k));
                            }
                            if (alliteration.size() >= MIN_SIZE)
                                alliterations.get(index).add(alliteration);
//                            System.out.println(alliteration);
                        }
                        ind = new ArrayList<>();
                        ind.add(pos.get(i));
                    }
//                    System.out.println("ind_x: " + ind);
                }
                if (ind.size() >= MIN_SIZE) {
                    List<String> alliteration =  new ArrayList<>();
                    for (Integer k : ind) {
                        if (!alliteration.contains(posWords.get(k)));
                        alliteration.add(posWords.get(k));
                    }
                    if (alliteration.size() >= MIN_SIZE) {
                        alliterations.get(index).add(alliteration);
                    }
//                    System.out.println(alliteration);
                }
            }
            index++;
        }
//        System.out.println(alliterations);
        return alliterations;
    }
    
    // better than second version
    public static Map<Integer, List<List<String>>> findAssonances(List<List<String>> units) {
        Map<Integer, List<List<String>>> assonances = new TreeMap<>();
        
        int index = 1;
        for (List<String> unit : units) {
            Map<String, Map<Integer, String>> map = new HashMap<>();
            for (int i = 0; i < unit.size(); i++) {
                String w = unit.get(i).toLowerCase();
                if (StopWords.isStopWord(w, Lang.en)) {
                    continue;
                }
                List<Syllable> sylls = SyllabifiedCMUDict.getInstance().getDict().get(w.toLowerCase());
                if (sylls == null) continue;
                for (Syllable s : sylls) {
                    if (!s.isPrimaryStressed()) {
                        continue;
                    }
                    for (String symbol : s.getSymbols()) {
                        if (Character.isDigit(symbol.charAt(symbol.length()-1))) {
                            String key = symbol.substring(0, symbol.length() - 1);
                            if (!map.containsKey(key)) {
                                map.put(key, new TreeMap<>());
                            }
                            map.get(key).put(i, w);
                        }
                    }
                }
            }
//            System.out.println(map);
            // get alliteration if exists
            assonances.put(index, new ArrayList<>());
            for (Map.Entry<String, Map<Integer, String>> entry : map.entrySet()) {
                Map<Integer, String> posWords = entry.getValue();
                if (posWords.size() < MIN_SIZE) continue;
                List<Integer> pos = posWords.keySet().stream().collect(Collectors.toList());
                List<Integer> ind = new ArrayList<>();
                ind.add(pos.get(0));
                for (int i = 1; i < pos.size(); i++) {
                    if (pos.get(i) - ind.get(ind.size()-1) <= DELTA_RHYTHM) {
                        ind.add(pos.get(i));
                    } else {
                        if (ind.size() >= MIN_SIZE) {
                            List<String> assonance =  new ArrayList<>();
                            for (Integer k : ind) {
                                if (!assonance.contains(posWords.get(k))) {
                                    assonance.add(posWords.get(k));
                                }
                            }
                            if (assonance.size() >= MIN_SIZE) {
                                assonances.get(index).add(assonance);
                            }
                        }
                        ind = new ArrayList<>();
                        ind.add(pos.get(i));
                    }
                }
                if (ind.size() >= MIN_SIZE) {
                    List<String> assonance =  new ArrayList<>();
                    for (Integer k : ind) {
                        if (!assonance.contains(posWords.get(k))) {
                            assonance.add(posWords.get(k));
                        }
                    }
                    if (assonance.size() >= MIN_SIZE) {
                        assonances.get(index).add(assonance);
                    }
                }
            }
            index++;
        }
        return assonances;
//        Map<Integer, List<String>> assonances = new TreeMap<>();
//        List<List<String>> assonances = new ArrayList<>();
//        
//        int index = 1, wordPos = 0;
//        Map<String, Map<Integer, String>> map = new HashMap<>();
//        for (List<String> unit : units) {
//            for (String w : unit) {
//                wordPos++;
////                String w = unit.get(i).toLowerCase();
//                w = w.toLowerCase();
//                if (StopWords.isStopWord(w, Lang.en)) {
//                    continue;
//                }
//                List<Syllable> sylls = SyllabifiedCMUDict.getInstance().getDict().get(w);
//                if (sylls == null) continue;
//                for (Syllable s : sylls) {
//                    if (!s.isPrimaryStressed()) {
//                        continue;
//                    }
//                    for (String symbol : s.getSymbols()) {
//                        if (Character.isDigit(symbol.charAt(symbol.length()-1))) {
//                            String key = symbol.substring(0, symbol.length() - 1);
//                            if (!map.containsKey(key)) {
//                                map.put(key, new TreeMap<>());
//                            }
//                            map.get(key).put(wordPos, w);
//                        }
//                    }
//                }
//            }
//        }
//        System.out.println(map);
//            // get alliteration if exists
////            assonances.put(index, new ArrayList<>());
//        for (Map.Entry<String, Map<Integer, String>> entry : map.entrySet()) {
//            Map<Integer, String> posWords = entry.getValue();
//            if (posWords.size() < 2) continue;
//            List<Integer> pos = posWords.keySet().stream().collect(Collectors.toList());
//            List<Integer> ind = new ArrayList<>();
//            ind.add(pos.get(0));
//            for (int i = 1; i < pos.size(); i++) {
//                if (pos.get(i) - ind.get(ind.size()-1) <= DELTA) {
//                    ind.add(pos.get(i));
//                } else {
//                    if (ind.size() >= 2) {
//                        List<String> assonance =  new ArrayList<>();
//                        for (Integer k : ind) {
//                            if (!assonance.contains(posWords.get(k))) {
//                                assonance.add(posWords.get(k));
//                            }
//                        }
//                        if (assonance.size() >= 2) {
//                            assonances.add(assonance);
////                            assonances.put(index++, assonance);
//                        }
////                        assonances.get(index++).add(assonance);
//////                            System.out.println(alliteration);
//                    }
//                    ind = new ArrayList<>();
//                    ind.add(pos.get(i));
//                    }
//////                    System.out.println("ind_x: " + ind);
//                }
//            if (ind.size() >= 2) {
//                List<String> assonance =  new ArrayList<>();
//                for (Integer k : ind) {
//                    if (!assonance.contains(posWords.get(k))) {
//                        assonance.add(posWords.get(k));
//                    }
//                }
//                if (assonance.size() >= 2) {
//                    assonances.add(assonance);
////                    assonances.put(index++, assonance);
//                }
////                assonances.get(index).add(alliteration);
////                System.out.println(alliteration);
//            }
//        }
////            index++;
////        }
////        System.out.println(alliterations);
//        return assonances;
    }
    
    public static Map<Integer, List<List<String>>> findAssonances2(List<List<String>> units) {
        Map<Integer, List<List<String>>> assonances = new TreeMap<>();
        
        int index = 1;
        for (List<String> unit : units) {
            Map<String, List<String>> map = new HashMap<>();
            for (String w : unit) {
                if (StopWords.isStopWord(w, Lang.en)) {
                    continue;
                }
                List<Syllable> sylls = SyllabifiedCMUDict.getInstance().getDict().get(w.toLowerCase());
                if (sylls == null) continue;
                System.out.println(sylls);
                for (Syllable s : sylls) {
                    for (String symbol : s.getSymbols()) {
                        if (Character.isDigit(symbol.charAt(symbol.length()-1))) {
                            if (symbol.charAt(symbol.length()-1) != '1') {
                                continue;
                            }
                            String key = symbol.substring(0, symbol.length() - 1);
                            if (!map.containsKey(key)) {
                                map.put(key, new ArrayList<>());
                            }
                            if (!map.get(key).contains(w))
                                map.get(key).add(w);
                        }
                    }
                }
            }
            assonances.put(index, new ArrayList<>());
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                if (entry.getValue().size() >= 3)
                    assonances.get(index).add(entry.getValue());
            }
            index++;
        }
        
        return assonances;
    }
    
    public static void updatePhonemes(List<Word> unit, Map<String, Double> phonemes) {
        for (Word w : unit) {
            if (StopWords.isStopWord(w.getText().toLowerCase(), Lang.en)) {
                continue;
            }
            List<Syllable> syllables = w.getSyllables();
            if (syllables == null) continue;
            for (Syllable syll : syllables) {
                syll.getSymbols().forEach((symbol) -> {
                    String phoneme = symbol.replaceAll("\\d", "").toUpperCase();
                    phonemes.put(phoneme,
                    phonemes.containsKey(phoneme) ? phonemes.get(phoneme)+1 : 1);
                });
            }
        }
    }

/* -------------------------------------------------------------------------- */
    // sistemul clasic de notare a silabelor este substituit prin sistemul numeric
    public static String getNumericalSystem(List<Word> unit, Map<String, Integer> phonemesFrequency) {
        SyllabifiedCMUDict dict = SyllabifiedCMUDict.getInstance();
        String SN = "";
        int cnt = 0;
        
        for (int i = 0; i < unit.size(); i++) {
            Word w = unit.get(i);
            List<Syllable> syllables = w.getSyllables();
//            System.out.println(w + " " + syllables);
            if (syllables == null) {
//                System.out.println("not found: " + w + " " + Syllable.syllable(w.getText()));
                if (i == 0) {
                    SN += "!";
                }
                cnt += services.complexity.readability.Syllable.syllable(w.getText());
            } else {
                for (int j = 0; j < syllables.size(); j++) {
                    Syllable syllable = syllables.get(j);
                    syllable.getSymbols().forEach((symbol) -> {
                        String phoneme = symbol.replaceAll("\\d", "");
                        phonemesFrequency.put(phoneme,
                        phonemesFrequency.containsKey(phoneme) ? phonemesFrequency.get(phoneme)+1 : 1);
                    });
                    if (syllable.isPrimaryStressed()) {
                        if (cnt != 0)
                            SN += String.valueOf(cnt);
                        cnt = 1;
                    } else {
                        if (i == 0 && j == 0) {
                            // silaba anota la inceputul versului
                            SN += '!';
                        }
                        cnt++;
                    }
                }
            }
        }
        
        return SN;
//        int NT, NA = 0;
//        NT = (SN.charAt(0) == '!') ? SN.length()-1 : SN.length();
//        for (int i = 0; i < SN.length(); i++) {
//            if (Character.isDigit(SN.charAt(i))) {
//                int syll = Character.getNumericValue(SN.charAt(i));
//                Integer count = syllabicFrequencies.get(syll);
//                syllabicFrequencies.put(syll, (null==count) ? 1 : count+1);
//                NA += syll;
//            }
//        }
//        System.out.println("Sistemul numeric: " + SN);
//        // numar tonic = numarul de accente de intensitate suprinse in fiecare unitate melodica
//        System.out.println("Numar tonic: " + NT);
//        // numarul de silabe cuprinse in cadrul unitatilor melodice
//        System.out.println("Numar aritmetic: " + NA);
        
        // frecventa silabica reprezinta rapotul dintre numarul total al fiecarei cifre
        // din numarul respectiv si totalul unitatilor cifrice (nr. total al segmentelor silabice)
        
        // coeficientul ritmului sau coeficient de ritmicitate = numar de abateri / numar total de aparitii
    }
    
    public static int getArithmeticNumber(String numericSystem, Map<Integer, Integer> cntSyllables) {
        int NA = 0;
        
        for (int i = 0; i < numericSystem.length(); i++) {
            if (Character.isDigit(numericSystem.charAt(i))) {
                int syll = Character.getNumericValue(numericSystem.charAt(i));
                cntSyllables.put(syll, cntSyllables.containsKey(syll) ? cntSyllables.get(syll)+1 : 1);
//                Integer count = cntSyllables.get(syll);
//                cntSyllables.put(syll, (null==count) ? 1 : count+1);
                NA += syll;
            }
        }
        return NA;
    }
    
    // o abatere este considerata si doua silabe accentuate consecutive
    public static int calcPossibleDeviations(String numericSystem) {
        int nrDeviations = 0;
        
        for (int i = 0; i < numericSystem.length() - 1; i++) {
            char c = numericSystem.charAt(i);
            if (Character.isDigit(c) && c == '1') {
                nrDeviations++;
            }
        }
            
        return nrDeviations;
    }
    
    public static String rhythmPattern(List<String> unit) {
        SyllabifiedCMUDict dict = SyllabifiedCMUDict.getInstance();
        String pattern = "";
        
        for (String w : unit) {
            List<Syllable> syllables = dict.getDict().get(w.toLowerCase());
            if (syllables == null) {
                int nrSyll = services.complexity.readability.Syllable.syllable(w.toLowerCase());
                for (int i = 0; i < nrSyll; ++i) {
                    pattern += "x ";
                }
            } else {
                for (Syllable syll : syllables) {
                    if (syll.isPrimaryStressed()) {
                        pattern += "/ ";
                    } else {
                        pattern += "x ";
                    }
                }
            }
        }
        
        return pattern;
    }
/* -------------------------------------------------------------------------- */
    
    public static void main(String[] args) {
//        System.out.println(calcDeviations(Arrays.asList(0,1,1)));
//        System.out.println(calcDeviations(Arrays.asList(1)));
//        System.out.println(calcDeviations(Arrays.asList(0,1,1,2,1)));
//        System.out.println(calcDeviations(Arrays.asList(2,3,2,1,1)));
//        System.out.println(calcDeviations(Arrays.asList(0, 4, 6, 8, 10, 2)));           // 1
//        System.out.println(calcDeviations(Arrays.asList(0, 1, 3, 2, 5, 6, 5, 3)));      // 1
//        System.out.println(calcDeviations(Arrays.asList(0, 7, 6, 3, 4, 7, 2, 1, 7)));   // 2
//        System.out.println(calcDeviations(Arrays.asList(0, 3, 5, 5, 2)));               // 2
//        System.out.println(calcRhythmIndex(0, 1));
//        System.out.println(calcRhythmIndex(1, 0));
//        System.out.println(calcRhythmIndex(0, 0));
//        System.out.println(calcPossibleDeviations("11221"));
//        System.out.println(calcPossibleDeviations("!11111211111112"));
//        System.out.println(getDominantIndex(Arrays.asList(10,10,8,3,1,2)));
//        System.out.println(getDominantIndex(Arrays.asList(7,10,10,8,3,1,2)));
//        System.out.println(getDominantIndex(Arrays.asList(7,10,9,10,8,1,2)));
/**
//        Iamb (x /)
        List<String> iamb = Arrays.asList("Shall", "I", "compare", "thee", "to", "a", "summer", "day");
        System.out.println(rhythmPattern(iamb));
//        Trochee (/ x)
        List<String> trochee = Arrays.asList("Tell", "me", "not", "in", "mournful", "numbers");
        System.out.println(rhythmPattern(trochee));
//        Spondee (/ /)
        List<String> spondee = Arrays.asList("White", "founts", "falling", "in", "the", "Courts", "of", "the", "sun");
        System.out.println(rhythmPattern(spondee));
//        Dactyl (/ x x)
        List<String> dactyl = Arrays.asList("This", "is", "the", "forest", "primeval", "The", "murmuring", "pines", "and", "the", "hemlocks");
        System.out.println(rhythmPattern(dactyl));
//        Anapest (x x /)
        List<String> anapest = Arrays.asList("It", "was", "the", "night", "before", "Christmas", "and", "all", "through", "the", "house");
        System.out.println(rhythmPattern(anapest));
*/        
//        List<String> unit1 = Arrays.asList("before", "you", "discuss", "the", "resolution", ",", "let", "me", "place", "before", "you", "one", "or", "two", "things", ",", "i", "want", "you", "to", "understand", "two", "things", "very", "clearly", "and", "to", "consider", "them", "from", "the", "same", "point", "of", "view", "from", "which", "i", "am", "placing", "them", "before", "you");
//        List<String> unit2 = Arrays.asList("i", "ask", "you", "to", "consider", "it", "from", "my", "point", "of", "view", ",", "because", "if", "you", "approve", "of", "it", ",", "you", "will", "be", "enjoined", "to", "carry", "out", "all", "i", "say");
//        List<String> unit3 = Arrays.asList("it", "will", "be", "a", "great", "responsibility");
//        List<String> unit4 = Arrays.asList("there", "are", "people", "who", "ask", "me", "whether", "i", "am", "the", "same", "man", "that", "i", "was", "in", "1920", ",", "or", "whether", "there", "has", "been", "any", "change", "in", "me");
//        List<String> unit5 = Arrays.asList("you", "are", "right", "in", "asking", "that", "question");
//        System.out.println(rhythmPattern(unit1));
//        System.out.println(rhythmPattern(unit2));
//        System.out.println(rhythmPattern(unit3));
//        System.out.println(rhythmPattern(unit4));
//        System.out.println(rhythmPattern(unit5));

        List<String> unit1 = Arrays.asList("As","I","looked","to","the","east","right","into","the","sun");
        List<String> unit2 = Arrays.asList("I","saw","a","tower","on","a","toft","worthily","built");
        List<String> unit3 = Arrays.asList("A","deep","dale","beneath","a","dungeon","therein");
        List<String> unit4 = Arrays.asList("With","deep","ditches","and","dark","and","dreadful","of","sight");
        List<String> unit5 = Arrays.asList("A","fair","field","full","of","folk","found","I","in","between");
        List<String> unit6 = Arrays.asList("Of","all","manner","of","men","the","rich","and","the","poor");
        List<String> unit7 = Arrays.asList("Working","and","wandering","as","the","world","asketh");
        List<List<String>> units = Arrays.asList(unit1, unit2, unit3, unit4, unit5, unit6, unit7);
        RhythmTool.findAlliterations(units);
        
        List<String> unit11 = Arrays.asList("From","forth","the","fatal","loins","of","these","two","foes");
        List<String> unit22 = Arrays.asList("A","pair","of","star-cross’d","lovers","take","their","life");
        List<List<String>> units1 = Arrays.asList(unit11, unit22);
//        RhythmTool.findAlliterations(units1);

        List<String> asson1 = Arrays.asList("That","solitude","which","suits","abstruser","musings");
//        List<List<String>> target = Arrays.asList(asson1);
//        System.out.println(findAssonances(target));
        List<String> asson2 = Arrays.asList("I","must","confess","that","in","my","quest","I","felt","depressed","and","restless");
        List<List<String>> target = Arrays.asList(asson2);
        List<String> asson3 = Arrays.asList("finally","whether","you","are","citizens","of","America","or","citizens","of","the","world","ask","of","us","here","the","same","high","standards","of","strength","and","sacrifice","which","we","ask","of","you");
//        List<List<String>> target = Arrays.asList(asson3);
//        System.out.println(findAssonances2(target));
//        List<Integer> numbers = Arrays.asList(1,2,1,4,5, 5);
        List<Integer> numbers = new ArrayList<>();
        int maxVal = Collections.max(numbers);
        System.out.println(Collections.frequency(numbers, maxVal));
    }
}
