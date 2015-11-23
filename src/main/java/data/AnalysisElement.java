package data;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import org.apache.log4j.Logger;

import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import data.discourse.Topic;
import data.sentiment.SentimentEntity;
import edu.cmu.lti.jawjaw.pobj.Lang;

/**
 * This abstract class is the base for all type of elements. It is extended later for 
 * all processing elements in the following hierarchical order: Document > Block > 
 * Utterance.
 *   
 * @author Mihai Dascalu
 */
public abstract class AnalysisElement implements Serializable {
	
	/**
	 * A version number for the AnaylisElement class
	 */
	private static final long serialVersionUID = -8110285459013257550L;

	/**
	 * The logger instance
	 */
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
	 * Determines the LSA vector for the corresponding analysis element by using local
	 * tf-idf * LSA
	 * Determines the LDA probability distribution.
	 * TODO: explain better
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
	 * Determines number of occurrences for each word in a list of analysis elements.
	 * The method goes through all elements and, for each word, increments the number of 
	 * occurrences in a local variable. 
	 * TODO: word occurences from (Documents from Blocks), (Blocks from Sentences) (mai spunem?)
	 * 
	 * @param elements
	 * 			The list of analysis elements
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
	 * @return map of (word, no_occurrences) associations  
	 */
	public Map<Word, Integer> getWordOccurences() {
		return wordOccurences;
	}

	/**
	 * @param wordOccurences
	 * 			map of (word, no_occurences) associations to be set
	 */
	public void setWordOccurences(Map<Word, Integer> wordOccurences) {
		this.wordOccurences = wordOccurences;
	}

	/**
	 * @return Latent Semantic Analysis object
	 */
	public LSA getLSA() {
		return lsa;
	}

	/**
	 * @param lsa
	 * 			Latent Semantic Analysis object to be set
	 */
	public void setLSA(LSA lsa) {
		this.lsa = lsa;
	}

	/**
	 * @return Latent Dirichlet Allocation object
	 */
	public LDA getLDA() {
		return lda;
	}

	/**
	 * @param lda
	 * 			Latent Dirichlet Allocation object to be set
	 */
	public void setLDA(LDA lda) {
		this.lda = lda;
	}

	/**
	 * @return the language the text is written in
	 */
	public Lang getLanguage() {
		return language;
	}

	/**
	 * @param language
	 * 			the language the text is written in to be set
	 */
	public void setLanguage(Lang language) {
		this.language = language;
	}

	/**
	 * @return Latent Semantic Analysis vector
	 */
	public double[] getLSAVector() {
		return lsaVector;
	}

	/**
	 * @param vector
	 * 			Latent Semantic Analysis vector to be set
	 */
	public void setLSAVector(double[] vector) {
		this.lsaVector = vector;
	}

	/**
	 * TODO: e ok explicatia?
	 * @return Latent Dirichlet Allocation Probability Distribution vector
	 */
	public double[] getLDAProbDistribution() {
		return ldaProbDistribution;
	}

	/**
	 * @param ldaProbDistribution
	 * 			Latent Dirichlet Allocation Probability Distribution vector to be set
	 */
	public void setLDAProbDistribution(double[] ldaProbDistribution) {
		this.ldaProbDistribution = ldaProbDistribution;
	}

	/**
	 * @return initial score for the analysis element 
	 */
	public double getIndividualScore() {
		return individualScore;
	}

	/**
	 * @param individualScore
	 * 			score for the analysis element to be set
	 */
	public void setIndividualScore(double individualScore) {
		this.individualScore = individualScore;
	}

	/**
	 * @return total score after augmentation from the cohesion graph
	 */
	public double getOverallScore() {
		return overallScore;
	}

	/**
	 * @param overallScore
	 * 			total score to be set
	 */
	public void setOverallScore(double overallScore) {
		this.overallScore = overallScore;
	}

	/**
	 * @return parsed text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text
	 * 			parsed text to be set
	 */
	public void setText(String text) {
		this.text = text;
		this.alternateText = text;
	}

	/**
	 * @return processed text
	 */
	public String getProcessedText() {
		return processedText;
	}

	/**
	 * @param processedText
	 * 			processed text to be set
	 */
	public void setProcessedText(String processedText) {
		this.processedText = processedText;
	}

	/**
	 * TODO: e ok explicatia?
	 * @return current index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @param index
	 * 			current index to be set
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
