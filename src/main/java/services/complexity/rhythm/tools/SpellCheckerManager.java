/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.complexity.rhythm.tools;

import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.event.SpellChecker;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author valentin.sergiu.cioaca@gmail.com
 */
public class SpellCheckerManager {
    protected static SpellDictionaryHashMap dictionary = null;
    protected static SpellChecker spellChecker = null;
    protected static MaxentTagger tagger = null;
    
    static {
        try {
            dictionary = new SpellDictionaryHashMap(new File("resources/config/EN/word lists/dict_en.txt"));
            tagger = new MaxentTagger("resources/config/EN/taggers/english-left3words-distsim.tagger");
        } catch (IOException e) {
            e.printStackTrace();
        }
        spellChecker = new SpellChecker(dictionary);
    }
    
    public static List<String> getSuggestions(String word, int threshold) {
        return spellChecker.getSuggestions(word, threshold);
    }
    
    public static String getBestSuggestion(String word, String posTagging) {
        List suggestions = SpellCheckerManager.getSuggestions(word, 0);
        for (Iterator<com.swabunga.spell.engine.Word> it = suggestions.iterator(); it.hasNext();) {
            String tagged = tagger.tagString(it.next().toString()).trim();
            String pos = tagged.substring(Math.max(tagged.length() - 2, 0));
            if (!pos.equals(posTagging)) {
                it.remove();
            }
        }
        return (!suggestions.isEmpty()) ? suggestions.get(0).toString() : word;
    }
    
    public static void main(String args[]) {
        List<String> words = Arrays.asList("serch","contetnt","favourite","developping",
                                           "conviced","rezolve","togheter","definiteley",
                                           "yout","anwered","disapear","usefull");
        for (String w : words) {
            System.out.println(w + ": " + getSuggestions(w, 0));
        }
        System.out.println(getBestSuggestion(words.get(0), "NN"));
    }
}
