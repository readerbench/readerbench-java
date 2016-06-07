package webService.result;

import java.util.HashMap;
import java.util.List;

public class ResultvCoP {

		private ResultTopic participantInteractionAllDocuments;
		private ResultTopic participantInteractionStartEndDate;
		private List<ResultTopic> participantInteractionInTimeList;
		//ResultTopic participantInteractionInTime;
		
		public ResultvCoP(
				ResultTopic participantInteractionAllDocuments,
				ResultTopic participantInteractionStartEndDate,
				List<ResultTopic> participantInteractionInTimeList
				) {
			super();
			this.participantInteractionAllDocuments = participantInteractionAllDocuments;
			this.participantInteractionStartEndDate = participantInteractionStartEndDate;
			this.participantInteractionInTimeList = participantInteractionInTimeList;
		}
		
	//	public ResultvCoP(ResultTopic participantInteractionInTime) {
	//		super();
	//		this.participantInteractionInTime = participantInteractionInTime;
	//	}

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

		public List<ResultTopic> getParticipantInteractionInTimeList() {
			return participantInteractionInTimeList;
		}

		public void setParticipantInteractionInTimeList(List<ResultTopic> participantInteractionInTimeList) {
			this.participantInteractionInTimeList = participantInteractionInTimeList;
		}

	/*	public ResultTopic getParticipantInteractionInTime() {
			return participantInteractionInTime;
		}

		public void setParticipantInteractionInTime(ResultTopic participantInteractionInTime) {
			this.participantInteractionInTime = participantInteractionInTime;
		}		*/
}
