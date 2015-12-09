package services.complexity.coreference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import utils.localization.LocalizationUtils;
import data.Block;
import data.AbstractDocument;
import data.Sentence;
import data.Word;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.Dictionaries.MentionType;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.CoreMap;

/***
 * @author Mihai Alexandru Ortelecan
 */
public class CoreferenceResolutionComplexity extends IComplexityFactors {

	/**
	 * Chains of size <= 1 are ignored
	 * 
	 * @param data
	 * @return total number of coreference chains per document
	 */
	public static int getNoCorefChainsPerDoc(CoreferenceResolutionData data) {
		return data.getNoChains();
	}

	/**
	 * Chains of size <= 1 are ignored
	 * 
	 * @param data
	 * @return the avg. num. of coreferences per chain
	 */
	public static float getAverageCorefsPerChain(CoreferenceResolutionData data) {
		if (data.getNoChains() != 0)
			return (float) data.getNoCoreferences() / data.getNoChains();
		return 0;
	}

	/***
	 * The span of one chain is the distance between the index of the first and
	 * last entity in that chain.
	 * 
	 * @param data
	 * @param doc
	 * @return avg. chain span
	 */

	public static float getAverageChainSpan(CoreferenceResolutionData data) {
		if (data.getNoChains() != 0)
			return (float) data.getTotalSizeOfSpan() / data.getNoChains();
		return 0;
	}

	/**
	 * A chain is defined to be active for a word or an entity if this chain
	 * passes through its current location. We ignore the chains with size <= 1.
	 * 
	 * @param data
	 * @return nr of active coreferences chains per word
	 */
	public static float getNoActiveCorefChainsPerWord(
			CoreferenceResolutionData data) {
		if (data.getNoWords() != 0)
			return (float) data.getNoChains() / data.getNoWords();
		return 0;
	}

	/***
	 * Big span is considered a span >= CoreferenceResolutionData.PROPORTION *
	 * documentLength .The document length is the length of all the words it
	 * contains(words from utterance.getWords)
	 * 
	 * @param data
	 * @return nr of chains with big span
	 */
	public static int getNoCorefChainsWithBigSpan(CoreferenceResolutionData data) {
		return data.getNoChainsWithBigSpan();
	}

	/**
	 * Search all the pronominal mentions. For each of those, calculate the
	 * distance to nearest referent and return the mean value of those
	 * distances.
	 * 
	 * @param blocks
	 * @return inference distance
	 */
	public static float getAverageInferenceDistancePerChain(List<Block> blocks) {
		float inferenceDistance = 0;
		int chains = 0;

		for (Block block : blocks) {
			if (block != null) {
				Map<Integer, CorefChain> coref = block.getCorefs();
				if (coref != null) {
					for (Map.Entry<Integer, CorefChain> entry : coref
							.entrySet()) {
						CorefChain c = entry.getValue();
						if (c.getMentionsInTextualOrder().size() > 1) {
							inferenceDistance += getInferenceDistance(c,
									block.getStanfordSentences());
							chains++;
						}
					}
				}
			}
		}
		if (chains != 0)
			return (float) Math.round(inferenceDistance) / chains;
		return 0;
	}

	/**
	 * see getAvgInferenceDistancePerChain for inference distance definition
	 * 
	 * @param c
	 * @param sentences
	 * @return inference distance
	 */
	private static float getInferenceDistance(CorefChain c,
			List<CoreMap> sentences) {

		List<CorefMention> mentions = c.getMentionsInTextualOrder();
		ArrayList<Integer> pronounMentions = new ArrayList<Integer>();
		int totalDistance = 0;

		for (int i = 0; i < mentions.size(); i++) {
			CorefMention corefMention = mentions.get(i);
			if (corefMention.mentionType == MentionType.PRONOMINAL) {
				pronounMentions.add(i);
			}
		}

		for (int i = 0; i < pronounMentions.size(); i++) {
			int minDistance = Math.min(
					searchReferentLeft(pronounMentions.get(i), mentions,
							sentences),
					searchReferentRight(pronounMentions.get(i), mentions,
							sentences));
			if (minDistance == Integer.MAX_VALUE)
				minDistance = 0;
			totalDistance += minDistance;
		}

		if (pronounMentions.isEmpty()) {
			return 0;
		}
		return (float) totalDistance / pronounMentions.size();
	}

