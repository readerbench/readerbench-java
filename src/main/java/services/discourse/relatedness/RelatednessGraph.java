package services.discourse.relatedness;

import org.apache.log4j.Logger;

import data.AbstractDocument;
import data.Block;
import data.discourse.SemanticRelatedness;

/**
 * 
 * @author Gabriel Gutu
 */
public class RelatednessGraph {
	static Logger logger = Logger.getLogger(RelatednessGraph.class);

	/**
	 * Build the relatedness graph of a document.
	 * 
	 * @param d
	 *            the document for which to build the cohesion graph
	 */
	public static void buildRelatednessGraph(AbstractDocument d) {

		logger.info("Building relatedness graph...");

		// determine block-document semantic relatedness

		// initialize semantic relatedness vector for the semantic relatedness
		// of (block, document) pairs
		d.setBlockDocRelatedness(new SemanticRelatedness[d.getBlocks().size()]);
		// iterate through all blocks of the document
		for (int i = 0; i < d.getBlocks().size(); i++) {
			if (d.getBlocks().get(i) != null) {
				// set semantic relatedness between the block and the document
				d.getBlockDocRelatedness()[i] = new SemanticRelatedness(d.getBlocks().get(i), d);
			}
		}

		double
		// auxiliary variables used to compute mean and standard deviation of
		// semantic relatedness
		s0 = 0, s1 = 0, s2 = 0,

				// mean and standard deviation of computed semantic relatedness
				// values
				mean = 0, stdev = 0;

		// determine inner-block semantic relatedness

		// initialize semantic relatedness arrays for the semantic cohesion of
		// (block, block) pairs
		d.setBlockRelatedness(new SemanticRelatedness[d.getBlocks().size()][d.getBlocks().size()]);
		d.setPrunnedBlockRelatedness(new SemanticRelatedness[d.getBlocks().size()][d.getBlocks().size()]);
		// iterate through all blocks of the document
		for (int i = 0; i < d.getBlocks().size() - 1; i++) {
			if (d.getBlocks().get(i) != null) {
				// retrospective view built on explicit links
				// compute semantic relatedness between the block and the block
				// it is set to as explicit link
				int j = -1;
				if (d.getBlocks().get(i).getRefBlock() != null) {
					j = d.getBlocks().indexOf(d.getBlocks().get(i).getRefBlock());
					if (j != -1) {
						SemanticRelatedness sim = new SemanticRelatedness(d.getBlocks().get(j), d.getBlocks().get(i));
						d.getBlockRelatedness()[j][i] = sim;
						d.getBlockRelatedness()[i][j] = sim;
						if (sim.getRelatedness() != 0) {
							s0++;
							s1 += sim.getRelatedness();
							s2 += Math.pow(sim.getRelatedness(), 2);
						}
					}
				}

				// prospective view of semantic relatedness
				// compute semantic relatedness between the block and the
				// following WINDOW_SIZE blocks
				for (j = i + 1; j < Math.min(d.getBlocks().size(), i + SemanticRelatedness.WINDOW_SIZE + 1); j++) {
					if (d.getBlocks().get(j) != null) {
						SemanticRelatedness sim = new SemanticRelatedness(d.getBlocks().get(j), d.getBlocks().get(i));
						d.getBlockRelatedness()[j][i] = sim;
						d.getBlockRelatedness()[i][j] = sim;
						if (sim.getRelatedness() != 0) {
							s0++;
							s1 += sim.getRelatedness();
							s2 += Math.pow(sim.getRelatedness(), 2);
						}
					}
				}
			}
		}

		// determine mean and standard deviation values of semantic relatedness
		if (s0 != 0) {
			mean = s1 / s0;
			stdev = Math.sqrt(s0 * s2 - Math.pow(s1, 2)) / s0;
		}

		// prune initial graph, but always keep adjacent pairs of blocks or
		// explicitly referred blocks

		// iterate through all pairs of blocks of the document
		for (int i = 0; i < d.getBlocks().size() - 1; i++) {
			for (int j = i + 1; j < d.getBlocks().size(); j++) {
				// if the semantic relatedness is set for the pair of blocks (i,
				// j)
				if (d.getBlockRelatedness()[i][j] != null && (
				// if the semantic relatedness is greater than sum of mean and
				// standard deviation
				(d.getBlockRelatedness()[i][j].getRelatedness() >= (mean + stdev))
						// if j is the next block after i and there is not an
						// explicit link set for j
						|| (d.getBlocks().get(j).getRefBlock() == null && j == i + 1)
						// if there is an explicit link set for j and it is i
						|| (d.getBlocks().get(j).getRefBlock() != null
								&& d.getBlocks().get(j).getRefBlock().getIndex() == d.getBlocks().get(i).getIndex()))) {
					// keep this semantic relatedness
					d.getPrunnedBlockRelatedness()[i][j] = d.getBlockRelatedness()[i][j];
					d.getPrunnedBlockRelatedness()[j][i] = d.getPrunnedBlockRelatedness()[i][j];
				} else {
					// prune this semantic relatedness
					d.getPrunnedBlockRelatedness()[i][j] = null;
					d.getPrunnedBlockRelatedness()[j][i] = null;
				}
			}
		}

		// determine intra-block semantic relatedness

		Block prevBlock = null, nextBlock = null;
		// iterate through blocks
		for (Block b : d.getBlocks()) {
			if (b != null) {
				// build link to next block
				nextBlock = null;
				for (int next = d.getBlocks().indexOf(b) + 1; next < d.getBlocks().size(); next++) {
					if (d.getBlocks().get(next) != null) {
						nextBlock = d.getBlocks().get(next);
						break;
					}
				}

				// set semantic relatedness between block's first sentence and
				// previous block
				if (prevBlock != null && b.getSentences().size() > 0) {
					b.setPrevSentenceBlockRelatedness(new SemanticRelatedness(b.getSentences().get(0), prevBlock));
				}

				// set semantic relatedness between block's last sentence and
				// next block
				if (nextBlock != null && b.getSentences().size() > 0) {
					b.setNextSentenceBlockRelatedness(
							new SemanticRelatedness(nextBlock, b.getSentences().get(b.getSentences().size() - 1)));
				}

				// determine sentence-block semantic relatedness

				// initialize semantic relatedness vector for the semantic
				// relatedness of (sentence, block) pairs
				b.setSentenceBlockRelatedness(new SemanticRelatedness[b.getSentences().size()]);
				// iterate through all sentences of the block
				for (int i = 0; i < b.getSentences().size(); i++) {
					// set semantic relatedness between the sentence and the
					// block
					b.getSentenceBlockRelatedness()[i] = new SemanticRelatedness(b.getSentences().get(i), b);
				}

				// determine sentence-sentence semantic relatedness

				// initialize semantic relatedness arrays for the semantic
				// relatedness of (sentence, sentence) pairs
				b.setSentenceRelatedness(new SemanticRelatedness[b.getSentences().size()][b.getSentences().size()]);
				b.setPrunnedSentenceRelatedness(
						new SemanticRelatedness[b.getSentences().size()][b.getSentences().size()]);

				s0 = 0;
				s1 = 0;
				s2 = 0;
				mean = 0;
				stdev = 0;

				// iterate through all pairs of sentences of the block
				for (int i = 0; i < b.getSentences().size() - 1; i++) {
					for (int j = i + 1; j < b.getSentences().size(); j++) {
						// compute semantic relatedness between the sentence and
						// the following blocks
						SemanticRelatedness sim = new SemanticRelatedness(b.getSentences().get(j),
								b.getSentences().get(i));
						b.getSentenceRelatedness()[i][j] = sim;
						b.getSentenceRelatedness()[j][i] = sim;
						if (sim.getRelatedness() != 0) {
							s0++;
							s1 += sim.getRelatedness();
							s2 += Math.pow(sim.getRelatedness(), 2);
						}

					}
				}

				// determine mean and standard deviation values of semantic
				// relatedness
				if (s0 != 0) {
					mean = s1 / s0;
					stdev = Math.sqrt(s0 * s2 - Math.pow(s1, 2)) / s0;
				}

				// prune initial graph, but always keep adjacent pairs of
				// sentences

				// iterate through all pairs of sentences of the block
				for (int i = 0; i < b.getSentences().size() - 1; i++) {
					for (int j = i + 1; j < b.getSentences().size(); j++) {
						// if the semantic relatedness is greater than sum of
						// mean and standard deviation and j is the next
						// sentence after i
						if ((b.getSentenceRelatedness()[i][j].getRelatedness() >= (mean + stdev)) || (j == i + 1)) {
							// keep this semantic similarity
							b.getPrunnedSentenceRelatedness()[i][j] = b.getSentenceRelatedness()[i][j];
							b.getPrunnedSentenceRelatedness()[j][i] = b.getPrunnedSentenceRelatedness()[i][j];
						} else {
							// prune this semantic similarity
							b.getPrunnedSentenceRelatedness()[i][j] = null;
							b.getPrunnedSentenceRelatedness()[j][i] = null;
						}

					}
				}

				// set previous block for linking next blocks to it
				prevBlock = b;
			}
		}

	}
}
