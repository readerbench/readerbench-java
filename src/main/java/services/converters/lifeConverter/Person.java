/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.converters.lifeConverter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author sergiu
 */
@XmlRootElement(name = "Person")
public class Person {
    int id;

    public Person(int id) {
        this.id = id;
    }

    public Person() {
        id = -1;
    }

    public int getId() {
        return id;
    }
    
    @XmlAttribute( name = "nickname")
    public void setId(int id) {
        this.id = id;
    }
    
}
