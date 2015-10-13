package services.discourse.cohesion;

import org.apache.log4j.Logger;

import DAO.Block;
import DAO.AbstractDocument;
import DAO.Sentence;
import DAO.discourse.SemanticCohesion;

/**
 * 
 * @author Mihai Dascalu
 */
public class CohesionGraph {
	static Logger logger = Logger.getLogger(CohesionGraph.class);

	public static void buildCohesionGraph(AbstractDocument d) {
		// build the cohesion graph
		logger.info("Building cohesion graph");
		d.setBlockDocDistances(new SemanticCohesion[d.getBlocks().size()]);

		for (int i = 0; i < d.getBlocks().size(); i++) {
			if (d.getBlocks().get(i) != null) {
				d.getBlockDocDistances()[i] = new SemanticCohesion(d.getBlocks().get(i), d);
			}
		}

		double s0 = 0, s1 = 0, s2 = 0, mean = 0, stdev = 0;

		// determine inner-block distances
		d.setBlockDistances(new SemanticCohesion[d.getBlocks().size()][d.getBlocks().size()]);
		d.setPrunnedBlockDistances(new SemanticCohesion[d.getBlocks().size()][d.getBlocks().size()]);

		for (int i = 0; i < d.getBlocks().size() - 1; i++) {
			if (d.getBlocks().get(i) != null) {
				// retrospective view built on explicit links
				int j = -1;
				if (d.getBlocks().get(i).getRefBlock() != null) {
					j = d.getBlocks().indexOf(d.getBlocks().get(i).getRefBlock());
					if (j != -1) {
						SemanticCohesion coh = new SemanticCohesion(d.getBlocks().get(j), d.getBlocks().get(i));
						d.getBlockDistances()[j][i] = coh;
						d.getBlockDistances()[i][j] = coh;
						if (coh.getCohesion() != 0) {
							s0++;
							s1 += coh.getCohesion();
							s2 += Math.pow(coh.getCohesion(), 2);
						}
					}
				}
				// prospective view of cohesion
				for (j = i + 1; j < Math.min(d.getBlocks().size(), i + SemanticCohesion.WINDOW_SIZE + 1); j++) {
					if (d.getBlocks().get(j) != null) {
						SemanticCohesion coh = new SemanticCohesion(d.getBlocks().get(j), d.getBlocks().get(i));
						d.getBlockDistances()[i][j] = coh;
						d.getBlockDistances()[j][i] = coh;
						if (coh.getCohesion() != 0) {
							s0++;
							s1 += coh.getCohesion();
							s2 += Math.pow(coh.getCohesion(), 2);
						}
					}
				}
			}
		}

		// determine mean & stdev values
		if (s0 != 0) {
			mean = s1 / s0;
			stdev = Math.sqrt(s0 * s2 - Math.pow(s1, 2)) / s0;
		}

		// prune initial graph but always keep adjacent pairs of blocks or
		// explicitly referred blocks
		for (int i = 0; i < d.getBlocks().size() - 1; i++) {
			for (int j = i + 1; j < d.getBlocks().size(); j++) {
				if (d.getBlockDistances()[i][j] != null
						&& ((d.getBlockDistances()[i][j].getCohesion() >= (mean + stdev))
								|| (d.getBlocks().get(j).getRefBlock() == null
										&& j == i + 1)
						|| (d.getBlocks().get(j).getRefBlock() != null
								&& d.getBlocks().get(j).getRefBlock().getIndex() == d.getBlocks().get(i).getIndex()))) {
					d.getPrunnedBlockDistances()[i][j] = d.getBlockDistances()[i][j];
					d.getPrunnedBlockDistances()[j][i] = d.getPrunnedBlockDistances()[i][j];
				} else {
					d.getPrunnedBlockDistances()[i][j] = null;
					d.getPrunnedBlockDistances()[j][i] = null;
				}
			}
		}

		Block prevBlock = null, nextBlock = null;
		// determine intra-block distances
		for (Block b : d.getBlocks()) {
			if (b != null) {
				// determine title similarity only if title !=null
				if (d.getTitle() != null) {
					for (Sentence s : b.getSentences()) {
						s.setTitleSimilarity(new SemanticCohesion(s, d.getTitle()));
					}
				}

				// build connections to adjacent blocks
				nextBlock = null;
				for (int next = d.getBlocks().indexOf(b) + 1; next < d.getBlocks().size(); next++) {
					if (d.getBlocks().get(next) != null) {
						nextBlock = d.getBlocks().get(next);
						break;
					}
				}

				if (prevBlock != null && b.getSentences().size() > 0) {
					b.setPrevSentenceBlockDistance(new SemanticCohesion(b.getSentences().get(0), prevBlock));
				}
				if (nextBlock != null && b.getSentences().size() > 0) {
					b.setNextSentenceBlockDistance(
							new SemanticCohesion(nextBlock, b.getSentences().get(b.getSentences().size() - 1)));
				}

				// build utterance-block distances
				b.setSentenceBlockDistances(new SemanticCohesion[b.getSentences().size()]);
				for (int i = 0; i < b.getSentences().size(); i++) {
					b.getSentenceBlockDistances()[i] = new SemanticCohesion(b.getSentences().get(i), b);
				}

				// determine utterance distances
				b.setSentenceDistances(new SemanticCohesion[b.getSentences().size()][b.getSentences().size()]);
				b.setPrunnedSentenceDistances(new SemanticCohesion[b.getSentences().size()][b.getSentences().size()]);

				s0 = 0;
				s1 = 0;
				s2 = 0;
				mean = 0;
				stdev = 0;
				for (int i = 0; i < b.getSentences().size() - 1; i++) {
					for (int j = i + 1; j < b.getSentences().size(); j++) {
						SemanticCohesion coh = new SemanticCohesion(b.getSentences().get(j), b.getSentences().get(i));
						b.getSentenceDistances()[i][j] = coh;
						b.getSentenceDistances()[j][i] = coh;
						if (coh.getCohesion() != 0) {
							s0++;
							s1 += coh.getCohesion();
							s2 += Math.pow(coh.getCohesion(), 2);
						}
					}
				}
				// determine mean + stdev values
				if (s0 != 0) {
					mean = s1 / s0;
					stdev = Math.sqrt(s0 * s2 - Math.pow(s1, 2)) / s0;
				}

				// prune initial graph but always keep adjacent pairs of
				// utterances
				for (int i = 0; i < b.getSentences().size() - 1; i++) {
					for (int j = i + 1; j < b.getSentences().size(); j++) {
						if ((b.getSentenceDistances()[i][j].getCohesion() >= (mean + stdev)) || (j == i + 1)) {
							b.getPrunnedSentenceDistances()[i][j] = b.getSentenceDistances()[i][j];
							b.getPrunnedSentenceDistances()[j][i] = b.getPrunnedSentenceDistances()[i][j];
						} else {
							b.getPrunnedSentenceDistances()[i][j] = null;
							b.getPrunnedSentenceDistances()[j][i] = null;
						}
					}
				}
				prevBlock = b;
			}
		}
	}
}
