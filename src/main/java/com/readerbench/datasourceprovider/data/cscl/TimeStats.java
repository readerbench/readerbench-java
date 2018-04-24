/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.datasourceprovider.data.cscl;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class TimeStats {

    private int explicitLinks;
    private int sameSpeaker;
    private int differentSpeaker;
    private int explicitLinksPercentage;
    private int sameSpeakerPercentage;
    private int differentSpeakerPercentage;

    public TimeStats(int explicitLinks, int sameSpeaker, int differentSpeaker,
            int explicitLinksPercentage, int sameSpeakerPercentage, 
            int differentSpeakerPercentage) {
        super();
        this.explicitLinks = explicitLinks;
        this.sameSpeaker = sameSpeaker;
        this.differentSpeaker = differentSpeaker;
        this.explicitLinksPercentage = explicitLinksPercentage;
        this.sameSpeakerPercentage = sameSpeakerPercentage;
        this.differentSpeakerPercentage = differentSpeakerPercentage;
    }
    
    public TimeStats() {
        this(0, 0, 0, 0, 0, 0);
    }

    public int getExplicitLinks() {
        return explicitLinks;
    }

    public void setExplicitLinks(int explicitLinks) {
        this.explicitLinks = explicitLinks;
    }

    public int getSameSpeaker() {
        return sameSpeaker;
    }

    public void setSameSpeaker(int sameSpeaker) {
        this.sameSpeaker = sameSpeaker;
    }

    public int getDifferentSpeaker() {
        return differentSpeaker;
    }

    public void setDifferentSpeaker(int differentSpeaker) {
        this.differentSpeaker = differentSpeaker;
    }

    public int getExplicitLinksPercentage() {
        return explicitLinksPercentage;
    }

    public void setExplicitLinksPercentage(int explicitLinksPercentage) {
        this.explicitLinksPercentage = explicitLinksPercentage;
    }

    public int getSameSpeakerPercentage() {
        return sameSpeakerPercentage;
    }

    public void setSameSpeakerPercentage(int sameSpeakerPercentage) {
        this.sameSpeakerPercentage = sameSpeakerPercentage;
    }

    public int getDifferentSpeakerPercentage() {
        return differentSpeakerPercentage;
    }

    public void setDifferentSpeakerPercentage(int differentSpeakerPercentage) {
        this.differentSpeakerPercentage = differentSpeakerPercentage;
    }
    
}
