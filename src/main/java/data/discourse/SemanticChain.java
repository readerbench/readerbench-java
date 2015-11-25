package data.discourse;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import services.commons.ValueComparator;
import services.commons.VectorAlgebra;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import data.Word;
import data.lexicalChains.LexicalChain;
import data.lexicalChains.LexicalChainLink;
import data.sentiment.SentimentEntity;
import data.sentiment.SentimentValence;

public class SemanticChain implements Serializable, Comparable<SemanticChain> {

	private static final long serialVersionUID = -7902005522958585451L;
	private static final double LSA_SIMILARITY_THRESHOLD = 0.75;
	private static final double LDA_SIMILARITY_THRESHOLD = 0.85;

	private transient LSA lsa;
	private transient LDA lda;
	private List<Word> words;
	private Map<String, Double> termOccurrences;
	private double[] lsaVector;
	private double[] ldaProbDistribution;
	private double[] sentenceDistribution;
	private double[] blockDistribution;
	private double[] blockMovingAverage;
	private double averageImportanceScore;

	private boolean NNVB;
	private transient SentimentEntity chainSentiment;

	public SemanticChain(LexicalChain chain, LSA lsa, LDA lda) {
		words = new LinkedList<Word>();
		this.lsa = lsa;
		this.lda = lda;
		for (LexicalChainLink link : chain.getLinks()) {
			words.add(link.getWord());
		}
		this.setChainSentiment(new SentimentEntity());
		this.NNVB = false;
		computeSentimentDistribution();
	}

	private void computeSentimentDistribution() {
		int i = 0;
		int counter = 0;
		for (Word w : words) {
			i++;
			if (w.isNoun() || w.isVerb()) {
				counter++;
			}
		}
		if (counter == words.size())
			NNVB = true;
		else
			NNVB = false;
	}

	public static double similarity(SemanticChain chain1, SemanticChain chain2) {
		// determines whether 2 chains can be merged
		if (chain1 == null || chain2 == null)
			return -1;

		double dist = -1;
		// if words have same lemma
		for (Word w1 : chain1.getWords()) {
			for (Word w2 : chain2.getWords()) {
				if (w1.getLemma().equals(w2.getLemma())) {
					return 1;
				}
			}
		}

		double distLSA = VectorAlgebra.cosineSimilarity(chain1.getLSAVector(), chain2.getLSAVector());
		if (distLSA >= LSA_SIMILARITY_THRESHOLD)
			dist = Math.max(dist, distLSA);

		double distLDA = LDA.getSimilarity(chain1.getLDAProbDistribution(), chain2.getLDAProbDistribution());
		if (distLDA >= LDA_SIMILARITY_THRESHOLD)
			dist = Math.max(dist, distLDA);

		return dist;
	}

	public static SemanticChain merge(SemanticChain chain1, SemanticChain chain2) {
		// copy words from chain 2, update vector
		for (Word w2 : chain2.getWords())
			chain1.getWords().add(w2);
		chain1.updateSemanticRepresentation();

		return chain1;
	}

	public void updateSemanticRepresentation() {
		if (lsa != null) {
			lsaVector = new double[LSA.K];
			for (Word word : words) {
				for (int i = 0; i < LSA.K; i++) {
					lsaVector[i] += word.getLSAVector()[i];
				}
			}
		}

		// determine LDA distribution
		if (lda != null) {
			String text = "";
			for (Word word : words) {
				text += word.getLemma() + " ";
			}
			this.ldaProbDistribution = lda.getProbDistribution(text.trim());
		}

		Map<String, Double> unsortedOccurences = new TreeMap<String, Double>();
		for (Word word : words) {
			if (unsortedOccurences.containsKey(word.getLemma()))
				unsortedOccurences.put(word.getLemma(), unsortedOccurences.get(word.getLemma()) + 1);
			else
				unsortedOccurences.put(word.getLemma(), 1.0);
		}
		ValueComparator<String> kcvc = new ValueComparator<String>(unsortedOccurences);
		termOccurrences = new TreeMap<String, Double>(kcvc);
		termOccurrences.putAll(unsortedOccurences);
	}

	public List<Word> getWords() {
		return words;
	}

	public void setWords(List<Word> words) {
		this.words = words;
	}

	public double[] getLSAVector() {
		return lsaVector;
	}

	public void setLSAVector(double[] lsaVector) {
		this.lsaVector = lsaVector;
	}

	public double[] getLDAProbDistribution() {
		return ldaProbDistribution;
	}

	public void setLDAProbDistribution(double[] ldaProbDistribution) {
		this.ldaProbDistribution = ldaProbDistribution;
	}

	public LSA getLSA() {
		return lsa;
	}

	public void setLSA(LSA lsa) {
		this.lsa = lsa;
	}

	public LDA getLDA() {
		return lda;
	}

	public void setLDA(LDA lda) {
		this.lda = lda;
	}

	public int getNoWords() {
		return words.size();
	}

