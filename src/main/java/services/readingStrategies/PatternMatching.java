/* 
 * Copyright 2016 ReaderBench.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package services.readingStrategies;

import java.awt.Color;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import data.AnalysisElement;
import data.Sentence;
import data.document.ReadingStrategyType;
import services.nlp.listOfWords.ListOfWords;

/**
 *
 * @author Mihai Dascalu
 */
public class PatternMatching {

    static Logger logger = Logger.getLogger(PatternMatching.class);

    private static ListOfWords patterns_causality_fr;
    private static ListOfWords patterns_metacognition_fr;
    private static ListOfWords patterns_causality_en;
    private static ListOfWords patterns_metacognition_en;
    private static final Color COLOR_CAUSALITY = new Color(255, 0, 255);
    private static final Color COLOR_METACOGNITION = new Color(0, 203, 255);

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

    // returns the number of occurrences
    public static int containsStrategy(List<Sentence> sentences, AnalysisElement el, ReadingStrategyType strategy, boolean alreadyExistentCheck) {
        String text = " " + el.getAlternateText().toLowerCase() + " ";
        int no_occurences = 0;
        ListOfWords usedList = null;
        String usedColor = null;
        switch (strategy) {
            case CAUSALITY:
                usedColor = Integer.toHexString(COLOR_CAUSALITY.getRGB());
                usedColor = usedColor.substring(2, usedColor.length());
                switch (el.getLanguage()) {
                    case en:
                        usedList = getCausalityEn();
                        break;
                    case fr:
                        usedList = getCausalityFr();
                        break;
                    default:
                        break;
                }
                break;
            case META_COGNITION:
                usedColor = Integer.toHexString(COLOR_METACOGNITION.getRGB());
                usedColor = usedColor.substring(2, usedColor.length());
                switch (el.getLanguage()) {
                    case en:
                        usedList = getMetacognitionEn();
                        break;
                    case fr:
                        usedList = getMetacognitionFr();
                        break;
                    default:
                        break;
                }
                break;
        }

        if (usedList != null && strategy.equals(ReadingStrategyType.META_COGNITION)) {
            for (String pattern : usedList.getWords()) {
                // check that the pattern does not exist in any of the previous sentences
                boolean exists = false;
                if (alreadyExistentCheck) {
                    for (Sentence s : sentences) {
                        Pattern javaPattern = Pattern.compile(" " + pattern + " ");
                        Matcher matcher = javaPattern.matcher(" " + s.getText() + " ");
                        if (matcher.find()) {
                            exists = true;
                            break;
                        }
                    }
                }
                if (!exists) {
                    Pattern javaPattern = Pattern.compile(" " + pattern + " ");
                    Matcher matcher = javaPattern.matcher(" " + text.trim() + " ");
                    while (matcher.find()) {
                        no_occurences++;
                    }
                    text = colorText(text.trim(), pattern, usedColor);
                }
            }
        }
        if (usedList != null && strategy.equals(ReadingStrategyType.CAUSALITY)) {
            for (String pattern : usedList.getWords()) {
                if (text.trim().startsWith(pattern + " ")) {
                    Pattern javaPattern = Pattern.compile(" " + pattern + " ");
                    Matcher matcher = javaPattern.matcher(" " + text.trim().substring(pattern.length() + 1).trim() + " ");
                    while (matcher.find()) {
                        no_occurences++;
                    }
                    text = pattern + " " + colorText(text.trim().substring(pattern.length() + 1).trim(), pattern, usedColor);
                } else {
                    Pattern javaPattern = Pattern.compile(" " + pattern + " ");
                    Matcher matcher = javaPattern.matcher(" " + text.trim() + " ");
                    while (matcher.find()) {
                        no_occurences++;
                    }
                    text = colorText(text.trim(), pattern, usedColor);
                }
                // recheck just to be sure
                Pattern javaPattern = Pattern.compile(" " + pattern + " ");
                Matcher matcher = javaPattern.matcher(" " + text.trim() + " ");
                while (matcher.find()) {
                    no_occurences++;
                }
                text = colorText(text.trim(), pattern, usedColor);
            }
        }
        el.setAlternateText(text.trim());
        return no_occurences;
    }

    public static String colorText(String text, String pattern, String color) {
        String phrase = " " + text + " ";
        phrase = phrase.replaceAll(" " + pattern + " ",
                " <font color=\"" + color + "\"><b>" + pattern + "</b></font> ");
        return phrase.trim();
    }

    public static String underlineIntalicsText(String text, String color) {
        return " <font color=\"" + color + "\" style=\"text-decoration: underline, italics;\">" + text.trim() + "</font>";
    }

    public static String colorTextIndex(String text, String pattern, String color, int index) {
        String phrase = " " + text + " ";
        String replacement = " <font color=\"" + color + "\"><b>" + pattern + "[" + index + "]</b></font> ";
        phrase = phrase.replaceAll(" " + pattern + " ", replacement);
        return phrase.trim();
    }

    public static String colorTextStar(String text, String pattern, String color, String annotationText) {
        String phrase = " " + text + " ";
        String replacement = " <font color=\"" + color + "\"><b>" + pattern + "[" + annotationText + "*]</b></font> ";
        phrase = phrase.replaceAll(" " + pattern + " ", replacement);
        return phrase.trim();
    }
}
