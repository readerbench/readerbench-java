package services.complexity.discourse;

import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import utils.localization.LocalizationUtils;
import data.AbstractDocument;
import data.Block;
import data.discourse.SemanticCohesion;

public class SemanticCohesionComplexity extends IComplexityFactors {
	private final int semanticDistIndex;
	private final String semanticDistName;
	private final String semanticDistAcronym;

	public SemanticCohesionComplexity(int si) {
		this.semanticDistIndex = si;
		this.semanticDistName = SemanticCohesion.getSemanticDistanceNames()[si];
		this.semanticDistAcronym = SemanticCohesion.getSemanticDistanceAcronyms()[si];
	}

	public static double getAvgBlockDocCohesion(AbstractDocument d, int semanticDistIndex) {
		int no = 0;
		double sum = 0;
		if (d != null && d.getBlockDocDistances() != null) {
			for (SemanticCohesion coh : d.getBlockDocDistances()) {
				if (coh != null && coh.getCohesion() > 0) {
					sum += coh.getSemanticDistances()[semanticDistIndex];
					no++;
				}
			}
		}
		if (no == 1)
			return ComplexityIndices.IDENTITY;
		if (no != 0)
			return sum / no;
		return ComplexityIndices.IDENTITY;
	}

	public static double getAvgSentenceBlockCohesion(AbstractDocument d, int semanticDistIndex) {
		int noBlocks = 0;
		double sumBlocks = 0;
		for (Block b : d.getBlocks()) {
			if (b != null) {
				int no = 0;
				double sum = 0;
				if (b != null) {
					for (SemanticCohesion coh : b.getSentenceBlockDistances()) {
						if (coh != null && coh.getCohesion() > 0) {
							sum += coh.getSemanticDistances()[semanticDistIndex];
							no++;
						}
					}
				}
				if (no != 0) {
					sumBlocks += sum / no;
					noBlocks++;
				}
			}
		}
		if (noBlocks != 0)
			return sumBlocks / noBlocks;
		return ComplexityIndices.IDENTITY;
	}

	public static double getAvgInterBlockCohesion(AbstractDocument d, int semanticDistIndex) {
		int no = 0;
		double sum = 0;
		for (int i = 0; i < d.getBlocks().size(); i++) {
			Block b = d.getBlocks().get(i);
			if (b != null) {
				for (int j = 0; j < d.getBlocks().size(); j++) {
					if (i != j && d.getPrunnedBlockDistances() != null && d.getPrunnedBlockDistances()[i][j] != null
							&& d.getPrunnedBlockDistances()[i][j].getCohesion() > 0) {
						sum += d.getPrunnedBlockDistances()[i][j].getSemanticDistances()[semanticDistIndex];
						no++;
					}
				}
			}
		}
		if (no != 0)
			return sum / no;
		return ComplexityIndices.IDENTITY;
	}

	public static double getAvgAdjacencyBlockCohesion(AbstractDocument d, int semanticDistIndex) {
		int no = 0;
		double sum = 0;
		for (int i = 0; i < d.getBlocks().size() - 1; i++) {
			Block b = d.getBlocks().get(i);
			if (b != null) {
				if (d.getPrunnedBlockDistances() != null && d.getPrunnedBlockDistances()[i][i + 1] != null
						&& d.getPrunnedBlockDistances()[i][i + 1].getCohesion() > 0) {
					sum += d.getPrunnedBlockDistances()[i][i + 1].getSemanticDistances()[semanticDistIndex];
					no++;
				}
			}
		}
		if (no != 0)
			return sum / no;
		return ComplexityIndices.IDENTITY;
	}

	public static double getAvgIntraBlockCohesion(AbstractDocument d, int semanticDistIndex) {
		int noBlocks = 0;
		double sumBlocks = 0;
		for (Block b : d.getBlocks()) {
			if (b != null) {
				int no = 0;
				double sum = 0;
				for (int i = 0; i < b.getSentences().size(); i++) {
					if (b.getSentences().get(i) != null) {
						for (int j = 0; j < b.getSentences().size(); j++) {
							if (i != j && b.getSentences().get(j) != null && b.getPrunnedSentenceDistances() != null
									&& b.getPrunnedSentenceDistances()[i][j] != null
									&& b.getPrunnedSentenceDistances()[i][j].getCohesion() > 0) {
								sum += b.getPrunnedSentenceDistances()[i][j].getSemanticDistances()[semanticDistIndex];
								no++;
							}
						}
					}
				}
				if (no != 0) {
					sumBlocks += sum / no;
					noBlocks++;
				}
			}
		}
		if (noBlocks != 0)
			return sumBlocks / noBlocks;
		return ComplexityIndices.IDENTITY;
	}