	public int getNoSentences() {
		int no = 0;
		for (double d : sentenceDistribution) {
			if (d > 0) {
				no++;
			}
		}
		return no;
	}

	public int getNoBlocks() {
		int no = 0;
		for (double d : blockDistribution) {
			if (d > 0) {
				no++;
			}
		}
		return no;
	}

	public double getEntropySentence() {
		return VectorAlgebra.entropy(sentenceDistribution);
	}

	public double getEntropyBlock(boolean useMovingAverage) {
		if (useMovingAverage)
			return VectorAlgebra.entropy(blockMovingAverage);
		return VectorAlgebra.entropy(blockDistribution);
	}

	public double getAvgSentence(boolean useMovingAverage) {
		return VectorAlgebra.avg(sentenceDistribution);
	}

	public double getStdevSentence(boolean useMovingAverage) {
		return VectorAlgebra.stdev(sentenceDistribution);
	}

	public double getAvgBlock() {
		return VectorAlgebra.avg(blockDistribution);
	}

	public double getStdevBlock() {
		return VectorAlgebra.stdev(blockDistribution);
	}

	private double[] getRecurrenceSentence() {
		Vector<Integer> recurrence = new Vector<Integer>();
		int crtIndex = -1;
		for (int i = 0; i < sentenceDistribution.length; i++) {
			if (sentenceDistribution[i] > 0) {
				if (crtIndex == -1)
					crtIndex = i;
				else {
					recurrence.add(i - crtIndex);
					crtIndex = i;
				}
			}
		}
		double[] results = new double[recurrence.size()];
		for (int i = 0; i < recurrence.size(); i++)
			results[i] = recurrence.get(i);
		return results;
	}

	private double[] getRecurrenceBlock() {
		Vector<Integer> recurrence = new Vector<Integer>();
		int crtIndex = -1;
		for (int i = 0; i < blockDistribution.length; i++) {
			if (blockDistribution[i] > 0) {
				if (crtIndex == -1)
					crtIndex = i;
				else {
					recurrence.add(i - crtIndex);
					crtIndex = i;
				}
			}
		}
		double[] results = new double[recurrence.size()];
		for (int i = 0; i < recurrence.size(); i++)
			results[i] = recurrence.get(i);
		return results;
	}

	public double getAvgRecurrenceSentence() {
		return VectorAlgebra.avg(getRecurrenceSentence());
	}

	public double getAvgRecurrenceBlock() {
		return VectorAlgebra.avg(getRecurrenceBlock());
	}

	public double getStdevRecurrenceSentence() {
		return VectorAlgebra.stdev(getRecurrenceSentence());
	}

	public double getStdevRecurrenceBlock() {
		return VectorAlgebra.stdev(getRecurrenceBlock());
	}

	public String toString() {
		String s = "(";
		int noMax = 3, noCrt = 0;
		for (String key : termOccurrences.keySet()) {
			if (noCrt == noMax)
				break;
			s += key + ",";
			noCrt++;
		}
		if (noCrt > 0)
			s = s.substring(0, s.length() - 1);
		s += ")";
		return s;
	}

	public String toStringAllWords() {
		String s = "(";
		for (Word w : words) {
			s += w.getText() + "-" + w.getBlockIndex() + "/" + w.getUtteranceIndex() + ",";
		}
		if (words.size() > 0)
			s = s.substring(0, s.length() - 1);
		s += ")";
		return s;
	}

	public double[] getSentenceDistribution() {
		return sentenceDistribution;
	}

	public void setSentenceDistribution(double[] sentenceDistribution) {
		this.sentenceDistribution = sentenceDistribution;
	}

	public double[] getBlockDistribution() {
		return blockDistribution;
	}

	public void setBlockDistribution(double[] blockDistribution) {
		this.blockDistribution = blockDistribution;
	}

	public double[] getBlockMovingAverage() {
		return blockMovingAverage;
	}

	public void setBlockMovingAverage(double[] blockMovingAverage) {
		this.blockMovingAverage = blockMovingAverage;
	}

	public double getAverageImportanceScore() {
		return averageImportanceScore;
	}

	public void setAverageImportanceScore(double averageImportanceScore) {
		this.averageImportanceScore = averageImportanceScore;
	}

	public SentimentEntity getChainSentiment() {
		return chainSentiment;
	}

	public void setChainSentiment(SentimentEntity chainSentiment) {
		this.chainSentiment = chainSentiment;
	}

	public void setChainSentiment(int sentiment) {
		SentimentEntity sre = new SentimentEntity();
		String s = "";
		for (Word w : words) {
			s = s + w.getText() + " ";
		}
		//sre.addSentimentResultEntity(s, sentiment);
		this.chainSentiment = sre;
	}

	public double getStdevSentiment() {
		return VectorAlgebra.stdev(getSentenceDistribution());
	}

	public boolean hasNounsVerbs() {
		return NNVB;
	}

	@Override
	public int compareTo(SemanticChain o) {
		return (int) (Math.signum(o.getNoWords() - this.getNoWords()));
	}
}
