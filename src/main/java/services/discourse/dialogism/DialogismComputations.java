package services.discourse.dialogism;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import services.commons.VectorAlgebra;
import data.AbstractDocument;
import data.AnalysisElement;
import data.Block;
import data.Sentence;
import data.Word;
import data.cscl.CSCLIndices;
import data.cscl.Conversation;
import data.cscl.Participant;
import data.cscl.Utterance;
import data.discourse.SemanticChain;
import data.lexicalChains.LexicalChain;

public class DialogismComputations {
	static Logger logger = Logger.getLogger(DialogismComputations.class);

	public static final int WINDOW_SIZE = 5; // no sentences
	public static final int MAXIMUM_INTERVAL = 60; // seconds

	public static void determineVoices(AbstractDocument d) {
		// merge chains based on LSA / LDA in order to generate semantic
		// chains
		logger.info("Starting to assess voices by first building semantic chains");
		List<SemanticChain> semanticChains = new LinkedList<SemanticChain>();
		for (LexicalChain chain : d.getLexicalChains()) {
			SemanticChain newChain = new SemanticChain(chain, d.getLSA(), d.getLDA());
			newChain.updateSemanticRepresentation();
			semanticChains.add(newChain);
		}

		if (semanticChains.size() > 0) {
			boolean modified = true;
			List<SemanticChain> newSemanticChains = null;

			while (modified) {
				modified = false;
				newSemanticChains = new LinkedList<SemanticChain>();
				for (int i = 0; i < semanticChains.size() - 1; i++) {
					if (semanticChains.get(i) != null) {
						boolean alreadyAdded = false;
						double simMax = -1;
						int simMaxIndex = -1;
						for (int j = i + 1; j < semanticChains.size(); j++) {
							double sim = SemanticChain.similarity(semanticChains.get(i), semanticChains.get(j));
							if (sim != -1 && simMax < sim) {
								simMax = sim;
								simMaxIndex = j;
							}
						}
						if (simMaxIndex != -1) {
							SemanticChain newChain = SemanticChain.merge(semanticChains.get(i),
									semanticChains.get(simMaxIndex));
							alreadyAdded = true;
							newSemanticChains.add(newChain);
							// make old reference void
							semanticChains.set(simMaxIndex, null);
						}
						if (!alreadyAdded)
							newSemanticChains.add(semanticChains.get(i));
						modified = modified || alreadyAdded;
					}
				}
				// add last element
				if (semanticChains.get(semanticChains.size() - 1) != null)
					newSemanticChains.add(semanticChains.get(semanticChains.size() - 1));
				semanticChains = newSemanticChains;
			}
		}

		// specify for each word its corresponding semantic chain
		for (SemanticChain chain : semanticChains) {
			for (Word w : chain.getWords()) {
				w.setSemanticChain(chain);
			}
		}
		d.setVoices(semanticChains);
	}

	public static void determineVoiceDistribution(AnalysisElement e, AbstractDocument d) {
		if (d.getVoices() != null && d.getVoices().size() > 0) {
			e.setVoiceDistribution(new double[d.getVoices().size()]);

			for (Word w : e.getWordOccurences().keySet()) {
				double no = 1 + Math.log(e.getWordOccurences().get(w));
				int index = d.getVoices().indexOf(w.getSemanticChain());
				if (index >= 0)
					e.getVoiceDistribution()[index] += no;
			}
		}
	}

