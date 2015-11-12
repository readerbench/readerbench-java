package DAO;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import DAO.discourse.SemanticCohesion;
import DAO.discourse.SemanticRelatedness;
import edu.cmu.lti.jawjaw.pobj.Lang;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;

/**
 * 
 * @author Mihai Dascalu
 */
public class Block extends AnalysisElement implements Serializable {
	private static final long serialVersionUID = 8767353039355337678L;

	public static final String SPEAKER_ANNOTATION = "name";
	public static final String TIME_ANNOTATION = "time";
	public static final String ID_ANNOTATION = "id";
	public static final String REF_ANNOTATION = "ref";
	public static final String VERBALIZATION_ANNOTATION = "verba";

	private List<Sentence> sentences;
	private Block refBlock;
	// used for identifying the relationship between the
	// verbalizations and the initial text
	private boolean isFollowedByVerbalization;

	private transient Annotation annotation; // useful for rebuilding
												// coref-chains
	private transient Map<Integer, CorefChain> corefs;
	private transient List<CoreMap> stanfordSentences;

	// inter-sentence cohesion values
	private SemanticCohesion[][] sentenceDistances;
	private SemanticCohesion[][] prunnedSentenceDistances;
	// cohesion between an utterance and its corresponding block
	private SemanticCohesion[] sentenceBlockDistances;
	private SemanticCohesion prevSentenceBlockDistance, nextSentenceBlockDistance;
	
	// inner-sentence semantic similarity values
	private SemanticRelatedness[][] sentenceRelatedness;
	private SemanticRelatedness[][] prunnedSentenceRelatedness;
	// semantic similarity between an utterance and its corresponding block
	private SemanticRelatedness[] sentenceBlockRelatedness;
	private SemanticRelatedness prevSentenceBlockRelatedness, nextSentenceBlockRelatedness;

	public Block(AnalysisElement d, int index, String text, LSA lsa, LDA lda, Lang lang) {
		super(d, index, text.trim(), lsa, lda, lang);
		this.sentences = new LinkedList<Sentence>();
	}

	public void finalProcessing() {
		
		// add sentiment entity to the block
		
		
		// end add sentiment entity to the block
		
		
		setProcessedText(getProcessedText().trim());

		// determine overall word occurrences
		determineWordOccurences(getSentences());

		// determine LSA block vector
		determineSemanticDimensions();
	}

	public boolean isSignificant() {
		// determine if a block is significant from a quantitative point of view
		// useful for eliminating short utterances
		int noOccurences = 0;
		for (Entry<Word, Integer> entry : getWordOccurences().entrySet())
			noOccurences += entry.getValue();

		return (noOccurences >= 5);
	}

	public int noSignificant() {
		// determine if a block is significant from a quantitative point of view
		// useful for eliminating short utterances
		int noOccurences = 0;
		for (Entry<Word, Integer> entry : getWordOccurences().entrySet())
			noOccurences += entry.getValue();

		return noOccurences;
	}

	public static void addBlock(AbstractDocument d, Block b) {
		if (b.getIndex() != -1) {
			if (d.getBlocks().size() < b.getIndex() + 1) {
				d.getBlocks().setSize(b.getIndex() + 1);
			}
			d.getBlocks().set(b.getIndex(), b);
		} else {
			d.getBlocks().add(b);
		}
		d.setProcessedText(d.getProcessedText() + b.getProcessedText() + "\n");

		// sum block vectors
		for (int i = 0; i < LSA.K; i++) {
			d.getLSAVector()[i] += b.getLSAVector()[i];
		}
	}

	public List<Sentence> getSentences() {
		return sentences;
	}

	public void setSentences(List<Sentence> sentences) {
		this.sentences = sentences;
	}

	public Annotation getAnnotation() {
		return annotation;
	}

	public void setAnnotation(Annotation annotation) {
		this.annotation = annotation;
	}

	public Map<Integer, CorefChain> getCorefs() {
		return corefs;
	}

	public void setCorefs(Map<Integer, CorefChain> corefs) {
		this.corefs = corefs;
	}

	public Block getRefBlock() {
		return refBlock;
	}

	public void setRefBlock(Block refBlock) {
		this.refBlock = refBlock;
	}

	public List<CoreMap> getStanfordSentences() {
		return stanfordSentences;
	}

	public void setStanfordSentences(List<CoreMap> sentences) {
		this.stanfordSentences = sentences;
	}

	// public Participant getParticipant() {
	// return participant;
	// }
	//
	// public void setParticipant(Participant participant) {
	// this.participant = participant;
	// }
	//
	// public Date getTime() {
	// return time;
	// }
	//
	// public void setTime(Date time) {
	// this.time = time;
	// }
	//
	// public double getKB() {
	// return KB;
	// }
	//
	// public void setKB(double kB) {
	// KB = kB;
	// }
	//
	// public double getSocialKB() {
	// return socialKB;
	// }
	//
	// public void setSocialKB(double socialKB) {
	// this.socialKB = socialKB;
	// }
	//
	// public double getPersonalKB() {
	// return personalKB;
	// }
	//
	// public void setPersonalKB(double personalKB) {
	// this.personalKB = personalKB;
	// }
	
