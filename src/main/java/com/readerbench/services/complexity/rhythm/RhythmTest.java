/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.services.complexity.rhythm;

import com.readerbench.services.complexity.rhythm.tools.CMUDict;
import com.readerbench.services.complexity.rhythm.tools.SyllabifiedCMUDict;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author valentin.sergiu.cioaca@gmail.com
 */
public class RhythmTest {
    public static void main(String[] args) {
        List<String> unit1 = Arrays.asList("before", "you", "discuss", "the", "resolution", ",", "let", "me", "place", "before", "you", "one", "or", "two", "things", ",", "i", "want", "you", "to", "understand", "two", "things", "very", "clearly", "and", "to", "consider", "them", "from", "the", "same", "point", "of", "view", "from", "which", "i", "am", "placing", "them", "before", "you");
        List<String> unit2 = Arrays.asList("i", "ask", "you", "to", "consider", "it", "from", "my", "point", "of", "view", ",", "because", "if", "you", "approve", "of", "it", ",", "you", "will", "be", "enjoined", "to", "carry", "out", "all", "i", "say");
        List<String> unit3 = Arrays.asList("it", "will", "be", "a", "great", "responsibility");
        List<String> unit4 = Arrays.asList("there", "are", "people", "who", "ask", "me", "whether", "i", "am", "the", "same", "man", "that", "i", "was", "in", "1920", ",", "or", "whether", "there", "has", "been", "any", "change", "in", "me");
        List<String> unit5 = Arrays.asList("you", "are", "right", "in", "asking", "that", "question");
        List<String> unit6 = Arrays.asList("let", "me", ",", "however", ",", "hasten", "to", "assure", "that", "i", "am", "the", "same", "gandhi", "as", "i", "was", "in", "1920");
        List<String> unit7 = Arrays.asList("i", "have", "not", "changed", "in", "any", "fundamental", "respect");
        List<String> unit8 = Arrays.asList("i", "attach", "the", "same", "importance", "to", "non-violence", "that", "i", "did", "then");
        List<String> unit9 = Arrays.asList("if", "at", "all", ",", "my", "emphasis", "on", "it", "has", "grown", "stronger");
        List<String> unit10 = Arrays.asList("there", "is", "no", "real", "contradiction", "between", "the", "present", "resolution", "and", "my", "previous", "writings", "and", "utterances");
        
        CMUDict cmuDictEN = CMUDict.getInstance();
        SyllabifiedCMUDict syllabifiedCMUDict = SyllabifiedCMUDict.getInstance();
        List<List<String>> units = Arrays.asList(unit1, unit2, unit3, unit4, unit5, unit6, unit7, unit8, unit9, unit10);
        
        List<String> specialUnit = Arrays.asList("0.0");
        for (List<String> unit : units) {
            // in case of test unit must be a list of Words
            // change method parameter in Rhythm class
//            Rhythm.calculateRhythmSM(unit, syllabifiedCMUDict);
        }
        System.out.println((int) Math.ceil(1.0 * 2 / 3));
        System.out.println((int) Math.ceil(1.0 * 3 / 2));
        System.out.println(Math.max((int) Math.ceil(1.0 * 2 / 3), (int) Math.ceil(1.0 * 3 / 2)));
//        List<Integer> syllables = new ArrayList<>();
//        List<String> stresses = new ArrayList<>();
//        List<String> phonemes = new ArrayList<>();
//        List<Double> indices = new ArrayList<>();
//        List<Integer> i_indices = new ArrayList<>();
//        Map<Integer, Integer> int_indices = new HashMap<>();
//        for (List<String> unit : units) {
//            for (String s : unit) {
//                System.out.print(s + " ");
//            }
//            double rhythm = Rhythm.calculateRhythm2(unit, cmuDictEN, syllables, stresses, phonemes);
//            indices.add(rhythm);
//            int rhythmIntegerValue = (int)Math.ceil(rhythm);
//            i_indices.add(rhythmIntegerValue);
//            if (!int_indices.containsKey(rhythm)) {
//                int_indices.put(rhythmIntegerValue, 1);
//            } else {
//                int_indices.put(rhythmIntegerValue, int_indices.get(rhythmIntegerValue) + 1);
//            }
//            System.out.println("\nUnit rhythm: " + rhythm );
//            System.out.println("\nUnit rhythm integer value: " + rhythmIntegerValue);
//        }
//        
//        double sum = 0;
//        sum = indices.stream().map((ind) -> ind).reduce(sum, (accumulator, _item) -> accumulator + _item);
//        double ave_k = sum / indices.size();
    }
}
