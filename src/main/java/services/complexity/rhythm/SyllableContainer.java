/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.complexity.rhythm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author valentin.sergiu.cioaca@gmail.com
 */
public class SyllableContainer {
    private final List<String> symbols;
    private final boolean primaryStressed;
    private final boolean secondaryStressed;
    
    public SyllableContainer(String syll) {
        symbols = new ArrayList<>(Arrays.asList(syll.trim().split("\\s+")));
        String str = syll.replaceAll("\\s+", "");
        primaryStressed = str.contains("1");
        secondaryStressed = str.contains("2");
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
    
    public static void main(String[] args) {
        String syll1 = "AE1 ";
        String syll2 = "B AH0 ";
        String syll3 = "K AH0 S";
        
//        SyllableContainer s1 = new SyllableContainer(syll1);
//        System.out.println(s1.getSymbols());
//        System.out.println(s1);
//        SyllableContainer s2 = new SyllableContainer(syll2);
//        System.out.println(s2.getSymbols());
//        System.out.println(s2);
        SyllableContainer s3 = new SyllableContainer(syll3);
        System.out.println(s3.getSymbols().size());
        System.out.println(s3.isPrimaryStressed());
    }
}
