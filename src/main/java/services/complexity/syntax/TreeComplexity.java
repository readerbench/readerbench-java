package services.complexity.syntax;

import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import DAO.AbstractDocument;
import DAO.Sentence;

public class TreeComplexity implements IComplexityFactors {
	public static double getAverageTreeSize(AbstractDocument d) {
		int noSentences = 0, size = 0;
		for (Sentence s : d.getSentencesInDocument()) {
			if (s.getWords().size() > 0) {
				noSentences++;
				size += s.getPOSTreeSize();
			}
		}
		if (noSentences != 0)
			return ((double) size) / noSentences;
		return ComplexityIndices.IDENTITY;
	}

	public static double getAverageNoDependencies(AbstractDocument d) {
		int noSentences = 0, size = 0;
		for (Sentence s : d.getSentencesInDocument()) {
			if (s.getWords().size() > 0) {
				noSentences++;
				if (s.getDependencies() != null)
					size += s.getDependencies().typedDependencies().size();
			}
		}
		if (noSentences != 0 && size != 0)
			return ((double) size) / noSentences;
		return ComplexityIndices.IDENTITY;
	}

	public static double getAverageTreeDepth(AbstractDocument d) {
		int noSentences = 0, size = 0;
		for (Sentence s : d.getSentencesInDocument()) {
			if (s.getWords().size() > 0) {
				noSentences++;
				size += s.getPOSTreeDepth();
			}
		}
		if (noSentences != 0)
			return ((double) size) / noSentences;
		return ComplexityIndices.IDENTITY;
	}

	@Override
	public void setComplexityFactorNames(String[] names) {
		names[ComplexityIndices.AVERAGE_TREE_DEPTH] = "Average parsing tree depth";
		names[ComplexityIndices.AVERAGE_TREE_SIZE] = "Average parsing tree size";
		names[ComplexityIndices.AVERAGE_NO_SEMANTIC_DEPENDENCIES] = "Average number of dependencies from the syntactic graph (EN only)";
	}

	@Override
	public void computeComplexityFactors(AbstractDocument d) {
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_TREE_DEPTH] = TreeComplexity
				.getAverageTreeDepth(d);
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_TREE_SIZE] = TreeComplexity
				.getAverageTreeSize(d);
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_SEMANTIC_DEPENDENCIES] = TreeComplexity
				.getAverageNoDependencies(d);
	}

	@Override
	public String getClassName() {
		return "Syntax (Parsing tree complexity)";
	}

	@Override
	public int[] getIDs() {
		return new int[] { ComplexityIndices.AVERAGE_TREE_DEPTH,
				ComplexityIndices.AVERAGE_TREE_SIZE,
				ComplexityIndices.AVERAGE_NO_SEMANTIC_DEPENDENCIES };
	}
}