	/**
	 * Searches in the coref chain the nearest right side referent.
	 * 
	 * @param indexOfMention
	 *            The mention with type PRONOMINAL
	 * @param mentions
	 * @param sentences
	 * @return
	 */

	private static int searchReferentRight(Integer indexOfMention,
			List<CorefMention> mentions, List<CoreMap> sentences) {

		for (int i = indexOfMention + 1; i < mentions.size(); i++) {
			CorefMention mention = mentions.get(i);
			if (mention.mentionType != MentionType.PRONOMINAL) {
				return getDistanceBetweenMentions(sentences,
						mentions.get(indexOfMention), mention);
			}
		}
		return Integer.MAX_VALUE;
	}

	/**
	 * Searches in the coref chain the nearest left side referent
	 * 
	 * @param indexOfMention
	 *            The mention with type PRONOMINAL
	 * @param mentions
	 * @param sentences
	 * @return
	 */
	private static int searchReferentLeft(Integer indexOfMention,
			List<CorefMention> mentions, List<CoreMap> sentences) {

		for (int i = indexOfMention - 1; i >= 0; i--) {
			CorefMention mention = mentions.get(i);
			if (mention.mentionType != MentionType.PRONOMINAL) {
				return getDistanceBetweenMentions(sentences, mention,
						mentions.get(indexOfMention));
			}
		}
		return Integer.MAX_VALUE;
	}

	/***
	 * computes the distance between two words from the text.Distance is defined
	 * by the length of all the words between the two given ones(all words
	 * contained in utterace.allWords) Ex: John bought himself a book ====>
	 * distance(John,himself) = sizeof(bought) = 6
	 * 
	 * @param c
	 * @param sentences
	 * @return
	 */
	private static int getCorefSpan(CorefChain c, List<CoreMap> sentences) {
		List<CorefMention> mentions = c.getMentionsInTextualOrder();
		CorefMention first = mentions.get(0);
		CorefMention last = mentions.get(mentions.size() - 1);

		return getDistanceBetweenMentions(sentences, first, last);
	}

	/**
	 * Computes the distance between two mentions(first and last) in number of
	 * characters excepting spaces. The order of the arguments first and last
	 * it's important, meaning that first has to be before last in the sentence
	 * 
	 * @param sentences
	 * @param first
	 * @param last
	 * @return
	 */
	private static int getDistanceBetweenMentions(List<CoreMap> sentences,
			CorefMention first, CorefMention last) {

		int distance = 0;
		int sentOfFirst = first.sentNum - 1;
		int sentOfLast = last.sentNum - 1;

		if (sentOfFirst == sentOfLast) {
			List<CoreLabel> tks = sentences.get(sentOfFirst).get(
					TokensAnnotation.class);
			for (int i = first.endIndex - 1; i < last.startIndex - 1; i++) {
				distance += tks.get(i).get(TextAnnotation.class).length();
			}
		} else {
			/* compute the distance in the first sentence of the first mention */
			List<CoreLabel> tks = sentences.get(sentOfFirst).get(
					TokensAnnotation.class);
			for (int i = first.endIndex - 1; i < tks.size(); i++) {
				distance += tks.get(i).get(TextAnnotation.class).length();
			}
			/* compute the distance in the last sentence of the last mention */
			tks = sentences.get(sentOfLast).get(TokensAnnotation.class);
			for (int i = 0; i < last.startIndex - 1; i++) {
				distance += tks.get(i).get(TextAnnotation.class).length();
			}
			/**/
			for (int i = sentOfFirst + 1; i < sentOfLast; i++) {
				distance += sentences.get(i).get(TextAnnotation.class).length();
			}
		}
		return distance;
	}

	/**
	 * Extract the data needed by CoreferenceResolution metrics
	 * 
	 * @param blocks
	 * @return
	 */

