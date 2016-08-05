package services.complexity.syntax;

import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import services.nlp.listOfWords.ClassesOfWords;
import services.nlp.listOfWords.Pronouns;
import utils.localization.LocalizationUtils;

import org.apache.commons.lang.WordUtils;

import data.AbstractDocument;

public class PronounsComplexity extends IComplexityFactors {

	@Override
	public String getClassName() {
		return LocalizationUtils.getTranslation("Syntax (Pronouns)");
	}

	@Override
	public void setComplexityIndexDescription(String[] descriptions) {
		int index = 0;
		for (String className : Pronouns.PRONOUNS_EN.getClasses().keySet()) {
			descriptions[ComplexityIndices.PRONOUNS + (index++)] = LocalizationUtils.getTranslation("Average number of")
					+ " " + className.replaceAll("_", " ") + " "
					+ LocalizationUtils.getTranslation("pronouns per paragraph");
			descriptions[ComplexityIndices.PRONOUNS + (index++)] = LocalizationUtils.getTranslation("Average number of")
					+ " " + className.replaceAll("_", " ") + " "
					+ LocalizationUtils.getTranslation("pronouns per sentence");
		}
	}

	public void setComplexityIndexAcronym(String[] acronyms) {
		int index = 0;
		for (String className : Pronouns.PRONOUNS_EN.getClasses().keySet()) {
			String acronymClassName = WordUtils.capitalizeFully(className, new char[] { '_' }).replaceAll("_", "");
			acronyms[ComplexityIndices.PRONOUNS + (index++)] = "Avg" + acronymClassName + "PronBl";
			acronyms[ComplexityIndices.PRONOUNS + (index++)] = "Avg" + acronymClassName + "PronSnt";
		}
	}

	@Override
	public int[] getIDs() {
		int[] ids = new int[Pronouns.NO_PRONOUN_TYPES * 2];
		for (int i = 0; i < Pronouns.NO_PRONOUN_TYPES * 2; i++)
			ids[i] = ComplexityIndices.PRONOUNS + i;
		return ids;
	}

    @Override
    public void computeComplexityFactors(AbstractDocument document) {
        ClassesOfWords classes;
        switch (document.getLanguage()) {
            case fr:
                classes = Pronouns.PRONOUNS_FR;
                break;
            case eng:
                classes = Pronouns.PRONOUNS_EN;
                break;
            case ro:
                classes = Pronouns.PRONOUNS_RO;
                break;
            case nl:
                classes = Pronouns.PRONOUNS_NL;
                break;
            case la:
                classes = Pronouns.PRONOUNS_LA;
                break;
            default:
                classes = null;
                break;
        }

		if (classes != null) {
			int index = 0;
			for (String className : Pronouns.PRONOUNS_EN.getClasses().keySet()) {
				document.getComplexityIndices()[ComplexityIndices.PRONOUNS + (index++)] = classes
						.countAveragePatternOccurrences(document, className, true); // per paragraph
				document.getComplexityIndices()[ComplexityIndices.PRONOUNS + (index++)] = classes
						.countAveragePatternOccurrences(document, className, false); // per sentence
			}
		}
	}
}
