/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.textualcomplexity.rhythm;

import com.readerbench.coreservices.rhythm.Syllable;
import com.readerbench.datasourceprovider.data.Word;
import com.readerbench.datasourceprovider.pojo.Lang;
import com.readerbench.textualcomplexity.rhythm.tools.CMUDict;
import com.readerbench.coreservices.rhythm.SyllabifiedCMUDict;
import com.readerbench.coreservices.nlp.wordlists.StopWords;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 *
 * @author valentin.sergiu.cioaca@gmail.com
 */

class Triplet<T, U, V> {
    private final T first;
    private final U second;
    private final V third;
    
    public Triplet(T first, U second, V third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }
    
    public T getFirst() { return first; }
    public U getSecond() { return second; }
    public V getThird() { return third; }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Triplet)) {
            return false;
        }
        Triplet<?, ?, ?> p = (Triplet<?, ?, ?>) o;
        return first.equals(p.getFirst()) &&
               second.equals(p.getSecond())&&
               third.equals(p.getThird());
    }
    
    private static boolean equals(Object x, Object y) {
        return (x == null && y == null) || (x != null && x.equals(y));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 31 * hash + Objects.hashCode(this.first);
        hash = 31 * hash + Objects.hashCode(this.second);
        hash = 31 * hash + Objects.hashCode(this.third);
        return hash;
    }
}

public class Rhythm {
    
    public static double calculateRhythm(List<Word> unit, CMUDict cmuDictEN) {
        List<Integer> strs = new ArrayList<>();
        List<Integer> syll = new ArrayList<>();
        int nsyl = 0;
        
        for (Word w : unit) {
            String s = w.getText().toLowerCase();
            System.out.println("Text: " + s);
            List<String> pron = cmuDictEN.getDict().get(s);
            System.out.println("Pron: " + pron);
            nsyl = 0;
            for (String phoneme : pron) {
                char last = phoneme.charAt(phoneme.length() - 1);
                if (Character.isDigit(last)) {
                    if (StopWords.isStopWord(s, Lang.en)) {
                        strs.add(0);
                    } else {
                        strs.add(Character.getNumericValue(last));
                    }
                    nsyl += 1;
                }
            }
            syll.add(nsyl);
        }
        
        int lw = unit.size();
        int lstruct = 0;
        lstruct = strs.stream().filter((s) -> (s == 1)).map((_item) -> 1).reduce(lstruct, Integer::sum);
        
        return 1.0 * lw / lstruct;
    }
    
    public static double calculateRhythm2(List<String> unit, CMUDict cmuDictEN,
            List<Integer> syllables, List<String> stresses, List<String> phonemes) {

        List<Integer> syll = new ArrayList<>();
        List<Character> strs = new ArrayList<>();
        List<String> phon = new ArrayList<>();
        int nsyl;
        
//        List<> strss = new ArrayList<>();

        for (String word : unit) {
            nsyl = 0;
            try {
//                System.out.println(word);
                List<String> pron = cmuDictEN.getDict().get(word);
//                System.out.println(pron);
                for (String phoneme : pron) {
                    char last = phoneme.charAt(phoneme.length() - 1);
                    if (Character.isDigit(last)) {
                        if (StopWords.isStopWord(word, Lang.en)) {
//                          System.out.println("Stop word: "+ word);
//                            System.out.println("Stress: 0");
                            strs.add('0');
                        } else {
//                            System.out.println("Stress: " + last);
                            strs.add(last);
                        }
                        nsyl += 1;
                    }
                }
                syll.add(nsyl);
            } catch (NullPointerException e) {
                if (Pattern.matches("\\p{Punct}", word)) {
                    strs.add('p');
                } else {
                    strs.add('u');
                    syll.add(1);
                }
            }
        }
        syllables.addAll(syll);
//        System.out.println(strs);
        int lw = unit.size();
//        System.out.println(lw);
        int lstruct = 0;
        for (char c : strs) {
            if (c == '1')
                lstruct++;
        }
//        System.out.println(lstruct);
        double k_real;
        try {
            k_real =  1.0 * lw / lstruct;
        } catch (ArithmeticException e) {
            k_real = lw + 1;
        }

        return k_real;
    }
    
    public static int calculateRhythmIndexSM(List<Word> unit, SyllabifiedCMUDict syllabifiedCMUDict) {
        List<Integer> rhythmicStructure = new ArrayList<>();
        int cnt = 1;
        
        for (Word w : unit) {
            List<com.readerbench.datasourceprovider.data.Syllable> syllables = w.getSyllables();
            if (null == syllables) {
                // case when the word was not found in the dictionary
                // count the number of syllables using Syllable.syllable() method
                // the word is considered to be unstressed
                cnt += Syllable.syllable(w.getText());
            } else {
                for (com.readerbench.datasourceprovider.data.Syllable syll : syllables) {
                    if (syll.isPrimaryStressed()) {
                        rhythmicStructure.add(cnt);
                        cnt = 1;
                    } else {
                        cnt++;
                    }
                }
            }
        }        
        int rhythmicLength = rhythmicStructure.size();
        int unitLength = unit.size();
        int rhythmicIndex;
        try {
            // only for positive numbers
            rhythmicIndex = (unitLength + rhythmicLength - 1) / rhythmicLength;
        } catch (ArithmeticException e) {
            rhythmicIndex = rhythmicLength + 1;
        }
//        System.out.println();
//        System.out.println("Rhythmic length: " + rhythmicLength);
//        System.out.println("Unit length: " + unitLength);
//        System.out.println("Rhythmic index: " + rhythmicIndex);
        
        // encode rhythmic index to include superior and inferior limit (maybe)
        return rhythmicIndex;
    }
}
