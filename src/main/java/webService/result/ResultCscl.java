package webService.result;

import java.util.List;
import java.util.Vector;

public class ResultCscl {

	private ResultTopic participantInteraction;
	private String participantEvolution;
	private List<ResultGraphPoint> socialKB;
	private List<ResultGraphPoint> voiceOverlap;

	public ResultCscl(ResultTopic participantInteraction,
			String participantEvolution,
			List<ResultGraphPoint> socialKB,
			List<ResultGraphPoint> voiceOverlap
			) {
		super();
		this.participantInteraction = participantInteraction;
		this.participantEvolution = participantEvolution;
		this.socialKB = socialKB;
		this.voiceOverlap = voiceOverlap;
	}

	public ResultTopic getParticipantInteraction() {
		return participantInteraction;
	}

	public void setParticipantInteraction(ResultTopic participantInteraction) {
		this.participantInteraction = participantInteraction;
	}

	public String getParticipantEvolution() {
		return participantEvolution;
	}

	public void setParticipantEvolution(String participantEvolution) {
		this.participantEvolution = participantEvolution;
	}

	public List<ResultGraphPoint> getSocialKB() {
		return socialKB;
	}

	public void setSocialKB(List<ResultGraphPoint> socialKB) {
		this.socialKB = socialKB;
	}

	public List<ResultGraphPoint> getVoiceOverlap() {
		return voiceOverlap;
	}

	public void setVoiceOverlap(List<ResultGraphPoint> voiceOverlap) {
		this.voiceOverlap = voiceOverlap;
	}
}
