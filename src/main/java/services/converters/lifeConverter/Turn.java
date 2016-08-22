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
