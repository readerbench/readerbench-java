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
   public void setMesg(String m) {
        this.mesg = m;
    }

    @XmlAttribute (name = "genid")
    public void setGenid(int id) {
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
