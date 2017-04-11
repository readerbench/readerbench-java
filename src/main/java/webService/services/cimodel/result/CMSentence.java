/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webService.services.cimodel.result;

import webService.services.lak.result.TwoModeGraph;

/**
 *
 * @author ionutparaschiv
 */
public class CMSentence {
    private String text;
    private TwoModeGraph graph;
    private int index;
    
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    public TwoModeGraph getGraph() {
        return graph;
    }

    public void setGraph(TwoModeGraph graph) {
        this.graph = graph;
    }
    
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
