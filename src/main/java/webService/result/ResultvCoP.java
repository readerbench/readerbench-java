package webService.result;

import java.util.HashMap;
import java.util.List;

public class ResultvCoP {

		private ResultTopic participantInteractionAllDocuments;
		private ResultTopic participantInteractionStartEndDate;
		private List<ResultTopic> participantInteractionInTime;
		
		public ResultvCoP(
				ResultTopic participantInteractionAllDocuments,
				ResultTopic participantInteractionStartEndDate,
				List<ResultTopic> participantInteractionInTime
				) {
			super();
			this.participantInteractionAllDocuments = participantInteractionAllDocuments;
			this.participantInteractionStartEndDate = participantInteractionStartEndDate;
			this.participantInteractionInTime = participantInteractionInTime;
		}

		public ResultTopic getParticipantInteractionAllDocuments() {
			return participantInteractionAllDocuments;
		}

		public void setParticipantInteractionAllDocuments(ResultTopic participantInteractionAllDocuments) {
			this.participantInteractionAllDocuments = participantInteractionAllDocuments;
		}

		public ResultTopic getParticipantInteractionStartEndDate() {
			return participantInteractionStartEndDate;
		}

		public void setParticipantInteractionStartEndDate(ResultTopic participantInteractionStartEndDate) {
			this.participantInteractionStartEndDate = participantInteractionStartEndDate;
		}

		public List<ResultTopic> getParticipantInteractionInTime() {
			return participantInteractionInTime;
		}

		public void setParticipantInteractionInTime(List<ResultTopic> participantInteractionInTime) {
			this.participantInteractionInTime = participantInteractionInTime;
		}
		
}
