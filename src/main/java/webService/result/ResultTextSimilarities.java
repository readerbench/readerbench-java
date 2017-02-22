/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webService.result;

import java.util.Map;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class ResultTextSimilarities {
    
    private final Map<String, Double> similarityScores;
    
    public ResultTextSimilarities(Map<String, Double> similarityScores) {
        this.similarityScores = similarityScores;
    }
    
}
