package services.complexity.discourse;

import org.apache.commons.lang.WordUtils;

import data.AbstractDocument;
import data.Lang;
import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import services.nlp.listOfWords.ClassesOfWords;
import services.nlp.listOfWords.Connectives;
import utils.localization.LocalizationUtils;

public class ConnectivesComplexity extends IComplexityFactors {

    private Lang lang;

    public ConnectivesComplexity(Lang lang) {
        this.lang = lang;
    }

    public String getClassName() {
        return LocalizationUtils.getTranslation("Discourse Factors (Connectives)") + " (" + lang + ")";
    }

    public void setComplexityIndexDescription(String[] descriptions) {
        int index = 0;
        switch (lang) {
            case eng:
                for (String className : Connectives.CONNECTIVES_EN.getClasses().keySet()) {
                    descriptions[ComplexityIndices.CONNECTIVES_EN + index] = "Average number of "
                            + className.replaceAll("_", " ") + " connectives per block (EN)";
                    index++;
                }
                break;
            case fr:
                for (String className : Connectives.CONNECTIVES_FR.getClasses().keySet()) {
                    descriptions[ComplexityIndices.CONNECTIVES_FR + index] = "Nombre moyen de connecteur "
                            + className.replaceAll("_", " ") + " selon un bloc (FR)";
                    index++;
                }
                break;
            case ro:
                for (String className : Connectives.CONNECTIVES_RO.getClasses().keySet()) {
                    descriptions[ComplexityIndices.CONNECTIVES_RO + index] = "Număr mediu de conectori"
                            + className.replaceAll("_", " ") + " conectori per paragraf (RO)";
                    index++;
                }
            default:
                break;
        }
    }

    public void setComplexityIndexAcronym(String[] acronyms) {
        int index = 0;
        switch (lang) {
            case eng:
                for (String className : Connectives.CONNECTIVES_EN.getClasses().keySet()) {
                    String classNameAcronym = WordUtils.capitalizeFully(className, new char[]{'_'}).replaceAll("_", "");
                    acronyms[ComplexityIndices.CONNECTIVES_EN + index] = "Avg" + classNameAcronym + "BlEn";
                    index++;
                }
                break;
            case fr:
                for (String className : Connectives.CONNECTIVES_FR.getClasses().keySet()) {
                    String classNameAcronym = WordUtils.capitalizeFully(className, new char[]{'_'}).replaceAll("_", "");
                    acronyms[ComplexityIndices.CONNECTIVES_FR + index] = "Avg" + classNameAcronym + "BlFr";
                    index++;
                }
                break;
            case ro:
                for (String className : Connectives.CONNECTIVES_RO.getClasses().keySet()) {
                    String classNameAcronym = WordUtils.capitalizeFully(className, new char[]{'_'}).replaceAll("_", "");
                    acronyms[ComplexityIndices.CONNECTIVES_RO + index] = "Avg" + classNameAcronym + "BlRo";
                    index++;
                }
            default:
                break;
        }
    }

    public int[] getIDs() {
        int[] ids = null;
        switch (lang) {
            case eng:
                ids = new int[Connectives.NO_CONNECTIVE_TYPES_EN];
                for (int i = 0; i < Connectives.NO_CONNECTIVE_TYPES_EN; i++) {
                    ids[i] = ComplexityIndices.CONNECTIVES_EN + i;
                }
                break;
            case fr:
                ids = new int[Connectives.NO_CONNECTIVE_TYPES_FR];
                for (int i = 0; i < Connectives.NO_CONNECTIVE_TYPES_FR; i++) {
                    ids[i] = ComplexityIndices.CONNECTIVES_EN + i;
                }
                break;
            case ro:
                ids = new int[Connectives.NO_CONNECTIVE_TYPES_RO];
                for (int i = 0; i < Connectives.NO_CONNECTIVE_TYPES_RO; i++) {
                    ids[i] = ComplexityIndices.CONNECTIVES_EN + i;
                }
                break;
            default:
                break;
        }
        return ids;
    }

    public void computeComplexityFactors(AbstractDocument document) {
        for (int i = 0; i < Connectives.NO_CONNECTIVE_TYPES_EN + Connectives.NO_CONNECTIVE_TYPES_FR
                + Connectives.NO_CONNECTIVE_TYPES_RO; i++) {
            document.getComplexityIndices()[ComplexityIndices.CONNECTIVES_EN + i] = ComplexityIndices.IDENTITY;
        }

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
            case ro:
                classes = Connectives.CONNECTIVES_RO;
                index = ComplexityIndices.CONNECTIVES_RO;
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
