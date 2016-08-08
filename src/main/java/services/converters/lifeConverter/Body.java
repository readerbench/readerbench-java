/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services.converters.lifeConverter;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author sergiu
 */
@XmlRootElement(name = "Body")
public class Body {
    List<Turn> Turns;

    public List<Turn> getTurns() {
        return Turns;
    }

    @XmlElement(name = "Turn")
    public void setTurns(List<Turn> Turns) {
        this.Turns = Turns;
    }
    
    
}
