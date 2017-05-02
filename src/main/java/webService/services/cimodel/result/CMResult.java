/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webService.services.cimodel.result;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ionutparaschiv
 */
public class CMResult {
    public List<CMSentence> sentenceList;
    public List<CMWordResult> wordList;
    
    public CMResult() {
        this.sentenceList = new ArrayList<>();
        this.wordList = new ArrayList<>();
    }
}