	/**
	 * @return
	 */
	public SemanticRelatedness[] getSentenceBlockRelatedness() {
		return sentenceBlockRelatedness;
	}
	
	/**
	 * @param sentenceBlockRelatedness
	 */
	public void setSentenceBlockRelatedness(SemanticRelatedness[] sentenceBlockRelatedness) {
		this.sentenceBlockRelatedness = sentenceBlockRelatedness;
	}
	
	/**
	 * @return
	 */
	public SemanticRelatedness[][] getSentenceRelatedness() {
		return sentenceRelatedness;
	}
	
	/**
	 * @param sentenceRelatedness
	 */
	public void setSentenceRelatedness(SemanticRelatedness[][] sentenceRelatedness) {
		this.sentenceRelatedness = sentenceRelatedness;
	}

	/**
	 * @return
	 */
	public SemanticRelatedness[][] getPrunnedSentenceRelatedness() {
		return prunnedSentenceRelatedness;
	}

	/**
	 * @param prunnedSentenceRelatedness
	 */
	public void setPrunnedSentenceRelatedness(SemanticRelatedness[][] prunnedSentenceRelatedness) {
		this.prunnedSentenceRelatedness = prunnedSentenceRelatedness;
	}
	
	/**
	 * @return
	 */
	public SemanticCohesion[] getSentenceBlockDistances() {
		return sentenceBlockDistances;
	}

	/**
	 * @param sentenceBlockDistances
	 */
	public void setSentenceBlockDistances(SemanticCohesion[] sentenceBlockDistances) {
		this.sentenceBlockDistances = sentenceBlockDistances;
	}

	/**
	 * @return
	 */
	public SemanticCohesion[][] getSentenceDistances() {
		return sentenceDistances;
	}
	
	/**
	 * @param sentenceDistances
	 */
	public void setSentenceDistances(SemanticCohesion[][] sentenceDistances) {
		this.sentenceDistances = sentenceDistances;
	}

	/**
	 * @return
	 */
	public SemanticCohesion[][] getPrunnedSentenceDistances() {
		return prunnedSentenceDistances;
	}

	/**
	 * @param prunnedSentenceDistances
	 */
	public void setPrunnedSentenceDistances(SemanticCohesion[][] prunnedSentenceDistances) {
		this.prunnedSentenceDistances = prunnedSentenceDistances;
	}

	/**
	 * @return
	 */
	public boolean isFollowedByVerbalization() {
		return isFollowedByVerbalization;
	}

	/**
	 * @param isFollowedByVerbalization
	 */
	public void setFollowedByVerbalization(boolean isFollowedByVerbalization) {
		this.isFollowedByVerbalization = isFollowedByVerbalization;
	}
	
	/**
	 * @return
	 */
	public SemanticRelatedness getPrevSentenceBlockSimilarity() {
		return prevSentenceBlockRelatedness;
	}

	/**
	 * @param prevSentenceBlockRelatedness
	 */
	public void setPrevSentenceBlockRelatedness(SemanticRelatedness prevSentenceBlockRelatedness) {
		this.prevSentenceBlockRelatedness = prevSentenceBlockRelatedness;
	}

	/**
	 * @return
	 */
	public SemanticRelatedness getNextSentenceBlockRelatedness() {
		return nextSentenceBlockRelatedness;
	}

	/**
	 * @param nextSentenceBlockRelatedness
	 */
	public void setNextSentenceBlockRelatedness(SemanticRelatedness nextSentenceBlockRelatedness) {
		this.nextSentenceBlockRelatedness = nextSentenceBlockRelatedness;
	}

	/**
	 * @return
	 */
	public SemanticCohesion getPrevSentenceBlockDistance() {
		return prevSentenceBlockDistance;
	}

	/**
	 * @param prevSentenceBlockDistance
	 */
	public void setPrevSentenceBlockDistance(SemanticCohesion prevSentenceBlockDistance) {
		this.prevSentenceBlockDistance = prevSentenceBlockDistance;
	}

	/**
	 * @return
	 */
	public SemanticCohesion getNextSentenceBlockDistance() {
		return nextSentenceBlockDistance;
	}

	/**
	 * @param nextSentenceBlockDistance
	 */
	public void setNextSentenceBlockDistance(SemanticCohesion nextSentenceBlockDistance) {
		this.nextSentenceBlockDistance = nextSentenceBlockDistance;
	}

	@Override
	public String toString() {
		String s = "";
		s += "{\n";
		for (Sentence sentence : sentences)
			s += "\t" + sentence.toString() + "\n";
		s += "}\n[" + getOverallScore() + "]\n";
		return s;
	}
}
