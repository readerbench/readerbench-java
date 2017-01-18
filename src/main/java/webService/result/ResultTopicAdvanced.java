/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webService.result;

import java.util.List;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class ResultTopicAdvanced {

    private List<ResultNodeAdvanced> nodes;
    private List<ResultEdge> links;

    public ResultTopicAdvanced(List<ResultNodeAdvanced> nodes, List<ResultEdge> links) {
        this.nodes = nodes;
        this.links = links;
    }

    public List<ResultNodeAdvanced> getNodes() {
        return nodes;
    }

    public void setNodes(List<ResultNodeAdvanced> nodes) {
        this.nodes = nodes;
    }

    public List<ResultEdge> getLinks() {
        return links;
    }

    public void setLinks(List<ResultEdge> links) {
        this.links = links;
    }

}
