package DAO;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import DAO.discourse.SemanticCohesion;
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

	// inter-utterance cohesion values
	private SemanticCohesion[][] sentenceDistances;
	private SemanticCohesion[][] prunnedSentenceDistances;
	// cohesion between an utterance and its corresponding block
	private SemanticCohesion[] sentenceBlockDistances;
	private SemanticCohesion prevSentenceBlockDistance, nextSentenceBlockDistance;

	public Block(AnalysisElement d, int index, String text, LSA lsa, LDA lda, Lang lang) {
		super(d, index, text.trim(), lsa, lda, lang);
		this.sentences = new LinkedList<Sentence>();
	}

	public void finalProcessing() {
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
		for (Word w : getWordOccurences().keySet())
			noOccurences += getWordOccurences().get(w);

		return (noOccurences >= 5);
	}

	public static void addBlock(AbstractDocument d, Block b) {
		if (d.getBlocks().size() < b.getIndex() + 1) {
			d.getBlocks().setSize(b.getIndex() + 1);
		}
		d.getBlocks().set(b.getIndex(), b);

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

	public SemanticCohesion[][] getSentenceDistances() {
		return sentenceDistances;
	}

	public void setSentenceDistances(SemanticCohesion[][] sentenceDistances) {
		this.sentenceDistances = sentenceDistances;
	}

	public SemanticCohesion[][] getPrunnedSentenceDistances() {
		return prunnedSentenceDistances;
	}

	public void setPrunnedSentenceDistances(SemanticCohesion[][] prunnedSentenceDistances) {
		this.prunnedSentenceDistances = prunnedSentenceDistances;
	}

	public SemanticCohesion[] getSentenceBlockDistances() {
		return sentenceBlockDistances;
	}

	public void setSentenceBlockDistances(SemanticCohesion[] sentenceBlockDistances) {
		this.sentenceBlockDistances = sentenceBlockDistances;
	}

	public boolean isFollowedByVerbalization() {
		return isFollowedByVerbalization;
	}

	public void setFollowedByVerbalization(boolean isFollowedByVerbalization) {
		this.isFollowedByVerbalization = isFollowedByVerbalization;
	}

	public SemanticCohesion getPrevSentenceBlockDistance() {
		return prevSentenceBlockDistance;
	}

	public void setPrevSentenceBlockDistance(SemanticCohesion prevSentenceBlockDistance) {
		this.prevSentenceBlockDistance = prevSentenceBlockDistance;
	}

	public SemanticCohesion getNextSentenceBlockDistance() {
		return nextSentenceBlockDistance;
	}

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
