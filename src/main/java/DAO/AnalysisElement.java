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

	/**
	 * 
	 */
	public AnalysisElement() {
		this.processedText = "";
		this.alternateText = "";
		this.lsaVector = new double[LSA.K];
		this.wordOccurences = new TreeMap<Word, Integer>();
		this.topics = new LinkedList<Topic>();
		this.inferredConcepts = new LinkedList<Topic>();
		this.sentimentEntity = new SentimentEntity();
	}

	/**
	 * @param lsa
	 * @param lda
	 * @param language
	 */
	public AnalysisElement(LSA lsa, LDA lda, Lang language) {
		this();
		this.lsa = lsa;
		this.lda = lda;
		this.language = language;
	}

	/**
	 * @param elem
	 * @param index
	 * @param lsa
	 * @param lda
	 * @param language
	 */
	public AnalysisElement(AnalysisElement elem, int index, LSA lsa, LDA lda,
			Lang language) {
		this(lsa, lda, language);
		this.index = index;
		this.container = elem;
	}

	/**
	 * @param text
	 * @param lsa
	 * @param lda
	 * @param language
	 */
	public AnalysisElement(String text, LSA lsa, LDA lda, Lang language) {
		this(lsa, lda, language);
		this.text = text;
		this.alternateText = text;
	}

	/**
	 * @param elem
	 * @param index
	 * @param text
	 * @param lsa
	 * @param lda
	 * @param language
	 */
	public AnalysisElement(AnalysisElement elem, int index, String text,
			LSA lsa, LDA lda, Lang language) {
		this(elem, index, lsa, lda, language);
		this.text = text;
		this.alternateText = text;
	}

	
	/**
	 * 
	 */
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

	/**
	 * @param elements
	 */
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

	/**
	 * @return
	 */
	public Map<Word, Integer> getWordOccurences() {
		return wordOccurences;
	}

	/**
	 * @param wordOccurences
	 */
	public void setWordOccurences(Map<Word, Integer> wordOccurences) {
		this.wordOccurences = wordOccurences;
	}

	/**
	 * @return
	 */
	public LSA getLSA() {
		return lsa;
	}

	/**
	 * @param lsa
	 */
	public void setLSA(LSA lsa) {
		this.lsa = lsa;
	}

	/**
	 * @return
	 */
	public LDA getLDA() {
		return lda;
	}

	/**
	 * @param lda
	 */
	public void setLDA(LDA lda) {
		this.lda = lda;
	}

	/**
	 * @return
	 */
	public Lang getLanguage() {
		return language;
	}

	/**
	 * @param language
	 */
	public void setLanguage(Lang language) {
		this.language = language;
	}

	/**
	 * @return
	 */
	public double[] getLSAVector() {
		return lsaVector;
	}

	/**
	 * @param vector
	 */
	public void setLSAVector(double[] vector) {
		this.lsaVector = vector;
	}

	/**
	 * @return
	 */
	public double[] getLDAProbDistribution() {
		return ldaProbDistribution;
	}

	/**
	 * @param ldaProbDistribution
	 */
	public void setLDAProbDistribution(double[] ldaProbDistribution) {
		this.ldaProbDistribution = ldaProbDistribution;
	}

	/**
	 * @return
	 */
	public double getIndividualScore() {
		return individualScore;
	}

	/**
	 * @param individualScore
	 */
	public void setIndividualScore(double individualScore) {
		this.individualScore = individualScore;
	}

	/**
	 * @return
	 */
	public double getOverallScore() {
		return overallScore;
	}

	/**
	 * @param overallScore
	 */
	public void setOverallScore(double overallScore) {
		this.overallScore = overallScore;
	}

	/**
	 * @return
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text
	 */
	public void setText(String text) {
		this.text = text;
		this.alternateText = text;
	}

	/**
	 * @return
	 */
	public String getProcessedText() {
		return processedText;
	}

	/**
	 * @param processedText
	 */
	public void setProcessedText(String processedText) {
		this.processedText = processedText;
	}

	/**
	 * @return
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @param index
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * @return
	 */
	public AnalysisElement getContainer() {
		return container;
	}

	/**
	 * @param container
	 */
	public void setContainer(AnalysisElement container) {
		this.container = container;
	}

	/**
	 * @return
	 */
	public String getAlternateText() {
		return alternateText;
	}

	/**
	 * @param alternateText
	 */
	public void setAlternateText(String alternateText) {
		this.alternateText = alternateText;
	}

	/**
	 * @return
	 */
	public List<Topic> getTopics() {
		return topics;
	}

	/**
	 * @param topics
	 */
	public void setTopics(List<Topic> topics) {
		this.topics = topics;
	}

	/**
	 * @return
	 */
	public List<Topic> getInferredConcepts() {
		return inferredConcepts;
	}

	/**
	 * @param inferredConcepts
	 */
	public void setInferredConcepts(List<Topic> inferredConcepts) {
		this.inferredConcepts = inferredConcepts;
	}

	/**
	 * @return
	 */
	public double getSpecificity() {
		return specificity;
	}

	/**
	 * @param specificity
	 */
	public void setSpecificity(double specificity) {
		this.specificity = specificity;
	}

	/**
	 * @return
	 */
	public double getCombinedScore() {
		return specificity * overallScore;
	}

	/**
	 * @return
	 */
	public double[] getVoiceDistribution() {
		return voiceDistribution;
	}

	/**
	 * @param voiceDistribution
	 */
	public void setVoiceDistribution(double[] voiceDistribution) {
		this.voiceDistribution = voiceDistribution;
	}

	/**
	 * @return
	 */
	public SentimentEntity getSentimentEntity() {
		return sentimentEntity;
	}

	/**
	 * @param sentimentEntity
	 */
	public void setSentimentEntity(SentimentEntity sentimentEntity) {
		this.sentimentEntity = sentimentEntity;
	}
}