	public static double getAvgSentenceAdjacencyCohesion(AbstractDocument d, int semanticDistIndex) {
		int noBlocks = 0;
		double sumBlocks = 0;
		for (Block b : d.getBlocks()) {
			if (b != null) {
				int no = 0;
				double sum = 0;
				for (int i = 0; i < b.getSentences().size() - 1; i++) {
					if (b.getSentences().get(i) != null && b.getSentences().get(i + 1) != null
							&& b.getPrunnedSentenceDistances() != null
							&& b.getPrunnedSentenceDistances()[i][i + 1] != null
							&& b.getPrunnedSentenceDistances()[i][i + 1].getCohesion() > 0) {
						sum += b.getPrunnedSentenceDistances()[i][i + 1].getSemanticDistances()[semanticDistIndex];
						no++;
					}
				}
				if (no != 0) {
					sumBlocks += sum / no;
					noBlocks++;
				}
			}
		}
		if (noBlocks != 0)
			return sumBlocks / noBlocks;
		return ComplexityIndices.IDENTITY;
	}

	public static double getAvgTransitionCohesion(AbstractDocument d, int semanticDistIndex) {
		int no = 0;
		double sum = 0;
		Block previous = null;
		Block current = null;
		for (Block b : d.getBlocks()) {
			if (b != null) {
				current = b;
				if (previous != null) {
					if (!current.getSentences().isEmpty() && !previous.getSentences().isEmpty()) {
						SemanticCohesion coh = new SemanticCohesion(current.getSentences().get(0),
								previous.getSentences().get(previous.getSentences().size() - 1));
						sum += coh.getSemanticDistances()[semanticDistIndex];
						no++;
					}
				}
				previous = b;
			}
		}
		if (no != 0)
			return sum / no;
		return ComplexityIndices.IDENTITY;
	}

	public static double getStartMiddleCohesion(AbstractDocument d, int semanticDistIndex) {
		double no = 0;
		double sum = 0;
		Block startBlock = null;
		int startIndex = 0;
		int endIndex = d.getBlocks().size() - 1;
		for (; startIndex < d.getBlocks().size(); startIndex++) {
			Block b = d.getBlocks().get(startIndex);
			if (b != null) {
				startBlock = b;
				break;
			}
		}
		for (; endIndex >= 0; endIndex--) {
			Block b = d.getBlocks().get(endIndex);
			if (b != null) {
				break;
			}
		}

		for (int i = startIndex + 1; i < endIndex; i++) {
			Block b = d.getBlocks().get(i);
			if (b != null) {
				SemanticCohesion coh = new SemanticCohesion(startBlock, b);
				sum += coh.getSemanticDistances()[semanticDistIndex] / (i - startIndex);
				no += 1D / (i - startIndex);
			}
		}

		if (no != 0)
			return sum / no;
		return ComplexityIndices.IDENTITY;
	}

	public static double getMiddleEndCohesion(AbstractDocument d, int semanticDistIndex) {
		double no = 0;
		double sum = 0;
		Block endBlock = null;
		int startIndex = 0;
		int endIndex = d.getBlocks().size() - 1;
		for (; startIndex < d.getBlocks().size(); startIndex++) {
			Block b = d.getBlocks().get(startIndex);
			if (b != null) {
				break;
			}
		}
		for (; endIndex >= 0; endIndex--) {
			Block b = d.getBlocks().get(endIndex);
			if (b != null) {
				endBlock = b;
				break;
			}
		}

		for (int i = startIndex + 1; i < endIndex; i++) {
			Block b = d.getBlocks().get(i);
			if (b != null) {
				SemanticCohesion coh = new SemanticCohesion(b, endBlock);
				sum += coh.getSemanticDistances()[semanticDistIndex] / (endIndex - i);
				no += 1D / (endIndex - i);
			}
		}

		if (no != 0)
			return sum / no;
		return ComplexityIndices.IDENTITY;
	}

