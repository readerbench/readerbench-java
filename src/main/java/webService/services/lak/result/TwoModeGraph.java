package webService.services.lak.result;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ionutparaschiv
 */
public class TwoModeGraph {
    public List<TwoModeGraphEdge> edgeList;
    public List<TwoModeGraphNode> nodeList; 
    
    public TwoModeGraph() {
        this.edgeList = new ArrayList<>();
        this.nodeList = new ArrayList<>();
    }
    
    public void addEdge(TwoModeGraphEdge edge) {
        this.edgeList.add(edge);
    }
    
    public void addNode(TwoModeGraphNode node) {
        this.nodeList.add(node);
    }
}