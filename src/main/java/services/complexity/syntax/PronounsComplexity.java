package services.complexity.syntax;

import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import services.nlp.listOfWords.ClassesOfWords;
import services.nlp.listOfWords.Pronouns;
import utils.localization.LocalizationUtils;
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
			descriptions[ComplexityIndices.PRONOUNS + index] = LocalizationUtils.getTranslation("Average number of") + " "
					+ className.replaceAll("_", " ")
					+ " "+ LocalizationUtils.getTranslation("pronouns per paragraph");
			index++;
		}
	}
	public void setComplexityIndexAcronym(String[] acronyms) {
		int index = 0;
		for (String className : Pronouns.PRONOUNS_EN.getClasses().keySet()) {
			String acronymClassName = className.replaceAll("_", " ");
			acronymClassName = acronymClassName.substring(0, Math.min(3, acronymClassName.length()));
			acronyms[ComplexityIndices.PRONOUNS + index] = "AvgNoOf" + " "
					+ acronymClassName
					+ " "+ "PronPerPar";
			index++;
		}
	}
	
	@Override
	public int[] getIDs() {
		int[] ids = new int[Pronouns.NO_PRONOUN_TYPES];
		for (int i = 0; i < Pronouns.NO_PRONOUN_TYPES; i++)
			ids[i] = ComplexityIndices.PRONOUNS + i;
		return ids;
	}

	@Override
	public void computeComplexityFactors(AbstractDocument document) {
		ClassesOfWords classes = null;
		switch (document.getLanguage()) {
		case fr:
			classes = Pronouns.PRONOUNS_FR;
			break;
		default:
			classes = Pronouns.PRONOUNS_EN;
			break;
		}

		int index = 0;
		for (String className : Pronouns.PRONOUNS_EN.getClasses().keySet()) {
			document.getComplexityIndices()[ComplexityIndices.PRONOUNS + index] = classes
					.countAveragePatternOccurrences(document, className);
			index++;
		}
	}
}
