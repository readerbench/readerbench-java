package DAO.cscl;

import java.util.Date;

import DAO.Block;
import DAO.Sentence;

public class Utterance extends Block {

	private static final long serialVersionUID = -8055024257921316162L;

	// chat specific inputs
	private Participant participant;
	private Date time;
	// collaboration assessment in terms of Knowledge Building
	private double KB;
	private double socialKB;
	private double personalKB;

	public Utterance(Block b, Participant p, Date time) {
		super(b.getContainer(), b.getIndex(), b.getText(), b.getLSA(), b.getLDA(), b.getLanguage());
		// inherit all attributes
		setSentences(b.getSentences());
		setRefBlock(b.getRefBlock());
		setFollowedByVerbalization(b.isFollowedByVerbalization());
		setAnnotation(b.getAnnotation());
		setCorefs(b.getCorefs());
		setStanfordSentences(b.getStanfordSentences());
		setWordOccurences(b.getWordOccurences());
		setLSAVector(b.getLSAVector());
		setLDAProbDistribution(b.getLDAProbDistribution());
		setProcessedText(b.getProcessedText());
		this.participant = p;
		this.time = time;
	}

	public Participant getParticipant() {
		return participant;
	}

	public void setParticipant(Participant participant) {
		this.participant = participant;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public double getKB() {
		return KB;
	}

	public void setKB(double kB) {
		KB = kB;
	}

	public double getSocialKB() {
		return socialKB;
	}

	public void setSocialKB(double socialKB) {
		this.socialKB = socialKB;
	}

	public double getPersonalKB() {
		return personalKB;
	}

	public void setPersonalKB(double personalKB) {
		this.personalKB = personalKB;
	}

	public boolean isEligible(Date startDate, Date endDate) {
		return ((startDate == null) || ((startDate != null) && time.after(startDate)))
				&& ((endDate == null) || ((endDate != null) && time.before(endDate)));
	}

	@Override
	public String toString() {
		String s = "";
		if (participant != null)
			s += participant.getName();
		if (time != null)
			s += "(" + time + ")";
		if (!s.equals(""))
			s += ":\n";
		s += "{\n";
		for (Sentence sentence : getSentences())
			s += "\t" + sentence.toString() + "\n";
		s += "}\n[" + getOverallScore() + "]\n";
		return s;
	}

}
