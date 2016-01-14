package webService.result;

public class ResultCscl {

	private String participantInteraction;
	private String participantEvolution;
	private String cumulatedContextualPmiVoice;
	private String socialKnowledgeBuilding;

	public ResultCscl(String participantInteraction) {
		super();
		this.participantInteraction = participantInteraction;
	}

	public String getContent() {
		return participantInteraction;
	}

	public void setContent(String content) {
		this.participantInteraction = content;
	}
}
