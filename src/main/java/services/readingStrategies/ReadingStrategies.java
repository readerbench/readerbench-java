package services.readingStrategies;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import DAO.Block;
import DAO.Sentence;
import DAO.Word;
import DAO.document.Summary;
import DAO.document.Metacognition;

public class ReadingStrategies {
	static Logger logger = Logger.getLogger(ReadingStrategies.class);

	private static int id = 0;
	public static final int PARAPHRASE = id++;
	public static final int CAUSALITY = id++;
	public static final int BRIDGING = id++;
	public static final int TEXT_BASED_INFERENCES = id++;
	public static final int INFERRED_KNOWLEDGE = id++;
	public static final int CONTROL = id++;
	public static final int NO_READING_STRATEGIES = id;
	public static final String[] STRATEGY_NAMES = { "Paraphrase", "Causality",
			"Text based inferences", "Bridging", "Inferred Knowledge",
			"Control" };

	public static void detReadingStrategies(Metacognition metacognition) {
		logger.info("Identifying reading strategies from verbalizations");

		metacognition.setAutomaticReadingStrategies(new int[metacognition
				.getBlocks().size()][ReadingStrategies.NO_READING_STRATEGIES]);

		// clear references of words in initial document
		for (Block b : metacognition.getBlocks()) {
			for (Sentence s : b.getSentences()) {
				for (Word w : s.getAllWords()) {
					w.setReadingStrategies(new boolean[ReadingStrategies.NO_READING_STRATEGIES]);
				}
				s.setAlternateText(s.getText());
			}
			b.setAlternateText(b.getText());
		}

		ParaphrasingStrategy paraphrasingStg = new ParaphrasingStrategy();
		InferredKnowledgeStrategy KIStg = new InferredKnowledgeStrategy();
		BridgingStrategy bridgingStg = new BridgingStrategy();

		int startIndex = 0;
		int endIndex = 0;
		double threshold = bridgingStg.determineThreshold(metacognition);
		List<Sentence> prevSentences = null;

		for (int i = 0; i < metacognition.getBlocks().size(); i++) {
			Block v = metacognition.getBlocks().get(i);
			// build list of previous blocks
			List<Sentence> crtSentences = new LinkedList<Sentence>();
			endIndex = v.getRefBlock().getIndex();
			for (int refBlockId = startIndex; refBlockId <= endIndex; refBlockId++) {
				for (Sentence s : metacognition.getReferredDoc().getBlocks()
						.get(refBlockId).getSentences()) {
					crtSentences.add(s);
				}
			}

			// afterwards causality and control
			metacognition.getAutomaticReadingStrategies()[i][ReadingStrategies.CAUSALITY] = PatternMatching
					.containsStrategy(crtSentences, v,
							PatternMatching.Strategy.CAUSALITY, true);
			metacognition.getAutomaticReadingStrategies()[i][ReadingStrategies.CONTROL] = PatternMatching
					.containsStrategy(crtSentences, v,
							PatternMatching.Strategy.CONTROL, true);

			// in the end determine paraphrases and inferred concepts as links
			// to previous paragraphs
			for (Sentence s : crtSentences) {
				paraphrasingStg.conceptsInCommon(v, s);
			}

			boolean isPrevParaphrase = false;
			for (Sentence s1 : v.getSentences()) {
				if (s1 != null) {
					for (Word w1 : s1.getWords()) {
						if (w1.getReadingStrategies()[ReadingStrategies.PARAPHRASE]) {
							if (!isPrevParaphrase) {
								metacognition.getAutomaticReadingStrategies()[i][ReadingStrategies.PARAPHRASE]++;
							}
							isPrevParaphrase = true;
						} else {
							isPrevParaphrase = false;
						}
					}
				}
			}

			metacognition.getAutomaticReadingStrategies()[i][ReadingStrategies.INFERRED_KNOWLEDGE] += KIStg
					.getInferredConcepts(v, crtSentences);
			// lastly perform bridging

			if (prevSentences == null)
				prevSentences = crtSentences;
			else
				prevSentences.addAll(crtSentences);
			metacognition.getAutomaticReadingStrategies()[i][ReadingStrategies.BRIDGING] = bridgingStg
					.containsStrategy(v, prevSentences, threshold);

			metacognition.getAutomaticReadingStrategies()[i][ReadingStrategies.TEXT_BASED_INFERENCES] = metacognition
					.getAutomaticReadingStrategies()[i][ReadingStrategies.BRIDGING]
					+ metacognition.getAutomaticReadingStrategies()[i][ReadingStrategies.CAUSALITY];

			startIndex = endIndex + 1;

			prevSentences = crtSentences;
		}

		// clear references of words in initial document
		for (Block b : metacognition.getReferredDoc().getBlocks()) {
			for (Sentence s : b.getSentences()) {
				for (Word w : s.getAllWords()) {
					w.setReadingStrategies(new boolean[ReadingStrategies.NO_READING_STRATEGIES]);
				}
				s.setAlternateText(s.getText());
			}
		}
	}

