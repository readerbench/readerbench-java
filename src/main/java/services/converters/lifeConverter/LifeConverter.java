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

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author sergiu
 */
public class LifeConverter {
    public static void main(String[] args) {

        try
        {
            File file = new File("2016_Bucarest.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(Dialog.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            Dialog d = (Dialog) jaxbUnmarshaller.unmarshal(file);
            int sz_b = d.getBody().size();
            int count_fl = 0;
            int count_id = 2;
            
            Dialog temp = new Dialog(new LinkedList<Person>(), new ArrayList<String>(), new ArrayList<Turn>(), d.id);
            
            for(int i = 0; i < sz_b; i++) {
                if(d.getBody().get(i).getId() == "4134") {
                    
                    if(d.getBody().get(i).getUtter().getMesg().contains("with respect to")) {
                        jaxbMarshaller.marshal(temp, new File("Question" + count_fl + ".xml"));
                        count_fl++;
                        temp = new Dialog(new LinkedList<Person>(), new ArrayList<String>(), new ArrayList<Turn>(), d.getId());
                        temp.getBody().add(new Turn("4134", new Utterance(1, 0, "", d.getBody().get(i - 1).getUtter().getMesg())));
                        temp.getBody().get(0).getUtter().setMesg(temp.getBody().get(0).getUtter().getMesg().concat(" " + d.getBody().get(i).getUtter().getMesg()));
                    }
                    else {
                        
                        if( i < sz_b && d.getBody().get(i + 1).getId() == "4134") {
                           continue;
                        }
                        else {
                            jaxbMarshaller.marshal(temp, new File("Question" + count_fl + ".xml"));
                            count_fl++;
                            temp = new Dialog(new LinkedList<Person>(), new ArrayList<String>(), new ArrayList<Turn>(), d.id);
                            temp.getBody().add(new Turn("4134", new Utterance(1, 0, "", d.getBody().get(i).getUtter().getMesg())));
                       }
                    }
                    count_id = 2;
                }
                else {
                    temp.getBody().add(new Turn(d.getBody().get(i).getId(), new Utterance(count_id, 1, "", d.getBody().get(i).getUtter().getMesg())));
                    count_id++;

                }
                temp.getParticipants().add(new Person(d.getBody().get(i).id));
                
            }
        }
        catch(JAXBException e) {
            e.printStackTrace();
        }

    }
}
