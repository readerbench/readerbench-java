/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.nlp.spellchecking;

import data.Lang;
import java.io.IOException;
import java.util.List;
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.language.Dutch;
import org.languagetool.language.French;
import org.languagetool.language.Italian;
import org.languagetool.language.Romanian;
import org.languagetool.language.Spanish;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.openide.util.Exceptions;

/**
 *
 * @author mihaidascalu
 */
public class Spellchecking {

    public static JLanguageTool spellechecker_ro = null;
    public static JLanguageTool spellechecker_fr = null;
    public static JLanguageTool spellechecker_it = null;
    public static JLanguageTool spellechecker_en = null;
    public static JLanguageTool spellechecker_es = null;
    public static JLanguageTool spellechecker_nl = null;

    public static void checkText(String text, Lang lang) {
        try {
            JLanguageTool langTool = getSpellcheckerInstance(lang);
            List<RuleMatch> matches = langTool.check(text);
            for (RuleMatch match : matches) {
                System.out.println("Potential typo at characters "
                        + match.getFromPos() + "-" + match.getToPos() + ": "
                        + match.getMessage());
                System.out.println("Suggested correction(s): "
                        + match.getSuggestedReplacements());
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
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
            spellechecker_ro = new JLanguageTool(new Romanian());
        }
        return spellechecker_ro;
    }

    public static JLanguageTool getSpellcheckerFr() {
        if (spellechecker_fr == null) {
            spellechecker_fr = new JLanguageTool(new French());
        }
        return spellechecker_fr;
    }

    public static JLanguageTool getSpellcheckerIt() {
        if (spellechecker_it == null) {
            spellechecker_it = new JLanguageTool(new Italian());
        }
        return spellechecker_it;
    }

    public static JLanguageTool getSpellcheckerEn() {
        if (spellechecker_en == null) {
            spellechecker_en = new JLanguageTool(new AmericanEnglish());
//            for (Rule rule : spellechecker_en.getAllRules()) {
//                if (!rule.isDictionaryBasedSpellingRule()) {
//                    spellechecker_en.disableRule(rule.getId());
//                }
//            }
        }
        return spellechecker_en;
    }

    public static JLanguageTool getSpellcheckerEs() {
        if (spellechecker_es == null) {
            spellechecker_es = new JLanguageTool(new Spanish());
        }
        return spellechecker_es;
    }

    public static JLanguageTool getSpellcheckerNl() {
        if (spellechecker_nl == null) {
            spellechecker_nl = new JLanguageTool(new Dutch());
        }
        return spellechecker_nl;
    }

    public static void main(String[] args) {
        checkText("A speling error is not that bad.", Lang.en);
    }
}