	public static double getStartEndCohesion(AbstractDocument d, int semanticDistIndex) {
		Block startBlock = null;
		Block endBlock = null;
		int startIndex = 0;
		int endIndex = d.getBlocks().size() - 1;
		for (; startIndex < d.getBlocks().size(); startIndex++) {
			Block b = d.getBlocks().get(startIndex);
			if (b != null) {
				startBlock = b;
				break;
			}
		}
		for (; endIndex >= 0; endIndex--) {
			Block b = d.getBlocks().get(endIndex);
			if (b != null) {
				endBlock = b;
				break;
			}
		}
		if (startBlock != null && endBlock != null) {
			SemanticCohesion coh = new SemanticCohesion(startBlock, endBlock);
			return coh.getSemanticDistances()[semanticDistIndex];
		}
		return ComplexityIndices.IDENTITY;
	}

	public String getClassName() {
		return LocalizationUtils.getTranslation("Semantic cohesion") + " (" + this.semanticDistName + ")";
	}

	public void setComplexityIndexDescription(String[] descriptions) {
		descriptions[ComplexityIndices.AVERAGE_BLOCK_DOC_COHESION + semanticDistIndex] = LocalizationUtils
				.getTranslation("Average paragraph document cohesion") + " (" + semanticDistName + ")";
		descriptions[ComplexityIndices.AVERAGE_SENTENCE_BLOCK_COHESION + semanticDistIndex] = LocalizationUtils
				.getTranslation("Average sentence paragraph cohesion") + " (" + semanticDistName + ")";
		descriptions[ComplexityIndices.AVERAGE_INTER_BLOCK_COHESION + semanticDistIndex] = LocalizationUtils
				.getTranslation("Average inter paragraph cohesion") + " (" + semanticDistName + ")";
		descriptions[ComplexityIndices.AVERAGE_INTRA_BLOCK_COHESION + semanticDistIndex] = LocalizationUtils
				.getTranslation("Average intra paragraph cohesion") + " (" + semanticDistName + ")";
		descriptions[ComplexityIndices.AVERAGE_BLOCK_ADJACENCY_COHESION + semanticDistIndex] = LocalizationUtils
				.getTranslation("Average paragraph adjacency cohesion") + " (" + semanticDistName + ")";
		descriptions[ComplexityIndices.AVERAGE_SENTENCE_ADJACENCY_COHESION + semanticDistIndex] = LocalizationUtils
				.getTranslation("Average sentence adjacency cohesion") + " (" + semanticDistName + ")";
		descriptions[ComplexityIndices.AVERAGE_TRANSITION_COHESION + semanticDistIndex] = LocalizationUtils
				.getTranslation("Average transition cohesion") + " (" + semanticDistName + ")";
		descriptions[ComplexityIndices.AVERAGE_START_MIDDLE_COHESION + semanticDistIndex] = LocalizationUtils
				.getTranslation("Weighted average start middle cohesion") + " (" + semanticDistName + ")";
		descriptions[ComplexityIndices.AVERAGE_MIDDLE_END_COHESION + semanticDistIndex] = LocalizationUtils
				.getTranslation("Weigthed average middle end cohesion") + " (" + semanticDistName + ")";
		descriptions[ComplexityIndices.START_END_COHESION + semanticDistIndex] = LocalizationUtils
				.getTranslation("Start end cohesion") + " (" + semanticDistName + ")";
	}