	public static CoreferenceResolutionData analyse(List<Block> blocks) {
		CoreferenceResolutionData data = new CoreferenceResolutionData();
		int noWords = 0;
		int noBlocks = 0;
		int noChains = 0;
		int noEntities = 0;
		int noCoreferences = 0;
		int totalSpan = 0;
		int docLength = 0;
		int noBigSpan = 0;

		/* compute the no of words, the no of entities and the doc length */
		for (Block block : blocks) {
			if (block != null) {
				noBlocks++;
				List<Sentence> sentences = block.getSentences();

				for (Sentence s : sentences) {
					List<Word> words = s.getAllWords();
					noWords += words.size();

					for (Word word : words) {
						if (word.getNE() != null && !word.getNE().equals("O")) {
							noEntities++;
						}
						docLength += word.getText().length();
					}
				}
			}
		}
		if (noBlocks != 0)
			docLength /= noBlocks;

		/* compute nr of chains ,nr of corefs,total span and bigSpans */
		for (Block block : blocks) {
			if (block != null) {
				Map<Integer, CorefChain> coref = block.getCorefs();
				if (coref != null) {
					for (Map.Entry<Integer, CorefChain> entry : coref
							.entrySet()) {
						CorefChain c = entry.getValue();
						if (c.getMentionsInTextualOrder().size() > 1) {
							int span = getCorefSpan(c,
									block.getStanfordSentences());
							if (span >= docLength
									* CoreferenceResolutionData.PROPORTION)
								noBigSpan++;
							totalSpan += span;
							noCoreferences += c.getMentionsInTextualOrder()
									.size();
							noChains++;
						}
					}
				}
			}
		}

		data.setNoChains(noChains);
		data.setNoCoreferences(noCoreferences);
		data.setNoEntities(noEntities);
		data.setNoWords(noWords);
		data.setTotalSizeOfSpan(totalSpan);
		data.setNoChainsWithBigSpan(noBigSpan);

		return data;
	}

	/***
	 * Static nested class used for computing all the data needed by these
	 * features. We prefer this usage because we don't want to perform all the
	 * operations every time we need a feature
	 * 
	 * @author Mihai Alexandru Ortelecan
	 * 
	 */
	public static class CoreferenceResolutionData {

		/*
		 * A big span is considered to be a span with size >= PROPORTION *
		 * docLenght
		 */
		public static final float PROPORTION = 0.3f;

		/* no... per document */
		private int noChains;
		private int noCoreferences;
		private int noEntities;
		private int noWords;
		/* the spans of every chain summed */
		private int totalSizeOfSpan;

		/* big span means that the span is >= PROPORTION * docLength */
		private int noChainsWithBigSpan;

		public int getNoChains() {
			return noChains;
		}

		public void setNoChains(int noChains) {
			this.noChains = noChains;
		}

		public int getNoCoreferences() {
			return noCoreferences;
		}

		public void setNoCoreferences(int noCoreferences) {
			this.noCoreferences = noCoreferences;
		}

		public int getNoEntities() {
			return noEntities;
		}

		public void setNoEntities(int noEntities) {
			this.noEntities = noEntities;
		}

		public int getNoWords() {
			return noWords;
		}

		public void setNoWords(int noWords) {
			this.noWords = noWords;
		}

		public int getTotalSizeOfSpan() {
			return totalSizeOfSpan;
		}

		public void setTotalSizeOfSpan(int totalSizeOfSpan) {
			this.totalSizeOfSpan = totalSizeOfSpan;
		}

		public int getNoChainsWithBigSpan() {
			return noChainsWithBigSpan;
		}

