/* 
 * Copyright 2016 ReaderBench.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package webService.result;

import java.util.HashMap;
import java.util.List;

public class ResultvCoP {

    private ResultTopic participantInteractionAllDocuments;
    private ResultTopic participantInteractionStartEndDate;
    private List<ResultTopic> participantInteractionInTimeList;
    //ResultTopic participantInteractionInTime;
    private List<ResultGraphPoint> participantEvolution;

    public ResultvCoP(
            ResultTopic participantInteractionAllDocuments,
            ResultTopic participantInteractionStartEndDate,
            List<ResultTopic> participantInteractionInTimeList,
            List<ResultGraphPoint> participantEvolution
    ) {
        super();
        this.participantInteractionAllDocuments = participantInteractionAllDocuments;
        this.participantInteractionStartEndDate = participantInteractionStartEndDate;
        this.participantInteractionInTimeList = participantInteractionInTimeList;
        this.participantEvolution = participantEvolution;
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
    
    public List<ResultGraphPoint> getParticipantEvolutionData() {
        return participantEvolution;
    }

    public void setParticipantEvolutionData(List<ResultGraphPoint> participantEvolution) {
        this.participantEvolution = participantEvolution;
    }

    /*	public ResultTopic getParticipantInteractionInTime() {
			return participantInteractionInTime;
		}

		public void setParticipantInteractionInTime(ResultTopic participantInteractionInTime) {
			this.participantInteractionInTime = participantInteractionInTime;
		}		*/
}
