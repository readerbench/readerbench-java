/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.converters.lifeConverter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author sergiu
 */
@XmlRootElement(name = "Turn")
public class Turn {
    int id;
    Utterance utter;

    public int getId() {
        return id;
    }

    public Utterance getUtter() {
        return utter;
    }

    @XmlAttribute (name = "nickname")
    public void setId(int id) {
        this.id = id;
    }
    
    @XmlElement (name = "Utterance")
    public void setUtter(Utterance utter) {
        this.utter = utter;
    }

    public Turn(int id, Utterance utter) {
        this.id = id;
        this.utter = utter;
    }

    public Turn() {
        id = -1;
        utter = null;
    }
    
}
