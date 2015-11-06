package services.nlp.listOfWords;

import edu.cmu.lti.jawjaw.pobj.Lang;

public class Pronouns {
	public static final ClassesOfWords PRONOUNS_EN = new ClassesOfWords(
			"resources/config/WordLists/pronouns_en.txt");
	public static final ClassesOfWords PRONOUNS_FR = new ClassesOfWords(
			"resources/config/WordLists/pronouns_fr.txt");
	public static final int NO_PRONOUN_TYPES = PRONOUNS_EN.getClasses().size();

	public static boolean isConnective(String s, Lang lang) {
		if (lang == null)
			return false;
		switch (lang) {
		case fr:
			return PRONOUNS_FR.getAllWords().contains(s);
		default:
			return PRONOUNS_EN.getAllWords().contains(s);
		}
	}

}
