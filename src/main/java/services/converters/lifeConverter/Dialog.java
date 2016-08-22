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
