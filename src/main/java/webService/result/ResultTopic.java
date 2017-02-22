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
public class ResultTopic {

    private List<ResultNode> nodes;
    private List<ResultEdge> links;

    public ResultTopic(List<ResultNode> nodes, List<ResultEdge> links) {
        this.nodes = nodes;
        this.links = links;
    }

    public List<ResultNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<ResultNode> nodes) {
        this.nodes = nodes;
    }

    public List<ResultEdge> getLinks() {
        return links;
    }

    public void setLinks(List<ResultEdge> links) {
        this.links = links;
    }

}
