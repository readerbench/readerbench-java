package services.complexity.discourse;

import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import services.nlp.listOfWords.ClassesOfWords;
import services.nlp.listOfWords.Connectives;
import utils.localization.LocalizationUtils;
import data.AbstractDocument;

public class ConnectivesComplexity extends IComplexityFactors {

	
	public String getClassName() {
		return LocalizationUtils.getTranslation("Discourse Factors (Connectives)");
	}

	
	public void setComplexityIndexDescription(String[] descriptions) {
		int index = 0;
		for (String className : Connectives.CONNECTIVES_EN.getClasses()
				.keySet()) {
			descriptions[ComplexityIndices.CONNECTIVES + index] = LocalizationUtils.getTranslation("Average number of ")
					+ className.replaceAll("_", " ") + LocalizationUtils.getTranslation(" per paragraph");
			index++;
		}
	}
	public void setComplexityIndexAcronym(String[] acronyms) {
		int index = 0;
		for (String className : Connectives.CONNECTIVES_EN.getClasses()
				.keySet()) {
			String classNameAcronym = className.replaceAll("_", " ");
			classNameAcronym = classNameAcronym.substring(0, Math.min(classNameAcronym.length(), 3));
			acronyms[ComplexityIndices.CONNECTIVES + index] = "AvgNo" + classNameAcronym + "PerParag";
			index++;
		}
	}
	
	
	public int[] getIDs() {
		int[] ids = new int[Connectives.NO_CONNECTIVE_TYPES];
		for (int i = 0; i < Connectives.NO_CONNECTIVE_TYPES; i++)
			ids[i] = ComplexityIndices.CONNECTIVES + i;
		return ids;
	}

	
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
