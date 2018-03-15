/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.readerbench.readerbenchcore.data.cscl;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class DistanceStats {

    private int total;
    private int sameSpeaker;
    private int differentSpeaker;
    private int sameSpeakerFirst;
    private int differentSpeakerFirst;

    public DistanceStats(int total, int sameSpeaker, int differentSpeaker, int sameSpeakerFirst,
            int differentSpeakerFirst) {
        super();
        this.total = total;
        this.sameSpeaker = sameSpeaker;
        this.differentSpeaker = differentSpeaker;
        this.sameSpeakerFirst = sameSpeakerFirst;
        this.differentSpeaker = differentSpeaker;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
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

}
