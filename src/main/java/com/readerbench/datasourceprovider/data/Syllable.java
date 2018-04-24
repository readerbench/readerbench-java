/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.datasourceprovider.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author admin_licenta
 */
public class Syllable implements Serializable, Comparable<Syllable> {
    private final String text;
    private final List<String> symbols;
    private final boolean primaryStressed;
    private final boolean secondaryStressed;
    
    public Syllable(String syll) {
        text = syll;
        symbols = new ArrayList<>(Arrays.asList(syll.trim().split("\\s+")));
        primaryStressed = syll.contains("1");
        secondaryStressed = syll.contains("2");
    }
    
    public String getText() {
        return text;
    }
    
    public List<String> getSymbols() {
        return symbols;
    }

    public boolean isPrimaryStressed() {
        return primaryStressed;
    }
    
    public boolean isSecondaryStressed() {
        return secondaryStressed;
    }

    @Override
    public String toString() {
        return symbols.stream().map(Object::toString).collect(Collectors.joining(" ")).trim();
    }
    
    @Override
    public int compareTo(Syllable s) {
        return text.compareTo(s.getText());
    }
    
    public static void main(String[] args) {
        String syll1 = "AE1 ";
        String syll2 = "B AH0 ";
        String syll3 = "K AH0 S";
        
        Syllable s1 = new Syllable(syll1);
        System.out.println(s1.getSymbols());
        System.out.println(s1.toString().length());
        Syllable s2 = new Syllable(syll2);
        System.out.println(s2.getSymbols());
        System.out.println(s2.toString().length());
        Syllable s3 = new Syllable(syll3);
        System.out.println(s3.getSymbols().size());
        System.out.println(s3.isPrimaryStressed());
    }
}
