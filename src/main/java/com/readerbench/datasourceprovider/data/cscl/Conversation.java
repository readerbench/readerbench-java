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
package com.readerbench.datasourceprovider.data.cscl;

import com.readerbench.coreservices.commons.VectorAlgebra;
import com.readerbench.coreservices.dialogism.DialogismComputations;
import com.readerbench.datasourceprovider.data.AbstractDocument;
import com.readerbench.datasourceprovider.data.discourse.SemanticChain;
import com.readerbench.datasourceprovider.data.semanticmodels.ISemanticModel;
import com.readerbench.datasourceprovider.pojo.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Mihai Dascalu
 *
 */
public class Conversation extends AbstractDocument {

    private static final Logger LOGGER = LoggerFactory.getLogger(Conversation.class);

    private static final long serialVersionUID = 2096182930189552475L;

    private List<Participant> participants;
    private double[][] participantContributions;
    private List<CollaborationZone> intenseCollabZonesSocialKB;
    private List<CollaborationZone> intenseCollabZonesVoice;
    private List<CollaborationZone> intenseConvergentZonesVoice;
    private List<CollaborationZone> intenseDivergentZonesVoice;
    private List<CollaborationZone> annotatedCollabZones;
    private double quantCollabPercentage;
    private double socialKBPercentage;
    private double socialKBvsScore;
    // determine the distribution throughout the conversation of social KB
    private double[] socialKBEvolution;
    // determine the distribution throughout the conversation of voice PMI
    private double[] voicePMIEvolution;
    private double[] voiceExtendedEvolution;
    // determine the distribution of collaboration from annotations throughout the conversation
    private double[] annotatedCollabEvolution;

    /**
     * @param path
     * @param models
     * @param lang
     */
    public Conversation(String path, List<ISemanticModel> models, Lang lang) {
        super(path, models, lang);
        participants = new ArrayList<>();
        intenseCollabZonesSocialKB = new ArrayList<>();
        intenseCollabZonesVoice = new ArrayList<>();
        annotatedCollabZones = new ArrayList<>();
    }

    /**
     * @param voice
     * @param p
     * @return
     */
    public double[] getParticipantBlockDistribution(SemanticChain voice, Participant p) {
        double[] distribution = new double[voice.getBlockDistribution().length];
        for (int i = 0; i < getBlocks().size(); i++) {
            if (getBlocks().get(i) != null && ((Utterance) getBlocks().get(i)).getParticipant().equals(p)) {
                distribution[i] = voice.getBlockDistribution()[i];
            }
        }
        return distribution;
    }

    /**
     * @param voice
     * @param p
     * @return
     */
    public double[] getExtendedParticipantBlockDistribution(SemanticChain voice, Participant p) {
        double[] distribution = new double[voice.getExtendedBlockDistribution().length];
        for (int i = 0; i < getBlocks().size(); i++) {
            if (getBlocks().get(i) != null && ((Utterance) getBlocks().get(i)).getParticipant().equals(p)) {
                distribution[i] = voice.getExtendedBlockDistribution()[i];
            }
        }
        return distribution;
    }

    /**
     * @param voice
     * @param p
     * @return
     */
    public double[] getParticipantBlockMovingAverage(SemanticChain voice, Participant p) {
        double[] distribution = getParticipantBlockDistribution(voice, p);
        return VectorAlgebra.movingAverage(distribution, DialogismComputations.WINDOW_SIZE, getBlockOccurrencePattern(), DialogismComputations.MAXIMUM_INTERVAL);
    }

    /**
     * @param voice
     * @param p
     * @return
     */
    public double[] getExtendedParticipantBlockMovingAverage(SemanticChain voice, Participant p) {
        double[] distribution = getExtendedParticipantBlockDistribution(voice, p);
        return VectorAlgebra.movingAverage(distribution, DialogismComputations.WINDOW_SIZE, getBlockOccurrencePattern(), DialogismComputations.MAXIMUM_INTERVAL);
    }

    /**
     * @return
     */
    public List<Participant> getParticipants() {
        return participants;
    }

    /**
     * @param participants
     */
    public void setParticipants(ArrayList<Participant> participants) {
        this.participants = participants;
    }

