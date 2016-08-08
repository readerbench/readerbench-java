/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.converters.lifeConverter;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 *
 * @author sergiu
 */
@XmlRootElement
public class Topic {
    String topic;

    public String getTopic() {
        return topic;
    }
    
    @XmlValue
    public void setTopic(String topic) {
        this.topic = topic;
    }
    
}
