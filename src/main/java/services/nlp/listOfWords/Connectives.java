package services.nlp.listOfWords;

import edu.cmu.lti.jawjaw.pobj.Lang;

public class Connectives {
	public static final ClassesOfWords CONNECTIVES_EN = new ClassesOfWords(
			"resources/config/WordLists/connectives_en.txt");
	public static final ClassesOfWords CONNECTIVES_FR = new ClassesOfWords(
			"resources/config/WordLists/connectives_fr.txt");
	public static final int NO_CONNECTIVE_TYPES_EN = CONNECTIVES_EN.getClasses().size();
	public static final int NO_CONNECTIVE_TYPES_FR = CONNECTIVES_FR.getClasses().size();

	public static boolean isConnective(String s, Lang lang) {
		if (lang == null)
			return false;
		switch (lang) {
		case fr:
			return CONNECTIVES_FR.getAllWords().contains(s);
		case eng:
			return CONNECTIVES_EN.getAllWords().contains(s);
		default:
			return false;
		}
	}

}
