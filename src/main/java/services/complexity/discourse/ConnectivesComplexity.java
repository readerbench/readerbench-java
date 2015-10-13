package services.complexity.discourse;

import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import services.nlp.listOfWords.ClassesOfWords;
import services.nlp.listOfWords.Connectives;
import DAO.AbstractDocument;

public class ConnectivesComplexity implements IComplexityFactors {

	@Override
	public String getClassName() {
		return "Discourse Factors (Connectives)";
	}

	@Override
	public void setComplexityFactorNames(String[] names) {
		int index = 0;
		for (String className : Connectives.CONNECTIVES_EN.getClasses()
				.keySet()) {
			names[ComplexityIndices.CONNECTIVES + index] = "Average number of "
					+ className.replaceAll("_", " ") + " per paragraph";
			index++;
		}
	}

	@Override
	public int[] getIDs() {
		int[] ids = new int[Connectives.NO_CONNECTIVE_TYPES];
		for (int i = 0; i < Connectives.NO_CONNECTIVE_TYPES; i++)
			ids[i] = ComplexityIndices.CONNECTIVES + i;
		return ids;
	}

	@Override
	public void computeComplexityFactors(AbstractDocument document) {
		ClassesOfWords classes = null;
		switch (document.getLanguage()) {
		case fr:
			classes = Connectives.CONNECTIVES_FR;
			break;
		default:
			classes = Connectives.CONNECTIVES_EN;
			break;
		}

		int index = 0;
		for (String className : Connectives.CONNECTIVES_EN.getClasses()
				.keySet()) {
			document.getComplexityIndices()[ComplexityIndices.CONNECTIVES
					+ index] = classes.countAveragePatternOccurrences(document,
					className);
			index++;
		}
	}
}
