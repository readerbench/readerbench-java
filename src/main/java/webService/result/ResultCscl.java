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

public class ResultCscl {

    private ResultTopic concepts;
    private ResultTopic participantInteraction;
    private List<ResultGraphPoint> participantEvolution;
    private List<ResultGraphPoint> socialKB;
    private List<ResultGraphPoint> voiceOverlap;
    private HashMap<String, HashMap<String, Double>> csclIndices;
    private HashMap<String, String> csclIndicesDescription;

    public ResultCscl(
            ResultTopic concepts,
            ResultTopic participantInteraction,
            List<ResultGraphPoint> participantEvolution,
            List<ResultGraphPoint> socialKB,
            List<ResultGraphPoint> voiceOverlap,
            HashMap<String, HashMap<String, Double>> csclIndices,
            HashMap<String, String> csclIndicesDescription
    ) {
        super();
        this.concepts = concepts;
        this.participantInteraction = participantInteraction;
        this.participantEvolution = participantEvolution;
        this.socialKB = socialKB;
        this.voiceOverlap = voiceOverlap;
        this.csclIndices = csclIndices;
        this.csclIndicesDescription = csclIndicesDescription;
    }

    public HashMap<String, String> getCsclIndicesDescription() {
        return csclIndicesDescription;
    }

    public void setCsclIndicesDescription(HashMap<String, String> csclIndicesDescription) {
        this.csclIndicesDescription = csclIndicesDescription;
    }

    public HashMap<String, HashMap<String, Double>> getCsclIndices() {
        return csclIndices;
    }

    public void setCsclIndices(HashMap<String, HashMap<String, Double>> csclIndices) {
        this.csclIndices = csclIndices;
    }

    public ResultTopic getConcepts() {
        return concepts;
    }

    public void setConcepts(ResultTopic concepts) {
        this.concepts = concepts;
    }

    public ResultTopic getParticipantInteraction() {
        return participantInteraction;
    }

    public void setParticipantInteraction(ResultTopic participantInteraction) {
        this.participantInteraction = participantInteraction;
    }

    public List<ResultGraphPoint> getParticipantEvolution() {
        return participantEvolution;
    }

    public void setParticipantEvolution(List<ResultGraphPoint> participantEvolution) {
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
