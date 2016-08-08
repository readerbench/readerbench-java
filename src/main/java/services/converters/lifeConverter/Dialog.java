/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.converters.lifeConverter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author sergiu
 */
@XmlRootElement(name = "Dialog")
public class Dialog {
    List<Person> participants;
    List<String> topics;
    int id;
    List<Turn> body;

    public Dialog(List<Person> participants, List<String> topics, List<Turn> body, int id) {
        this.participants = participants;
        this.topics = topics;
        this.id = id;
        this.body = body;
    }

    public Dialog() {
        participants = new LinkedList<Person>();
        topics = new ArrayList<String>();
        id = -1;
        
    }
    
    

    public List<Person> getParticipants() {
        return participants;
    }

    public List<String> getTopics() {
        return topics;
    }

    public List<Turn> getBody() {
        return body;
    }

    public int getId() {
        return id;
    }
    
    @XmlElementWrapper(name = "Participants")
    @XmlElement(name = "Person")
    public void setParticipants(List<Person> participants) {
        this.participants = participants;
    }

    @XmlElementWrapper(name = "Topics")
    @XmlElement(name = "Topic")
    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    @XmlElementWrapper(name = "Body")
    @XmlElement(name = "Turn")
    public void setBody(List<Turn> body) {
        this.body = body;
    }

    @XmlAttribute(name = "team")
    public void setId(int id) {
        this.id = id;
    }
    
}