		public void setNoChainsWithBigSpan(int noChainsWithBigSpan) {
			this.noChainsWithBigSpan = noChainsWithBigSpan;
		}

	}

	
	public String getClassName() {
		return LocalizationUtils.getTranslation("Coreference Complexity Factors (EN only)");
	}

	
	public void setComplexityIndexDescription(String[] descriptions) {
		descriptions[ComplexityIndices.TOTAL_NO_COREF_CHAINS_PER_DOC] = LocalizationUtils.getTranslation("Total number of coreference chains per document (EN only)");
		descriptions[ComplexityIndices.AVERAGE_NO_COREFS_PER_CHAIN] = LocalizationUtils.getTranslation("Average number of coreferences per chain (EN only)");
		descriptions[ComplexityIndices.AVERAGE_CHAIN_SPAN] = LocalizationUtils.getTranslation("Average coreference chain span (EN only)");
		descriptions[ComplexityIndices.NO_COREF_CHAINS_WITH_BIG_SPAN] = LocalizationUtils.getTranslation("Number of coreference chains with a big span (EN only)");
		descriptions[ComplexityIndices.AVERAGE_INFERENCE_DISTANCE_PER_CHAIN] = LocalizationUtils.getTranslation("Average inference distance per coreference chain (EN only)");
		descriptions[ComplexityIndices.NO_ACTIVE_COREF_CHAINS_PER_WORD] = LocalizationUtils.getTranslation("Number of active coreference chains per word (EN only)");
	}
	public void setComplexityIndexAcronym(String[] acronyms) {
		acronyms[ComplexityIndices.TOTAL_NO_COREF_CHAINS_PER_DOC] = this.getComplexityIndexAcronym("TOTAL_NO_COREF_CHAINS_PER_DOC");
		acronyms[ComplexityIndices.AVERAGE_NO_COREFS_PER_CHAIN] = this.getComplexityIndexAcronym("AVERAGE_NO_COREFS_PER_CHAIN");
		acronyms[ComplexityIndices.AVERAGE_CHAIN_SPAN] = this.getComplexityIndexAcronym("AVERAGE_CHAIN_SPAN");
		acronyms[ComplexityIndices.NO_COREF_CHAINS_WITH_BIG_SPAN] = this.getComplexityIndexAcronym("NO_COREF_CHAINS_WITH_BIG_SPAN");
		acronyms[ComplexityIndices.AVERAGE_INFERENCE_DISTANCE_PER_CHAIN] = this.getComplexityIndexAcronym("AVERAGE_INFERENCE_DISTANCE_PER_CHAIN");
		acronyms[ComplexityIndices.NO_ACTIVE_COREF_CHAINS_PER_WORD] = this.getComplexityIndexAcronym("NO_ACTIVE_COREF_CHAINS_PER_WORD");
	}

	
	public void computeComplexityFactors(AbstractDocument d) {
		switch (d.getLanguage()) {
		case fr:
			d.getComplexityIndices()[ComplexityIndices.TOTAL_NO_COREF_CHAINS_PER_DOC] = ComplexityIndices.IDENTITY;
			d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_COREFS_PER_CHAIN] = ComplexityIndices.IDENTITY;
			d.getComplexityIndices()[ComplexityIndices.AVERAGE_CHAIN_SPAN] = ComplexityIndices.IDENTITY;
			d.getComplexityIndices()[ComplexityIndices.NO_COREF_CHAINS_WITH_BIG_SPAN] = ComplexityIndices.IDENTITY;
			d.getComplexityIndices()[ComplexityIndices.AVERAGE_INFERENCE_DISTANCE_PER_CHAIN] = ComplexityIndices.IDENTITY;
			d.getComplexityIndices()[ComplexityIndices.NO_ACTIVE_COREF_CHAINS_PER_WORD] = ComplexityIndices.IDENTITY;
			break;
		default:
			CoreferenceResolutionData coreferenceData = CoreferenceResolutionComplexity
					.analyse(d.getBlocks());
			d.getComplexityIndices()[ComplexityIndices.TOTAL_NO_COREF_CHAINS_PER_DOC] = CoreferenceResolutionComplexity
					.getNoCorefChainsPerDoc(coreferenceData);
			d.getComplexityIndices()[ComplexityIndices.AVERAGE_NO_COREFS_PER_CHAIN] = CoreferenceResolutionComplexity
					.getAverageCorefsPerChain(coreferenceData);
			d.getComplexityIndices()[ComplexityIndices.AVERAGE_CHAIN_SPAN] = CoreferenceResolutionComplexity
					.getAverageChainSpan(coreferenceData);
			d.getComplexityIndices()[ComplexityIndices.NO_COREF_CHAINS_WITH_BIG_SPAN] = CoreferenceResolutionComplexity
					.getNoCorefChainsWithBigSpan(coreferenceData);
			d.getComplexityIndices()[ComplexityIndices.AVERAGE_INFERENCE_DISTANCE_PER_CHAIN] = CoreferenceResolutionComplexity
					.getAverageInferenceDistancePerChain(d.getBlocks());
			d.getComplexityIndices()[ComplexityIndices.NO_ACTIVE_COREF_CHAINS_PER_WORD] = CoreferenceResolutionComplexity
					.getNoActiveCorefChainsPerWord(coreferenceData);
		}
	}

	
	public int[] getIDs() {
		return new int[] { ComplexityIndices.TOTAL_NO_COREF_CHAINS_PER_DOC,
				ComplexityIndices.AVERAGE_NO_COREFS_PER_CHAIN,
				ComplexityIndices.AVERAGE_CHAIN_SPAN,
				ComplexityIndices.NO_COREF_CHAINS_WITH_BIG_SPAN,
				ComplexityIndices.AVERAGE_INFERENCE_DISTANCE_PER_CHAIN,
				ComplexityIndices.NO_ACTIVE_COREF_CHAINS_PER_WORD };
	}
}