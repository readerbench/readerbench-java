package services.discourse.cohesion;

import org.apache.log4j.Logger;

import DAO.Block;
import DAO.AbstractDocument;
import DAO.Sentence;
import DAO.Word;
import DAO.discourse.SemanticCohesion;
import DAO.discourse.SemanticSimilarity;

/**
 * 
 * @author Mihai Dascalu
 */
public class CohesionGraph {
	static Logger logger = Logger.getLogger(CohesionGraph.class);

	/**
	 * Build the cohesion graph of a document.
	 * 
	 * @param d
	 * 			the document for which to build the cohesion graph
	 */
	public static void buildCohesionGraph(AbstractDocument d) {

		logger.info("Building cohesion graph...");
		
		
		// determine block-document semantic cohesion
		
		// initialize semantic cohesion vector for the semantic cohesion of (block, document) pairs
		d.setBlockDocDistances(new SemanticCohesion[d.getBlocks().size()]);
		// initialize semantic similarity vector for the semantic cohesion of (block, document) pairs
		d.setBlockDocSimilarities(new SemanticSimilarity[d.getBlocks().size()]);
		// iterate through all blocks of the document
		for (int i = 0; i < d.getBlocks().size(); i++) {
			if (d.getBlocks().get(i) != null) {
				// set semantic cohesion between the block and the document 
				d.getBlockDocDistances()[i] = new SemanticCohesion(d.getBlocks().get(i), d);
				// set semantic similarity between the block and the document
				d.getBlockDocSimilarities()[i] = new SemanticSimilarity(d.getBlocks().get(i), d);
			}
		}
		
		double
			// auxiliary variables used to compute mean and standard deviation of semantic cohesion
			s0 = 0,
			s1 = 0,
			s2 = 0,

			// auxiliary variables used to compute mean and standard deviation of semantic similarity			
			sim0 = 0,
			sim1 = 0,
			sim2 = 0,
			
			// mean and standard deviation of computed semantic cohesion values
			mean = 0,
			stdev = 0,
			
			// mean and standard deviation of computed semantic simiarity values
			meanSim = 0,
			stdevSim = 0;

		
		// determine inner-block semantic cohesion
		
		// initialize semantic cohesion arrays for the semantic cohesion of (block, block) pairs
		d.setBlockDistances(new SemanticCohesion[d.getBlocks().size()][d.getBlocks().size()]);
		d.setPrunnedBlockDistances(new SemanticCohesion[d.getBlocks().size()][d.getBlocks().size()]);
		//initialize semantic similarity arrays for the semantic cohesion of (block, block) pairs
		d.setBlockSimilarities(new SemanticSimilarity[d.getBlocks().size()][d.getBlocks().size()]);
		d.setPrunnedBlockSimilarities(new SemanticSimilarity[d.getBlocks().size()][d.getBlocks().size()]);
		// iterate through all blocks of the document
		for (int i = 0; i < d.getBlocks().size() - 1; i++) {
			if (d.getBlocks().get(i) != null) {
				// retrospective view built on explicit links
				// compute semantic cohesion and similarity between the block and the block it is set to as explicit link
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
						
						SemanticSimilarity sim = new SemanticSimilarity(d.getBlocks().get(j), d.getBlocks().get(i));
						d.getBlockSimilarities()[j][i] = sim;
						d.getBlockSimilarities()[i][j] = sim;
						if (sim.getSimilarity() != 0) {
							sim0++;
							sim1 += sim.getSimilarity();
							sim2 += Math.pow(sim.getSimilarity(), 2);
						}
					}
				}
				
				// prospective view of cohesion and similarity
				// compute semantic cohesion and similarity between the block and the following WINDOW_SIZE blocks
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
						
						SemanticSimilarity sim = new SemanticSimilarity(d.getBlocks().get(j), d.getBlocks().get(i));
						d.getBlockSimilarities()[j][i] = sim;
						d.getBlockSimilarities()[i][j] = sim;
						if (sim.getSimilarity() != 0) {
							sim0++;
							sim1 += sim.getSimilarity();
							sim2 += Math.pow(sim.getSimilarity(), 2);
						}
					}
				}
			}
		}

		
		// determine mean and standard deviation values of semantic cohesion
		if (s0 != 0) {
			mean = s1 / s0;
			stdev = Math.sqrt(s0 * s2 - Math.pow(s1, 2)) / s0;
		}

		// determine mean and standard deviation values of semantic similarity
		if (sim0 != 0) {
			meanSim = sim1 / sim0;
			stdevSim = Math.sqrt(sim0 * sim2 - Math.pow(sim1, 2)) / sim0;
		}
		
		// prune initial graph, but always keep adjacent pairs of blocks or explicitly referred blocks
		
		// iterate through all pairs of blocks of the document
		for (int i = 0; i < d.getBlocks().size() - 1; i++) {
			for (int j = i + 1; j < d.getBlocks().size(); j++) {
				// if the semantic cohesion is set for the pair of blocks (i, j)
				if (d.getBlockDistances()[i][j] != null
						&& (
								// if the semantic cohesion is greater than sum of mean and standard deviation
								(d.getBlockDistances()[i][j].getCohesion() >= (mean + stdev))
								// if j is the next block after i and there is not an explicit link set for j
								|| (d.getBlocks().get(j).getRefBlock() == null
									&& j == i + 1)
								// if there is an explicit link set for j and it is i 
								|| (d.getBlocks().get(j).getRefBlock() != null
									&& d.getBlocks().get(j).getRefBlock().getIndex() == d.getBlocks().get(i).getIndex())
							)
					) {
					// keep this semantic cohesion
					d.getPrunnedBlockDistances()[i][j] = d.getBlockDistances()[i][j];
					d.getPrunnedBlockDistances()[j][i] = d.getPrunnedBlockDistances()[i][j];
				} else {
					// prune this semantic cohesion
					d.getPrunnedBlockDistances()[i][j] = null;
					d.getPrunnedBlockDistances()[j][i] = null;
				}
				
				// if the semantic similarity is set for the pair of blocks (i, j)
				if (d.getBlockSimilarities()[i][j] != null
						&& (
								// if the semantic similarity is greater than sum of mean and standard deviation
								(d.getBlockSimilarities()[i][j].getSimilarity() >= (meanSim + stdevSim))
								// if j is the next block after i and there is not an explicit link set for j
								|| (d.getBlocks().get(j).getRefBlock() == null
									&& j == i + 1)
								// if there is an explicit link set for j and it is i 
								|| (d.getBlocks().get(j).getRefBlock() != null
									&& d.getBlocks().get(j).getRefBlock().getIndex() == d.getBlocks().get(i).getIndex())
							)
					) {
					// keep this semantic similarity
					d.getPrunnedBlockSimilarities()[i][j] = d.getBlockSimilarities()[i][j];
					d.getPrunnedBlockSimilarities()[j][i] = d.getPrunnedBlockSimilarities()[i][j];
				} else {
					// prune this semantic similarity
					d.getPrunnedBlockSimilarities()[i][j] = null;
					d.getPrunnedBlockSimilarities()[j][i] = null;
				}
			}
		}

		// determine intra-block distances (semantic cohesion and similarity)
		
		Block prevBlock = null, nextBlock = null;
		// iterate through blocks
		for (Block b : d.getBlocks()) {
			if (b != null) {
				
				// determine title similarity only if title !=null
				if (d.getTitle() != null) {
					// iterate through sentences of block
					for (Sentence s : b.getSentences()) {
						s.setTitleSimilarity(new SemanticCohesion(s, d.getTitle()));
					}
				}

				// build link to next block
				nextBlock = null;
				for (int next = d.getBlocks().indexOf(b) + 1; next < d.getBlocks().size(); next++) {
					if (d.getBlocks().get(next) != null) {
						nextBlock = d.getBlocks().get(next);
						break;
					}
				}

				// set semantic cohesion and similarity between block's first sentence and previous block
				if (prevBlock != null && b.getSentences().size() > 0) {
					b.setPrevSentenceBlockDistance(new SemanticCohesion(b.getSentences().get(0), prevBlock));
					b.setPrevSentenceBlockSimilarity(new SemanticSimilarity(b.getSentences().get(0), prevBlock));
				}
				
				// set semantic cohesion and similarity between block's last sentence and next block
				if (nextBlock != null && b.getSentences().size() > 0) {
					b.setNextSentenceBlockDistance(
							new SemanticCohesion(nextBlock, b.getSentences().get(b.getSentences().size() - 1)));
					b.setNextSentenceBlockSimilarity(
							new SemanticSimilarity(nextBlock, b.getSentences().get(b.getSentences().size() - 1)));
				}
				
				// determine sentence-block semantic cohesion and similarity

				// initialize semantic cohesion and similarity vector for the semantic cohesion and similarity of (sentence, block) pairs
				b.setSentenceBlockDistances(new SemanticCohesion[b.getSentences().size()]);
				b.setSentenceBlockSimilarities(new SemanticSimilarity[b.getSentences().size()]);
				// iterate through all sentences of the block
				for (int i = 0; i < b.getSentences().size(); i++) {
					// set semantic cohesion and similarity between the sentence and the block
					b.getSentenceBlockDistances()[i] = new SemanticCohesion(b.getSentences().get(i), b);
					b.getSentenceBlockSimilarities()[i] = new SemanticSimilarity(b.getSentences().get(i), b);
				}

				// determine sentence-sentence semantic cohesion and similarity
				
				// initialize semantic cohesion and similarity arrays for the semantic cohesion and similarity of (sentence, sentence) pairs
				b.setSentenceDistances(new SemanticCohesion[b.getSentences().size()][b.getSentences().size()]);
				b.setPrunnedSentenceDistances(new SemanticCohesion[b.getSentences().size()][b.getSentences().size()]);
				b.setSentenceSimilarities(new SemanticSimilarity[b.getSentences().size()][b.getSentences().size()]);
				b.setPrunnedSentenceSimilarities(new SemanticSimilarity[b.getSentences().size()][b.getSentences().size()]);

				s0 = 0;
				s1 = 0;
				s2 = 0;
				mean = 0;
				stdev = 0;
				
				sim0 = 0;
				sim1 = 0;
				sim2 = 0;
				meanSim = 0;
				stdevSim = 0;
				
				// iterate through all pairs of sentences of the block
				for (int i = 0; i < b.getSentences().size() - 1; i++) {
					for (int j = i + 1; j < b.getSentences().size(); j++) {
						// compute semantic cohesion between the sentence and the following blocks
						SemanticCohesion coh = new SemanticCohesion(b.getSentences().get(j), b.getSentences().get(i));
						b.getSentenceDistances()[i][j] = coh;
						b.getSentenceDistances()[j][i] = coh;
						if (coh.getCohesion() != 0) {
							s0++;
							s1 += coh.getCohesion();
							s2 += Math.pow(coh.getCohesion(), 2);
						}

						// compute semantic similarity between the sentence and the following blocks
						SemanticSimilarity sim = new SemanticSimilarity(b.getSentences().get(j), b.getSentences().get(i));
						b.getSentenceSimilarities()[i][j] = sim;
						b.getSentenceSimilarities()[j][i] = sim;
						if (sim.getSimilarity() != 0) {
							sim0++;
							sim1 += sim.getSimilarity();
							sim2 += Math.pow(sim.getSimilarity(), 2);
						}

					}
				}
				
				// determine mean and standard deviation values of semantic cohesion
				if (s0 != 0) {
					mean = s1 / s0;
					stdev = Math.sqrt(s0 * s2 - Math.pow(s1, 2)) / s0;
				}

				// determine mean and standard deviation values of semantic similarity
				if (sim0 != 0) {
					meanSim = sim1 / sim0;
					stdevSim = Math.sqrt(sim0 * sim2 - Math.pow(sim1, 2)) / sim0;
				}

				// prune initial graph, but always keep adjacent pairs of sentences
				
				// iterate through all pairs of sentences of the block				
				for (int i = 0; i < b.getSentences().size() - 1; i++) {
					for (int j = i + 1; j < b.getSentences().size(); j++) {
						// if the semantic cohesion is greater than sum of mean and standard deviation and j is the next sentence after i
						if ((b.getSentenceDistances()[i][j].getCohesion() >= (mean + stdev)) || (j == i + 1)) {
							// keep this semantic cohesion							
							b.getPrunnedSentenceDistances()[i][j] = b.getSentenceDistances()[i][j];
							b.getPrunnedSentenceDistances()[j][i] = b.getPrunnedSentenceDistances()[i][j];
						} else {
							// prune this semantic cohesion							
							b.getPrunnedSentenceDistances()[i][j] = null;
							b.getPrunnedSentenceDistances()[j][i] = null;
						}
						
						// if the semantic similarity is greater than sum of mean and standard deviation and j is the next sentence after i
						if ((b.getSentenceSimilarities()[i][j].getSimilarity() >= (meanSim + stdevSim)) || (j == i + 1)) {
							// keep this semantic similarity
							b.getPrunnedSentenceSimilarities()[i][j] = b.getSentenceSimilarities()[i][j];
							b.getPrunnedSentenceSimilarities()[j][i] = b.getPrunnedSentenceSimilarities()[i][j];
						} else {
							// prune this semantic similarity
							b.getPrunnedSentenceSimilarities()[i][j] = null;
							b.getPrunnedSentenceSimilarities()[j][i] = null;
						}

					}
				}
				
				// set previous block for linking next blocks to it
				prevBlock = b;
			}
		}		

	}
}
