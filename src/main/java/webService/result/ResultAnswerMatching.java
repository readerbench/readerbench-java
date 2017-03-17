/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webService.result;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class ResultAnswerMatching {
    
    private final Integer answerId;
    private final Double similarityScore;
    
    public ResultAnswerMatching(Integer answerId, Double similarityScore) {
        this.answerId = answerId;
        this.similarityScore = similarityScore;
    }
    
}
