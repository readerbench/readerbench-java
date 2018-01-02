/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.data.cscl;

import java.util.Map;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class ChatStats {

    private int contributions;
    private int participants;
    private int maxRefDistance;
    private int duration; // timestamp
    private int explicitLinks;
    private int sameSpeakerFirst;
    private int differentSpeakerFirst;
    private int sameBlock;
    private int differentBlock;
    private double coverage;
    private Map<Integer, Integer> references; // number of references to
    // distance 1, 2, ... 5

    public ChatStats(int contributions, int participants, int duration, int explicitLinks, double coverage,
            int sameSpeakerFirst, int differentSpeakerFirst, int sameBlock, int differentBlock,
            Map<Integer, Integer> references) {
        super();
        this.contributions = contributions;
        this.participants = participants;
        this.duration = duration;
        this.explicitLinks = explicitLinks;
        this.coverage = coverage;
        this.sameSpeakerFirst = sameSpeakerFirst;
        this.differentSpeakerFirst = differentSpeakerFirst;
        this.sameBlock = sameBlock;
        this.differentBlock = sameBlock;
        this.references = references;
    }

    public int getSameSpeakerFirst() {
        return sameSpeakerFirst;
    }

    public void setSameSpeakerFirst(int sameSpeakerFirst) {
        this.sameSpeakerFirst = sameSpeakerFirst;
    }

    public int getDifferentSpeakerFirst() {
        return differentSpeakerFirst;
    }

    public void setDifferentSpeakerFirst(int differentSpeakerFirst) {
        this.differentSpeakerFirst = differentSpeakerFirst;
    }

    public int getDifferentBlock() {
        return differentBlock;
    }

    public void setDifferentBlock(int differentBlock) {
        this.differentBlock = differentBlock;
    }

    public int getContributions() {
        return contributions;
    }

    public void setContributions(int contributions) {
        this.contributions = contributions;
    }

    public int getParticipants() {
        return participants;
    }

    public void setParticipants(int participants) {
        this.participants = participants;
    }

    public int getMaxRefDistance() {
        return maxRefDistance;
    }

    public void setMaxRefDistance(int maxRefDistance) {
        this.maxRefDistance = maxRefDistance;
    }
    
    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getExplicitLinks() {
        return explicitLinks;
    }

    public void setExplicitLinks(int explicitLinks) {
        this.explicitLinks = explicitLinks;
    }

    public int getSameBlock() {
        return sameBlock;
    }

    public void setSameBlock(int sameBlock) {
        this.sameBlock = sameBlock;
    }

    public double getCoverage() {
        return coverage;
    }

    public void setCoverage(double coverage) {
        this.coverage = coverage;
    }

    public Map<Integer, Integer> getReferences() {
        return references;
    }

    public void setReferences(Map<Integer, Integer> references) {
        this.references = references;
    }

}
