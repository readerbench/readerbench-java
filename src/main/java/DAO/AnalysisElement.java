package DAO;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import DAO.discourse.Topic;
import DAO.sentiment.SentimentEntity;
import edu.cmu.lti.jawjaw.pobj.Lang;

/**
 * 
 * @author Mihai Dascalu
 */
// abstract class extended later on for all processing elements Document > Block
// > Utterance
public abstract class AnalysisElement implements Serializable {
	private static final long serialVersionUID = -8110285459013257550L;

	public static Logger logger = Logger.getLogger(AnalysisElement.class);

	private int index;
	private transient LSA lsa;
	private transient LDA lda;
	private Lang language;
	private AnalysisElement container; // the upper level element in the
										// analysis hierarchy: document > block
										// > utterance
	private String text;
	private String processedText; // lemmas without stop-words and punctuation
	private String alternateText; // text used for display in different colors
	private Map<Word, Integer> wordOccurences;
	private double[] lsaVector;
	private double[] ldaProbDistribution;
	private double individualScore;
	private double overallScore;
	private double[] voiceDistribution;

	private transient double specificity; // specificity score computed for a
											// specific class of topics

	private List<Topic> topics;
	private List<Topic> inferredConcepts;

	private transient SentimentEntity sentimentEntity;

	public AnalysisElement() {
		this.processedText = "";
		this.alternateText = "";
		this.lsaVector = new double[LSA.K];
		this.wordOccurences = new TreeMap<Word, Integer>();
		this.topics = new LinkedList<Topic>();
		this.inferredConcepts = new LinkedList<Topic>();
		this.sentimentEntity = new SentimentEntity();
	}

	public AnalysisElement(LSA lsa, LDA lda, Lang language) {
		this();
		this.lsa = lsa;
		this.lda = lda;
		this.language = language;
	}

	public AnalysisElement(AnalysisElement elem, int index, LSA lsa, LDA lda,
			Lang language) {
		this(lsa, lda, language);
		this.index = index;
		this.container = elem;
	}

	public AnalysisElement(String text, LSA lsa, LDA lda, Lang language) {
		this(lsa, lda, language);
		this.text = text;
		this.alternateText = text;
	}

	public AnalysisElement(AnalysisElement elem, int index, String text,
			LSA lsa, LDA lda, Lang language) {
		this(elem, index, lsa, lda, language);
		this.text = text;
		this.alternateText = text;
	}

	public void determineSemanticDimensions() {
		// determine the vector for the corresponding analysis element by using
		// local TfIdf * LSA
		if (lsa != null) {
			for (Word word : wordOccurences.keySet()) {
				double factor = (1 + Math.log(wordOccurences.get(word))
						* word.getIdf());
				for (int i = 0; i < LSA.K; i++) {
					lsaVector[i] += word.getLSAVector()[i] * factor;
				}
			}
		}

		// determine LDA distribution
		if (lda != null)
			this.ldaProbDistribution = lda.getProbDistribution(this);
	}

	public void determineWordOccurences(List<? extends AnalysisElement> elements) {
		// add all word occurrences from lower level analysis elements
		// (Documents from Blocks), (Blocks from Utterances)
		wordOccurences = new TreeMap<Word, Integer>();
		for (AnalysisElement el : elements) {
			if (el != null) {
				for (Word w : el.getWordOccurences().keySet()) {
					if (wordOccurences.containsKey(w)) {
						wordOccurences.put(w, wordOccurences.get(w)
								+ el.getWordOccurences().get(w));
					} else {
						wordOccurences.put(w, el.getWordOccurences().get(w));
					}
				}
			}
		}
	}

	public Map<Word, Integer> getWordOccurences() {
		return wordOccurences;
	}

	public void setWordOccurences(Map<Word, Integer> wordOccurences) {
		this.wordOccurences = wordOccurences;
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

	public Lang getLanguage() {
		return language;
	}

	public void setLanguage(Lang language) {
		this.language = language;
	}

	public double[] getLSAVector() {
		return lsaVector;
	}

	public void setLSAVector(double[] vector) {
		this.lsaVector = vector;
	}

	public double[] getLDAProbDistribution() {
		return ldaProbDistribution;
	}

	public void setLDAProbDistribution(double[] ldaProbDistribution) {
		this.ldaProbDistribution = ldaProbDistribution;
	}

	public double getIndividualScore() {
		return individualScore;
	}

	public void setIndividualScore(double individualScore) {
		this.individualScore = individualScore;
	}

	public double getOverallScore() {
		return overallScore;
	}

	public void setOverallScore(double overallScore) {
		this.overallScore = overallScore;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		this.alternateText = text;
	}

	public String getProcessedText() {
		return processedText;
	}

	public void setProcessedText(String processedText) {
		this.processedText = processedText;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public AnalysisElement getContainer() {
		return container;
	}

	public void setContainer(AnalysisElement container) {
		this.container = container;
	}

	public String getAlternateText() {
		return alternateText;
	}

	public void setAlternateText(String alternateText) {
		this.alternateText = alternateText;
	}

	public List<Topic> getTopics() {
		return topics;
	}

	public void setTopics(List<Topic> topics) {
		this.topics = topics;
	}

	public List<Topic> getInferredConcepts() {
		return inferredConcepts;
	}

	public void setInferredConcepts(List<Topic> inferredConcepts) {
		this.inferredConcepts = inferredConcepts;
	}

	public double getSpecificity() {
		return specificity;
	}

	public void setSpecificity(double specificity) {
		this.specificity = specificity;
	}

	public double getCombinedScore() {
		return specificity * overallScore;
	}

	public double[] getVoiceDistribution() {
		return voiceDistribution;
	}

	public void setVoiceDistribution(double[] voiceDistribution) {
		this.voiceDistribution = voiceDistribution;
	}

	public SentimentEntity getSentimentEntity() {
		return sentimentEntity;
	}

	public void setSentimentEntity(SentimentEntity sentimentEntity) {
		this.sentimentEntity = sentimentEntity;
	}
}
