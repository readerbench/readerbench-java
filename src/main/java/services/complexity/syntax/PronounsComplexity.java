package services.complexity.syntax;

import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import services.nlp.listOfWords.ClassesOfWords;
import services.nlp.listOfWords.Pronouns;
import data.AbstractDocument;

public class PronounsComplexity implements IComplexityFactors {

	@Override
	public String getClassName() {
		return "Syntax (Pronouns)";
	}

	@Override
	public void setComplexityFactorNames(String[] names) {
		int index = 0;
		for (String className : Pronouns.PRONOUNS_EN.getClasses().keySet()) {
			names[ComplexityIndices.PRONOUNS + index] = "Average number of "
					+ className.replaceAll("_", " ")
					+ " pronouns per paragraph";
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