	public void setComplexityIndexAcronym(String[] acronyms) {
		acronyms[ComplexityIndices.AVERAGE_BLOCK_DOC_COHESION + semanticDistIndex] = this
				.getComplexityIndexAcronym("AVERAGE_BLOCK_DOC_COHESION") + semanticDistAcronym;
		acronyms[ComplexityIndices.AVERAGE_SENTENCE_BLOCK_COHESION + semanticDistIndex] = this
				.getComplexityIndexAcronym("AVERAGE_SENTENCE_BLOCK_COHESION") + semanticDistAcronym;
		acronyms[ComplexityIndices.AVERAGE_INTER_BLOCK_COHESION + semanticDistIndex] = this
				.getComplexityIndexAcronym("AVERAGE_INTER_BLOCK_COHESION") + semanticDistAcronym;
		acronyms[ComplexityIndices.AVERAGE_INTRA_BLOCK_COHESION + semanticDistIndex] = this
				.getComplexityIndexAcronym("AVERAGE_INTRA_BLOCK_COHESION") + semanticDistAcronym;
		acronyms[ComplexityIndices.AVERAGE_BLOCK_ADJACENCY_COHESION + semanticDistIndex] = this
				.getComplexityIndexAcronym("AVERAGE_BLOCK_ADJACENCY_COHESION") + semanticDistAcronym;
		acronyms[ComplexityIndices.AVERAGE_SENTENCE_ADJACENCY_COHESION + semanticDistIndex] = this
				.getComplexityIndexAcronym("AVERAGE_SENTENCE_ADJACENCY_COHESION") + semanticDistAcronym;
		acronyms[ComplexityIndices.AVERAGE_TRANSITION_COHESION + semanticDistIndex] = this
				.getComplexityIndexAcronym("AVERAGE_TRANSITION_COHESION") + semanticDistAcronym;
		acronyms[ComplexityIndices.AVERAGE_START_MIDDLE_COHESION + semanticDistIndex] = this
				.getComplexityIndexAcronym("AVERAGE_START_MIDDLE_COHESION") + semanticDistAcronym;
		acronyms[ComplexityIndices.AVERAGE_MIDDLE_END_COHESION + semanticDistIndex] = this
				.getComplexityIndexAcronym("AVERAGE_MIDDLE_END_COHESION") + semanticDistAcronym;
		acronyms[ComplexityIndices.START_END_COHESION + semanticDistIndex] = this
				.getComplexityIndexAcronym("START_END_COHESION") + semanticDistAcronym;
	}

	public void computeComplexityFactors(AbstractDocument d) {
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_BLOCK_DOC_COHESION
				+ semanticDistIndex] = SemanticCohesionComplexity.getAvgBlockDocCohesion(d, semanticDistIndex);
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_SENTENCE_BLOCK_COHESION
				+ semanticDistIndex] = SemanticCohesionComplexity.getAvgSentenceBlockCohesion(d, semanticDistIndex);
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_INTER_BLOCK_COHESION
				+ semanticDistIndex] = SemanticCohesionComplexity.getAvgInterBlockCohesion(d, semanticDistIndex);
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_INTRA_BLOCK_COHESION
				+ semanticDistIndex] = SemanticCohesionComplexity.getAvgIntraBlockCohesion(d, semanticDistIndex);
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_BLOCK_ADJACENCY_COHESION
				+ semanticDistIndex] = SemanticCohesionComplexity.getAvgInterBlockCohesion(d, semanticDistIndex);
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_SENTENCE_ADJACENCY_COHESION
				+ semanticDistIndex] = SemanticCohesionComplexity.getAvgIntraBlockCohesion(d, semanticDistIndex);
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_TRANSITION_COHESION
				+ semanticDistIndex] = SemanticCohesionComplexity.getAvgTransitionCohesion(d, semanticDistIndex);
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_START_MIDDLE_COHESION
				+ semanticDistIndex] = SemanticCohesionComplexity.getStartMiddleCohesion(d, semanticDistIndex);
		d.getComplexityIndices()[ComplexityIndices.AVERAGE_MIDDLE_END_COHESION
				+ semanticDistIndex] = SemanticCohesionComplexity.getMiddleEndCohesion(d, semanticDistIndex);
		d.getComplexityIndices()[ComplexityIndices.START_END_COHESION + semanticDistIndex] = SemanticCohesionComplexity
				.getStartEndCohesion(d, semanticDistIndex);
	}

	public int[] getIDs() {
		return new int[] { ComplexityIndices.AVERAGE_BLOCK_DOC_COHESION + semanticDistIndex,
				ComplexityIndices.AVERAGE_SENTENCE_BLOCK_COHESION + semanticDistIndex,
				ComplexityIndices.AVERAGE_INTER_BLOCK_COHESION + semanticDistIndex,
				ComplexityIndices.AVERAGE_INTRA_BLOCK_COHESION + semanticDistIndex,
				ComplexityIndices.AVERAGE_BLOCK_ADJACENCY_COHESION + semanticDistIndex,
				ComplexityIndices.AVERAGE_SENTENCE_ADJACENCY_COHESION + semanticDistIndex,
				ComplexityIndices.AVERAGE_TRANSITION_COHESION + semanticDistIndex,
				ComplexityIndices.AVERAGE_START_MIDDLE_COHESION + semanticDistIndex,
				ComplexityIndices.AVERAGE_MIDDLE_END_COHESION + semanticDistIndex,
				ComplexityIndices.START_END_COHESION + semanticDistIndex };
	}
}
