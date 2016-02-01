package services.complexity.discourse;

import org.apache.commons.lang.WordUtils;

import data.AbstractDocument;
import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import services.nlp.listOfWords.ClassesOfWords;
import services.nlp.listOfWords.Connectives;
import utils.localization.LocalizationUtils;

public class ConnectivesComplexity extends IComplexityFactors {

	public String getClassName() {
		return LocalizationUtils.getTranslation("Discourse Factors (Connectives)");
	}

	public void setComplexityIndexDescription(String[] descriptions) {
		int index = 0;
		for (String className : Connectives.CONNECTIVES_EN.getClasses().keySet()) {
			descriptions[ComplexityIndices.CONNECTIVES_EN + index] = "Average number of "
					+ className.replaceAll("_", " ") + " connectives per block (EN)";
			index++;
		}
		index = 0;
		for (String className : Connectives.CONNECTIVES_FR.getClasses().keySet()) {
			descriptions[ComplexityIndices.CONNECTIVES_FR + index] = "Nombre moyen de connecteur "
					+ className.replaceAll("_", " ") + " selon un bloc (FR)";
			index++;
		}
	}

	public void setComplexityIndexAcronym(String[] acronyms) {
		int index = 0;
		for (String className : Connectives.CONNECTIVES_EN.getClasses().keySet()) {
			String classNameAcronym = WordUtils.capitalizeFully(className, new char[] { '_' }).replaceAll("_", "");
			acronyms[ComplexityIndices.CONNECTIVES_EN + index] = "Avg" + classNameAcronym + "BlEn";
			index++;
		}
		index = 0;
		for (String className : Connectives.CONNECTIVES_FR.getClasses().keySet()) {
			String classNameAcronym = WordUtils.capitalizeFully(className, new char[] { '_' }).replaceAll("_", "");
			acronyms[ComplexityIndices.CONNECTIVES_FR + index] = "Avg" + classNameAcronym + "BlFr";
			index++;
		}
	}

	public int[] getIDs() {
		int[] ids = new int[Connectives.NO_CONNECTIVE_TYPES_EN + Connectives.NO_CONNECTIVE_TYPES_FR];
		for (int i = 0; i < Connectives.NO_CONNECTIVE_TYPES_EN + Connectives.NO_CONNECTIVE_TYPES_FR; i++)
			ids[i] = ComplexityIndices.CONNECTIVES_EN + i;
		return ids;
	}

	public void computeComplexityFactors(AbstractDocument document) {
		for (int i = 0; i < Connectives.NO_CONNECTIVE_TYPES_EN + Connectives.NO_CONNECTIVE_TYPES_FR; i++)
			document.getComplexityIndices()[ComplexityIndices.CONNECTIVES_EN + i] = ComplexityIndices.IDENTITY;

		ClassesOfWords classes = null;
		int index = 0;
		switch (document.getLanguage()) {
		case eng:
			classes = Connectives.CONNECTIVES_EN;
			index = ComplexityIndices.CONNECTIVES_EN;
			break;
		case fr:
			classes = Connectives.CONNECTIVES_FR;
			index = ComplexityIndices.CONNECTIVES_FR;
			break;
		default:
			classes = null;
			break;
		}

		if (classes != null) {
			for (String className : classes.getClasses().keySet()) {
				document.getComplexityIndices()[index] = classes.countAveragePatternOccurrences(document, className);
				index++;
			}
		}
	}
}
