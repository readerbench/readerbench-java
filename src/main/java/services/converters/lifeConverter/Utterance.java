/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.converters.lifeConverter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 *
 * @author sergiu
 */
@XmlRootElement(name = "Utterance")
public class Utterance {
    int genid;
    int ref;
    String time;
    String mesg;

    String getMesg() {
        return mesg;
    }
    
    int getGenid() {
        return genid;
    }
   
    public int getRef() {
        return ref;
    }

    public String getTime() {
        return time;
    }
    
    @XmlValue
    void setMesg(String m) {
        this.mesg = m;
    }

    @XmlAttribute (name = "genid")
    void setGenid(int id) {
        this.genid = id;
    }
    
    @XmlAttribute (name = "ref")
    public void setRef(int ref) {
        this.ref = ref;
    }

    @XmlAttribute (name = "time")
    public void setTime(String time) {
        this.time = time;
    }

    public Utterance(int genid, int ref, String time, String mesg) {
        this.genid = genid;
        this.ref = ref;
        this.time = time;
        this.mesg = mesg;
    }

    public Utterance() {
    }

    
    
}
