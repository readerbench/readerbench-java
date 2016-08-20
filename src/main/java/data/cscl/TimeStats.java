/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.cscl;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class TimeStats {

    private int explicitLinks;
    private int sameSpeaker;
    private int differentSpeaker;

    public TimeStats(int explicitLinks, int sameSpeaker, int differentSpeaker) {
        super();
        this.explicitLinks = explicitLinks;
        this.sameSpeaker = sameSpeaker;
        this.differentSpeaker = differentSpeaker;
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

}