    /**
     * @return
     */
    public double[][] getParticipantContributions() {
        return participantContributions;
    }

    /**
     * @param participantContributions
     */
    public void setParticipantContributions(double[][] participantContributions) {
        this.participantContributions = participantContributions;
    }

    /**
     * @return
     */
    public List<CollaborationZone> getIntenseCollabZonesSocialKB() {
        return intenseCollabZonesSocialKB;
    }

    /**
     * @param intenseCollabZonesSocialKB
     */
    public void setIntenseCollabZonesSocialKB(List<CollaborationZone> intenseCollabZonesSocialKB) {
        this.intenseCollabZonesSocialKB = intenseCollabZonesSocialKB;
    }

    /**
     * @return
     */
    public List<CollaborationZone> getIntenseCollabZonesVoice() {
        return intenseCollabZonesVoice;
    }

    /**
     * @param intenseCollabZonesVoice
     */
    public void setIntenseCollabZonesVoice(List<CollaborationZone> intenseCollabZonesVoice) {
        this.intenseCollabZonesVoice = intenseCollabZonesVoice;
    }

    public List<CollaborationZone> getIntenseConvergentZonesVoice() {
        return intenseConvergentZonesVoice;
    }

    public void setIntenseConvergentZonesVoice(List<CollaborationZone> intenseConvergentZonesVoice) {
        this.intenseConvergentZonesVoice = intenseConvergentZonesVoice;
    }

    public List<CollaborationZone> getIntenseDivergentZonesVoice() {
        return intenseDivergentZonesVoice;
    }

    public void setIntenseDivergentZonesVoice(List<CollaborationZone> intenseDivergentZonesVoice) {
        this.intenseDivergentZonesVoice = intenseDivergentZonesVoice;
    }

    /**
     * @return
     */
    public List<CollaborationZone> getAnnotatedCollabZones() {
        return annotatedCollabZones;
    }

    /**
     * @param annotatedCollabZones
     */
    public void setAnnotatedCollabZones(List<CollaborationZone> annotatedCollabZones) {
        this.annotatedCollabZones = annotatedCollabZones;
    }

    /**
     * @return
     */
    public double getQuantCollabPercentage() {
        return quantCollabPercentage;
    }

    /**
     * @param quantCollabPercentage
     */
    public void setQuantCollabPercentage(double quantCollabPercentage) {
        this.quantCollabPercentage = quantCollabPercentage;
    }

    /**
     * @return
     */
    public double getSocialKBPercentage() {
        return socialKBPercentage;
    }

    /**
     * @param socialKBPercentage
     */
    public void setSocialKBPercentage(double socialKBPercentage) {
        this.socialKBPercentage = socialKBPercentage;
    }

    /**
     * @return
     */
    public double getSocialKBvsScore() {
        return socialKBvsScore;
    }

    /**
     * @param socialKBvsScore
     */
    public void setSocialKBvsScore(double socialKBvsScore) {
        this.socialKBvsScore = socialKBvsScore;
    }

    /**
     * @return
     */
    public double[] getSocialKBEvolution() {
        return socialKBEvolution;
    }

    /**
     * @param socialKBEvolution
     */
    public void setSocialKBEvolution(double[] socialKBEvolution) {
        this.socialKBEvolution = socialKBEvolution;
    }

    /**
     * @return
     */
    public double[] getVoicePMIEvolution() {
        return voicePMIEvolution;
    }

    /**
     * @param voicePMIEvolution
     */
    public void setVoicePMIEvolution(double[] voicePMIEvolution) {
        this.voicePMIEvolution = voicePMIEvolution;
    }

    public double[] getVoiceExtendedEvolution() {
        return voiceExtendedEvolution;
    }

    public void setVoiceExtendedEvolution(double[] voiceExtendedEvolution) {
        this.voiceExtendedEvolution = voiceExtendedEvolution;
    }

    /**
     * @return
     */
    public double[] getAnnotatedCollabEvolution() {
        return annotatedCollabEvolution;
    }

    /**
     * @param annotatedCollabEvolution
     */
    public void setAnnotatedCollabEvolution(double[] annotatedCollabEvolution) {
        this.annotatedCollabEvolution = annotatedCollabEvolution;
    }
}