	public static void determineVoiceDistributions(AbstractDocument d) {
		logger.info("Identifying voice distributions");
		// determine distribution of each lexical chain
		int noSentences = 0;
		int[][] traceability = new int[d.getBlocks().size()][];
		for (int i = 0; i < d.getBlocks().size(); i++) {
			if (d.getBlocks().get(i) != null) {
				traceability[i] = new int[d.getBlocks().get(i).getSentences().size()];
				for (int j = 0; j < d.getBlocks().get(i).getSentences().size(); j++) {
					traceability[i][j] = noSentences++;
				}
			}
		}

		// build time intervals
		d.setBlockOccurrencePattern(new long[d.getBlocks().size()]);
		if (d instanceof Conversation) {
			Date earlierDate = null, laterDate = null;
			for (int blockIndex = 0; blockIndex < d.getBlocks().size(); blockIndex++) {
				if (d.getBlocks().get(blockIndex) != null) {
					Utterance u = (Utterance) d.getBlocks().get(blockIndex);
					if (earlierDate == null)
						earlierDate = u.getTime();
					else {
						laterDate = u.getTime();
						if (laterDate != null)
							d.getBlockOccurrencePattern()[blockIndex] = Math
									.min((laterDate.getTime() - earlierDate.getTime()) / 1000, 0);
					}
				}
			}
		}

		// determine spread
		if (d.getVoices() != null) {
			for (SemanticChain chain : d.getVoices()) {
				chain.setSentenceDistribution(new double[noSentences]);
				chain.setBlockDistribution(new double[d.getBlocks().size()]);
				Map<String, Integer> voiceOccurrences = new TreeMap<String, Integer>();
				for (Word w : chain.getWords()) {
					int blockIndex = w.getBlockIndex();
					int sentenceIndex = w.getUtteranceIndex();
					// determine spread as 1+log(no_occurences) per sentence
					chain.getSentenceDistribution()[traceability[blockIndex][sentenceIndex]] += 1;
					chain.getBlockDistribution()[blockIndex] += 1;

					// build cumulative importance in terms of sentences in
					// which occurrences have been spotted
					if (voiceOccurrences.containsKey(blockIndex + "_" + sentenceIndex)) {
						voiceOccurrences.put(blockIndex + "_" + sentenceIndex,
								voiceOccurrences.get(blockIndex + "_" + sentenceIndex) + 1);
					} else {
						voiceOccurrences.put(blockIndex + "_" + sentenceIndex, 1);
					}

				}

				for (String key : voiceOccurrences.keySet()) {
					Integer blockIndex = Integer.valueOf(key.substring(0, key.indexOf("_")));
					Integer sentenceIndex = Integer.valueOf(key.substring(key.indexOf("_") + 1));
					Sentence s = d.getBlocks().get(blockIndex).getSentences().get(sentenceIndex);

					if (s.getWords().size() > 0) {
						chain.setAverageImportanceScore(chain.getAverageImportanceScore() + s.getOverallScore()
						// * (1 + Math.log(voiceOccurrences.get(key)))
						);
					}
				}
				// normalize
				if (voiceOccurrences.size() > 0) {
					chain.setAverageImportanceScore(chain.getAverageImportanceScore() / voiceOccurrences.size());
				}

				// normalize occurrences
				// at sentence level
				for (int i = 0; i < chain.getSentenceDistribution().length; i++) {
					if (chain.getSentenceDistribution()[i] > 0)
						chain.getSentenceDistribution()[i] = 1 + Math.log(chain.getSentenceDistribution()[i]);
				}
				// at block level
				for (int i = 0; i < chain.getBlockDistribution().length; i++) {
					if (chain.getBlockDistribution()[i] > 0)
						chain.getBlockDistribution()[i] = 1 + Math.log(chain.getBlockDistribution()[i]);
				}
				// define moving average at block level, relevant for chat
				// conversations
				chain.setBlockMovingAverage(VectorAlgebra.movingAverage(chain.getBlockDistribution(), WINDOW_SIZE,
						d.getBlockOccurrencePattern(), MAXIMUM_INTERVAL));
			}

			// sort semantic chains (voices) by importance
			Collections.sort(d.getVoices());

			// build voice distribution vectors for each block
			for (Block b : d.getBlocks()) {
				if (b != null) {
					determineVoiceDistribution(b, d);
				}
			}
		}
	}

	public static void determineParticipantInterAnimation(Conversation c) {
		if (c.getVoices() == null || c.getVoices().size() == 0)
			return;

		Iterator<Participant> it = c.getParticipants().iterator();
		List<Participant> lsPart = new ArrayList<Participant>();
		while (it.hasNext()) {
			Participant part = it.next();
			lsPart.add(part);
		}
		// take all voices
		for (int i = 0; i < c.getVoices().size(); i++) {
			for (int p1 = 0; p1 < lsPart.size() - 1; p1++) {
				for (int p2 = p1 + 1; p2 < lsPart.size(); p2++) {
					// for different participants build collaboration based
					// on inter-twined voices
					double[] ditrib1 = c.getParticipantBlockMovingAverage(c.getVoices().get(i), lsPart.get(p1));
					double[] ditrib2 = c.getParticipantBlockMovingAverage(c.getVoices().get(i), lsPart.get(p2));
					double addedInterAnimationDegree = VectorAlgebra.mutualInformation(ditrib1, ditrib2);

					lsPart.get(p1).getIndices().put(CSCLIndices.INTER_ANIMATION_DEGREE,
							lsPart.get(p1).getIndices().get(CSCLIndices.INTER_ANIMATION_DEGREE)
									+ addedInterAnimationDegree);
					lsPart.get(p2).getIndices().put(CSCLIndices.INTER_ANIMATION_DEGREE,
							lsPart.get(p2).getIndices().get(CSCLIndices.INTER_ANIMATION_DEGREE)
									+ addedInterAnimationDegree);
				}
			}
		}
	}

	public static void implicitLinksCohesion(AbstractDocument d) {
		// build voice distribution vectors for each block
		logger.info("Comparing implicit links");
		for (Block b : d.getBlocks()) {
			if (b != null && b.getRefBlock() != null) {
				System.out.println(b.getIndex() + "->" + b.getRefBlock().getIndex() + "\t"
						+ VectorAlgebra.pearsonCorrelation(b.getVoiceDistribution(),
								b.getRefBlock().getVoiceDistribution())
						+ "\t"
						+ VectorAlgebra.mutualInformation(b.getVoiceDistribution(),
								b.getRefBlock().getVoiceDistribution())
						+ "\t" + d.getBlockDistances()[d.getBlocks().indexOf(b)][d.getBlocks().indexOf(b.getRefBlock())]
								.getCohesion());
			}
		}
	}
}
