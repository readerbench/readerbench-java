/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.coreservices.nlp.spellchecking;

import com.readerbench.datasourceprovider.pojo.Lang;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Mihai Dascalu
 */
public class Spellchecking {

    private static final Logger LOGGER = LoggerFactory.getLogger(Spellchecking.class);

    public static JLanguageTool spellechecker_ro = null;
    public static JLanguageTool spellechecker_fr = null;
    public static JLanguageTool spellechecker_it = null;
    public static JLanguageTool spellechecker_en = null;
    public static JLanguageTool spellechecker_es = null;
    public static JLanguageTool spellechecker_de = null;
    public static JLanguageTool spellechecker_nl = null;

    private List<String> messages = new ArrayList<>();

    /*
    Spellchecking was designed for self-explanation - you give a text(exactly inputText), and you have to elaborate your
    thoughts about what you read. From the recommendations from the  spellchecking, it's clear that you want to
    prioritize words that appear in the original text since they are likely to be referred to.
    For chat, the inputText is an empty string.
     */

    public String checkText(String text, Lang lang, String inputText) {
        if (text == null || text.length() == 0) {
            return "";
        }
        try {
            JLanguageTool langTool = getSpellcheckerInstance(lang);
            StringBuilder correctText = new StringBuilder(text);
            List<RuleMatch> matches = langTool.check(text);
            messages = new ArrayList<>();

            Matcher m = Pattern.compile("\\w+").matcher(inputText);
            Set<String> tokens = new TreeSet<>();
            while (m.find()) {
                tokens.add(m.group(0));
            }

            int offset = 0;
            for (RuleMatch match : matches) {
                messages.add(match.getMessage() + " (characters " + match.getFromPos() + " - " + match.getToPos() + "); Suggested correction(s): " + match.getSuggestedReplacements());
                LOGGER.warn("{} (characters {} - {}); Suggested correction(s): {}", new Object[]{match.getMessage(), match.getFromPos(), match.getToPos(), match.getSuggestedReplacements()});
                if (match.getSuggestedReplacements().size() > 0) {
                    //prioritize words from the previous text
                    int bestMatch = 0;
                    for (int i = 0; i < match.getSuggestedReplacements().size(); i++) {
                        if (tokens.contains(match.getSuggestedReplacements().get(i))) {
                            bestMatch = i;
                            break;
                        }
                    }
                    correctText.replace(match.getFromPos() - offset, match.getToPos() - offset, match.getSuggestedReplacements().get(bestMatch));
                    offset += (match.getToPos() - match.getFromPos() - match.getSuggestedReplacements().get(bestMatch).length());
                }
            }

            return correctText.toString();
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
            return text;
        }
    }

    public static JLanguageTool getSpellcheckerInstance(Lang lang) {
        if (lang == null) {
            return null;
        }
        switch (lang) {
            case en:
                return getSpellcheckerEn();
            case fr:
                return getSpellcheckerFr();
            case ro:
                return getSpellcheckerRo();
            case it:
                return getSpellcheckerIt();
            case es:
                return getSpellcheckerEs();
            case nl:
                return getSpellcheckerNl();
            case de:
                return getSpellcheckerDe();
            default:
                return null;
        }
    }

    public static void addWordsToIgnore(JLanguageTool langTool, List<String> words) {
        langTool.getAllActiveRules().stream().filter((rule) -> (rule instanceof SpellingCheckRule)).forEachOrdered((rule) -> {
            ((SpellingCheckRule) rule).addIgnoreTokens(words);
        });
    }

    public static void addPhrasesToIgnore(JLanguageTool langTool, List<String> phrases) {
        langTool.getAllActiveRules().stream().filter((rule) -> (rule instanceof SpellingCheckRule)).forEachOrdered((rule) -> {
            ((SpellingCheckRule) rule).acceptPhrases(phrases);
        });
    }

    public static JLanguageTool getSpellcheckerRo() {
        if (spellechecker_ro == null) {
            try {
                spellechecker_ro = new JLanguageTool((org.languagetool.Language) Class.forName("org.languagetool.language.Romanian").newInstance());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
        return spellechecker_ro;
    }

    public static JLanguageTool getSpellcheckerFr() {
        if (spellechecker_fr == null) {
            try {
                spellechecker_fr = new JLanguageTool((org.languagetool.Language) Class.forName("org.languagetool.language.French").newInstance());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
        return spellechecker_fr;
    }

    public static JLanguageTool getSpellcheckerIt() {
        if (spellechecker_it == null) {
            try {
                spellechecker_it = new JLanguageTool((org.languagetool.Language) Class.forName("org.languagetool.language.Italian").newInstance());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
        return spellechecker_it;
    }

    public static JLanguageTool getSpellcheckerEn() {
        if (spellechecker_en == null) {
            try {
                spellechecker_en = new JLanguageTool((org.languagetool.Language) Class.forName("org.languagetool.language.AmericanEnglish").newInstance());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
        return spellechecker_en;
    }

    public static JLanguageTool getSpellcheckerEs() {
        if (spellechecker_es == null) {
            try {
                spellechecker_es = new JLanguageTool((org.languagetool.Language) Class.forName("org.languagetool.language.Spanish").newInstance());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
        return spellechecker_es;
    }

    public static JLanguageTool getSpellcheckerNl() {
        if (spellechecker_nl == null) {
            try {
                spellechecker_nl = new JLanguageTool((org.languagetool.Language) Class.forName("org.languagetool.language.Dutch").newInstance());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
        return spellechecker_nl;
    }

    public static JLanguageTool getSpellcheckerDe() {
        if (spellechecker_de == null) {
            try {
                spellechecker_de = new JLanguageTool((org.languagetool.Language) Class.forName("org.languagetool.language.German").newInstance());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
        return spellechecker_de;
    }

    public List<String> getMessages() {
        return messages;
    }
}