	public static void detReadingStrategies(Summary essay) {
		logger.info("Identifying reading strategies from essay");

		essay.setAutomaticReadingStrategies(new int[1][ReadingStrategies.NO_READING_STRATEGIES]);

		// clear references of words in initial document
		for (Block b : essay.getBlocks()) {
			for (Sentence s : b.getSentences()) {
				for (Word w : s.getAllWords()) {
					w.setReadingStrategies(new boolean[ReadingStrategies.NO_READING_STRATEGIES]);
				}
				s.setAlternateText(s.getText());
			}
			b.setAlternateText(b.getText());
		}

		ParaphrasingStrategy paraphrasingStg = new ParaphrasingStrategy();
		InferredKnowledgeStrategy KIStg = new InferredKnowledgeStrategy();
		BridgingStrategy bridgingStg = new BridgingStrategy();

		List<Sentence> originalSentences = new LinkedList<Sentence>();
		for (Block b : essay.getReferredDoc().getBlocks()) {
			for (Sentence s : b.getSentences()) {
				originalSentences.add(s);
			}
		}

		for (int i = 0; i < essay.getBlocks().size(); i++) {
			Block e = essay.getBlocks().get(i);
			// causality and control
			essay.getAutomaticReadingStrategies()[0][ReadingStrategies.CAUSALITY] += PatternMatching
					.containsStrategy(originalSentences, e,
							PatternMatching.Strategy.CAUSALITY, false);
			essay.getAutomaticReadingStrategies()[0][ReadingStrategies.CONTROL] += PatternMatching
					.containsStrategy(originalSentences, e,
							PatternMatching.Strategy.CONTROL, false);

			// paraphrases and inferred concepts
			for (Sentence s : originalSentences) {
				paraphrasingStg.conceptsInCommon(e, s);
			}

			boolean isPrevParaphrase = false;
			for (Sentence s1 : e.getSentences()) {
				if (s1 != null) {
					for (Word w1 : s1.getWords()) {
						if (w1.getReadingStrategies()[ReadingStrategies.PARAPHRASE]) {
							if (!isPrevParaphrase) {
								essay.getAutomaticReadingStrategies()[0][ReadingStrategies.PARAPHRASE]++;
							}
							isPrevParaphrase = true;
						} else {
							isPrevParaphrase = false;
						}
					}
				}
			}

			essay.getAutomaticReadingStrategies()[0][ReadingStrategies.INFERRED_KNOWLEDGE] += KIStg
					.getInferredConcepts(e, originalSentences);
		}

		// bridging
		essay.getAutomaticReadingStrategies()[0][ReadingStrategies.BRIDGING] = bridgingStg
				.containsStrategy(essay, originalSentences);

		essay.getAutomaticReadingStrategies()[0][ReadingStrategies.TEXT_BASED_INFERENCES] = essay
				.getAutomaticReadingStrategies()[0][ReadingStrategies.BRIDGING]
				+ essay.getAutomaticReadingStrategies()[0][ReadingStrategies.CAUSALITY];

		// clear references of words in initial document
		for (Block b : essay.getReferredDoc().getBlocks()) {
			for (Sentence s : b.getSentences()) {
				for (Word w : s.getAllWords()) {
					w.setReadingStrategies(new boolean[ReadingStrategies.NO_READING_STRATEGIES]);
				}
				s.setAlternateText(s.getText());
			}
		}
	}
}
