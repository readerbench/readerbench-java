package services.semanticModels;

import java.util.Set;
import java.util.TreeMap;

import data.AnalysisElement;
import data.Word;
import data.Lang;

public interface ISemanticModel {
	public double getSimilarity(Word w1, Word w2);

	public double getSimilarity(AnalysisElement e1, AnalysisElement e2);

	public TreeMap<Word, Double> getSimilarConcepts(Word w, double minThreshold);

	public TreeMap<Word, Double> getSimilarConcepts(AnalysisElement e, double minThreshold);

	public Set<Word> getWordSet();

	public String getPath();

	public Lang getLanguage();
}
