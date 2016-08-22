package services.nlp.listOfWords;

import data.Lang;

public class Pronouns {

    public static final ClassesOfWords PRONOUNS_EN = new ClassesOfWords(
            "resources/config/WordLists/pronouns_en.txt");
    public static final ClassesOfWords PRONOUNS_FR = new ClassesOfWords(
            "resources/config/WordLists/pronouns_fr.txt");
    public static final ClassesOfWords PRONOUNS_RO = new ClassesOfWords(
            "resources/config/WordLists/pronouns_ro.txt");
    public static final ClassesOfWords PRONOUNS_NL = new ClassesOfWords(
            "resources/config/WordLists/pronouns_nl.txt");
    public static final ClassesOfWords PRONOUNS_LA = new ClassesOfWords(
            "resources/config/WordLists/pronouns_la.txt");
    public static final int NO_PRONOUN_TYPES = PRONOUNS_EN.getClasses().size();

    public static ClassesOfWords getClasses(Lang lang) {
        switch (lang) {
            case eng:
                return PRONOUNS_EN;
            case fr:
                return PRONOUNS_FR;
            case ro:
                return PRONOUNS_RO;
            case nl:
                return PRONOUNS_NL;
            case la:
                return PRONOUNS_LA;
            default:
                return null;
        }
    }
    
    public static boolean isConnective(String s, Lang lang) {
        if (lang == null) {
            return false;
        }
        switch (lang) {
            case fr:
                return PRONOUNS_FR.getAllWords().contains(s);
            case ro:
                return PRONOUNS_RO.getAllWords().contains(s);
            case nl:
                return PRONOUNS_NL.getAllWords().contains(s);
            case la:
                return PRONOUNS_LA.getAllWords().contains(s);
            default:
                return PRONOUNS_EN.getAllWords().contains(s);
        }
    }
}
