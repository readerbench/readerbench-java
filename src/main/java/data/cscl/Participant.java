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
package data.cscl;

import data.AbstractDocument;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Participant implements Comparable<Participant>, Serializable {

    private static final long serialVersionUID = -4515721505776009876L;

    private String name;
    private AbstractDocument contributions;
    private AbstractDocument significantContributions;
    private double gradeAnnotator;
    private double textualComplexityLevel;
    private EnumMap<CSCLIndices, Double> indices;
    private Map<Entry<CSCLIndices, CSCLCriteria>, Double> longitudinalIndices;
    
    // added by valentin.sergiu.cioaca@gmail.com
    private double rhythmicIndexSM;         // Solomon Marcus study
    private double freqMaxRhythmIndex;
    private double rhythmicCoefficient;
    private double chatEntropyForRegularity;
    
    private ParticipantGroup participantGroup;

    public Participant(String name, AbstractDocument d) {
        super();
        this.name = name;
        this.contributions = new Conversation(null, d.getSemanticModels(), d.getLanguage());
        this.significantContributions = new Conversation(null, d.getSemanticModels(), d.getLanguage());
        this.indices = new EnumMap<>(CSCLIndices.class);
        this.longitudinalIndices = new HashMap<>();
        this.resetIndices();
    }

/*----------------------------------------------------------------------------*/    
    public void setRhythmicIndex(double rhythmicIndex) {
        this.rhythmicIndexSM = rhythmicIndex;
    }
    
    public void setRhythmicCoefficient(double rhythmicCoefficient) {
        this.rhythmicCoefficient = rhythmicCoefficient;
    }

    public void setChatEntropy(double chatEntropy) {
        this.chatEntropyForRegularity = chatEntropy;
    }
    
    public void setFreqMaxRhythmIndex(double freqMaxId) {
        this.freqMaxRhythmIndex = freqMaxId;
    }
    
    public double getRhythmicIndex() {
        return this.rhythmicIndexSM;
    }
    
    public double getRhythmicCoefficient() {
        return this.rhythmicCoefficient;
    }
    
    public double getChatEntropy() {
        return this.chatEntropyForRegularity;
    }
    
    public double getFreqMaxRhythmIndex() {
        return this.freqMaxRhythmIndex;
    }
/*----------------------------------------------------------------------------*/

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AbstractDocument getContributions() {
        return contributions;
    }

    public void setContributions(AbstractDocument interventions) {
        this.contributions = interventions;
    }

    public AbstractDocument getSignificantContributions() {
        return significantContributions;
    }

    public void setSignificantContributions(AbstractDocument significantInterventions) {
        this.significantContributions = significantInterventions;
    }

    public double getGradeAnnotator() {
        return gradeAnnotator;
    }

    public void setGradeAnnotator(double gradeAnnotator) {
        this.gradeAnnotator = gradeAnnotator;
    }

    public double getTextualComplexityLevel() {
        return textualComplexityLevel;
    }

    public void setTextualComplexityLevel(double textualComplexityLevel) {
        this.textualComplexityLevel = textualComplexityLevel;
    }

    public EnumMap<CSCLIndices, Double> getIndices() {
        return indices;
    }

    public void setIndices(EnumMap<CSCLIndices, Double> indices) {
        this.indices = indices;
    }

    public Map<Entry<CSCLIndices, CSCLCriteria>, Double> getLongitudinalIndices() {
        return longitudinalIndices;
    }

    public void setLongitudinalIndices(Map<Entry<CSCLIndices, CSCLCriteria>, Double> longitudinalIndices) {
        this.longitudinalIndices = longitudinalIndices;
    }

    private void resetIndices() {
        for (CSCLIndices index : CSCLIndices.values()) {
            indices.put(index, 0d);
        }
    }

    public ParticipantGroup getParticipantGroup() {
        return participantGroup;
    }

    public void setParticipantGroup(ParticipantGroup participantGroup) {
        this.participantGroup = participantGroup;
    }

    @Override
    public String toString() {
        return name + ": { " + indices.get(CSCLIndices.INDEGREE) + ", " + indices.get(CSCLIndices.OUTDEGREE) + ", "
                + indices.get(CSCLIndices.ECCENTRICITY) + ", " + participantGroup + "}\n";
    }

    @Override
    public boolean equals(Object obj) {
        Participant p = (Participant) obj;
        return this.getName().equals(p.getName());
    }

    @Override
    public int compareTo(Participant o) {
        return this.getName().compareTo(o.getName());
    }
}
