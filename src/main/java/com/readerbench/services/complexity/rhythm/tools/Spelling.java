/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.services.complexity.rhythm.tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * based on an algorithm by Peter Norvig
 */
public class Spelling {
    private final HashMap<String, Integer> nWords = new HashMap<>();

    public Spelling(String file) {
        try {
            FileReader fr = new FileReader(file);
            try (BufferedReader in = new BufferedReader(fr)) {
                Pattern p = Pattern.compile("\\w+");
                for(String temp = ""; temp != null; temp = in.readLine()){
                    Matcher m = p.matcher(temp.toLowerCase());
                    while(m.find()) nWords.put((temp = m.group()), nWords.containsKey(temp) ? nWords.get(temp) + 1 : 1);
                }
                in.close();
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private ArrayList<String> edits(String word) {
        ArrayList<String> result = new ArrayList<>();
        for(int i=0; i < word.length(); ++i) result.add(word.substring(0, i) + word.substring(i+1));
        for(int i=0; i < word.length()-1; ++i) result.add(word.substring(0, i) + word.substring(i+1, i+2) + word.substring(i, i+1) + word.substring(i+2));
        for(int i=0; i < word.length(); ++i) for(char c='a'; c <= 'z'; ++c) result.add(word.substring(0, i) + String.valueOf(c) + word.substring(i+1));
        for(int i=0; i <= word.length(); ++i) for(char c='a'; c <= 'z'; ++c) result.add(word.substring(0, i) + String.valueOf(c) + word.substring(i));
        return result;
    }

    public final String correct(String word) {
        if(nWords.containsKey(word)) return word;
        ArrayList<String> list = edits(word);
        HashMap<Integer, String> candidates = new HashMap<>();
        for(String s : list) if(nWords.containsKey(s)) candidates.put(nWords.get(s),s);
        if(candidates.size() > 0) return candidates.get(Collections.max(candidates.keySet()));
        for(String s : list) for(String w : edits(s)) if(nWords.containsKey(w)) candidates.put(nWords.get(w),w);
        return candidates.size() > 0 ? candidates.get(Collections.max(candidates.keySet())) : word;
    }
    
    public static void main(String args[]) {
        String bigTxtPath = "resources/config/EN/word lists/big.txt";
        Spelling spell = new Spelling(bigTxtPath);
        System.out.println(spell.correct("yout"));
    }
}
