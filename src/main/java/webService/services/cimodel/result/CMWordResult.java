/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webService.services.cimodel.result;

import java.util.ArrayList;
import java.util.List;
import services.comprehensionModel.utils.indexer.graphStruct.CMNodeType;

/**
 *
 * @author ionutparaschiv
 */
public class CMWordResult {
    public String value;
    public CMNodeType type;
    public List<CMWordActivationResult> activationList;
    
    public CMWordResult() {
        this.activationList = new ArrayList<>();
    }
}