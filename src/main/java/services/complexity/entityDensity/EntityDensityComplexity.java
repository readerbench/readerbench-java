package services.complexity.entityDensity;

import java.util.Set;
import java.util.TreeSet;

import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import DAO.AbstractDocument;
import DAO.Block;
import DAO.Sentence;
import DAO.Word;

/***
 * @author Mihai Dascalu
 */
public class EntityDensityComplexity implements IComplexityFactors {

	// Average number of named entities per paragraph/block
	private static double getAverageNamedEntitiesPerBlock(AbstractDocument d) {
		int noWords = 0;
		int noBlocks = 0;
		for (Block b : d.getBlocks()) {
			if (b != null) {
				for (Sentence s : b.getSentences()) {
					for (Word word : s.getAllWords()) {
						String ne = word.getNE();
						if (ne != null && !ne.equals("O")) {
							noWords++;
						}
					}
				}
				noBlocks++;
			}
		}
		if (noBlocks > 0)
			return ((double) noWords) / noBlocks;
		return ComplexityIndices.IDENTITY;
	}

	// Average number of noun named entities per paragraph/block
	private static double getAverageNounNamedEntitiesPerBlock(AbstractDocument d) {
		int noWords = 0;
		int noBlocks = 0;
		for (Block b : d.getBlocks()) {
			if (b != null) {
				for (Sentence s : b.getSentences()) {
					for (Word word : s.getAllWords()) {
						String pos = word.getPOS();
						String ne = word.getNE();
						if (pos != null && pos.contains("NN") && ne != null && !ne.equals("O")) {
							noWords++;
						}
					}
				}
				noBlocks++;
			}
		}
		if (noBlocks > 0)
			return ((double) noWords) / noBlocks;
		return ComplexityIndices.IDENTITY;
	}

	// Average number of named entities per paragraph/block
	private static double getAverageUniqueNamedEntitiesPerBlock(AbstractDocument d) {
		int noBlocks = 0;
		Set<String> uniqueEntities = new TreeSet<String>();
		for (Block b : d.getBlocks()) {
			if (b != null) {
				for (Sentence s : b.getSentences()) {
					for (Word word : s.getAllWords()) {
						String ne = word.getNE();
						if (ne != null && !ne.equals("O")) {
							uniqueEntities.add(word.getLemma());
						}
					}
				}
				noBlocks++;
			}
		}
		if (noBlocks > 0)
			return ((double) uniqueEntities.size()) / noBlocks;
		return ComplexityIndices.IDENTITY;
	}

	// Average number of named entities per sentence
	private static double getAverageNamedEntitiesPerSentence(AbstractDocument d) {
		int noWords = 0;
		int noSentences = 0;
		for (Sentence s : d.getSentencesInDocument()) {
			for (Word word : s.getAllWords()) {
				String ne = word.getNE();
				if (ne != null && !ne.equals("O")) {
					noWords++;
				}
			}
			noSentences++;
		}
		if (noSentences > 0)
			return ((double) noWords) / noSentences;
		return ComplexityIndices.IDENTITY;
	}

	@Override
	public void setComplexityFactorNames(String[] names) {
		names[ComplexityIndices.AVERAGE_NO_NAMED_ENT_PER_BLOCK] = "Average number of named entities per paragraph (EN only)";
		names[ComplexityIndices.AVERAGE_NO_NOUN_NAMED_ENT_PER_BLOCK] = "Average number of named entities that are nouns per paragraph (EN only)";
		names[ComplexityIndices.AVERAGE_NO_UNIQUE_NAMED_ENT_PER_BLOCK] = "Average number of unique named entities per paragraph (EN only)";
		names[ComplexityIndices.AVERAGE_NO_NAMED_ENT_PER_SENTENCE] = "Average number of entities per sentence (EN only)";
	}

	@Override
	public void computeComplexityFactors(AbstractDocument d) {
		switch (d.getLanguage()) {
		case fr:
			d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_NAMED_ENT_PER_BLOCK] = ComplexityIndices.IDENTITY;
			d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_NOUN_NAMED_ENT_PER_BLOCK] = ComplexityIndices.IDENTITY;
			d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_UNIQUE_NAMED_ENT_PER_BLOCK] = ComplexityIndices.IDENTITY;
			d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_NAMED_ENT_PER_SENTENCE] = ComplexityIndices.IDENTITY;
			break;
		default:
			d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_NAMED_ENT_PER_BLOCK] = EntityDensityComplexity
					.getAverageNamedEntitiesPerBlock(d);
			d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_NOUN_NAMED_ENT_PER_BLOCK] = EntityDensityComplexity
					.getAverageNounNamedEntitiesPerBlock(d);
			d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_UNIQUE_NAMED_ENT_PER_BLOCK] = EntityDensityComplexity
					.getAverageUniqueNamedEntitiesPerBlock(d);
			d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_NAMED_ENT_PER_SENTENCE] = EntityDensityComplexity
					.getAverageNamedEntitiesPerSentence(d);
			break;
		}
	}

	@Override
	public String getClassName() {
		return "Named Entity Complexity Factors (EN only)";
	}

	@Override
	public int[] getIDs() {
		return new int[] { ComplexityIndices.AVERAGE_NO_NAMED_ENT_PER_BLOCK,
				ComplexityIndices.AVERAGE_NO_NOUN_NAMED_ENT_PER_BLOCK,
				ComplexityIndices.AVERAGE_NO_UNIQUE_NAMED_ENT_PER_BLOCK,
				ComplexityIndices.AVERAGE_NO_NAMED_ENT_PER_SENTENCE };
	}
}