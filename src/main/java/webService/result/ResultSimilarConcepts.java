/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webService.result;

import data.Word;
import java.util.TreeMap;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class ResultSimilarConcepts {
    
    private TreeMap<Word, Double> concepts;
    
    public ResultSimilarConcepts(TreeMap<Word, Double>  concepts) {
        this.concepts = concepts;
    }
    
}
