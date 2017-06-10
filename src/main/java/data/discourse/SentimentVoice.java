package data.discourse;

import data.cscl.Participant;

/**
 * @author Florea Anda
 *
 */
public class SentimentVoice {

	private String keyword;
	private double valence;
	private Participant participant;
	private String sentenceOrContext;

	public SentimentVoice(String keyword, double valence, Participant participant, String sentenceOrContext) {
		this.keyword = keyword;
		this.participant = participant;
		this.valence = valence;
		this.sentenceOrContext = sentenceOrContext;
	}

	public String getKeyword() {
		return keyword;
	}

	public double getValence() {
		return valence;
	}

	public Participant getParticipant() {
		return participant;
	}

	public String getSentenceOrContext() {
		return sentenceOrContext;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public void setValence(double valence) {
		this.valence = valence;
	}

	public void setParticipant(Participant participant) {
		this.participant = participant;
	}

	public void setSentenceOrContext(String sentenceOrContext) {
		this.sentenceOrContext = sentenceOrContext;
	}

	public String toString() {
		String s = "(" + keyword + ", " + valence + ", " + participant.getName() + "): " + sentenceOrContext;
		return s;
	}
}
