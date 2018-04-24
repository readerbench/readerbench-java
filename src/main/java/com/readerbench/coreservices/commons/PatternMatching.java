package com.readerbench.coreservices.commons;

import com.readerbench.coreservices.nlp.listOfWords.ListOfWords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dorinela Sirbu <dorinela.92@gmail.com>
 */
public class PatternMatching {

    private static final Logger LOGGER = LoggerFactory.getLogger(PatternMatching.class);

    private static ListOfWords patterns_causality_fr;
    private static ListOfWords patterns_metacognition_fr;
    private static ListOfWords patterns_causality_en;
    private static ListOfWords patterns_metacognition_en;

    public static ListOfWords getCausalityFr() {
        if (patterns_causality_fr == null) {
            patterns_causality_fr = new ListOfWords("resources/config/FR/word lists/causality_fr.txt");
        }
        return patterns_causality_fr;
    }

    public static ListOfWords getMetacognitionFr() {
        if (patterns_metacognition_fr == null) {
            patterns_metacognition_fr = new ListOfWords("resources/config/FR/word lists/metacognition_fr.txt");
        }
        return patterns_metacognition_fr;
    }

    public static ListOfWords getCausalityEn() {
        if (patterns_causality_en == null) {
            patterns_causality_en = new ListOfWords("resources/config/EN/word lists/causality_en.txt");
        }
        return patterns_causality_en;
    }

    public static ListOfWords getMetacognitionEn() {
        if (patterns_metacognition_en == null) {
            patterns_metacognition_en = new ListOfWords("resources/config/EN/word lists/metacognition_en.txt");
        }
        return patterns_metacognition_en;
    }

}
