package services.semanticModels;

import java.util.TreeMap;

import edu.cmu.lti.jawjaw.pobj.Lang;
import DAO.AnalysisElement;
import DAO.Word;

public interface ISemanticModel {
	public double getSimilarity(Word w1, Word w2);

	public double getSimilarity(AnalysisElement e1, AnalysisElement e2);

	public TreeMap<Word, Double> getSimilarConcepts(Word w, double minThreshold);

	public TreeMap<Word, Double> getSimilarConcepts(AnalysisElement e, double minThreshold);

	public String getPath();

	public Lang getLanguage();
}
