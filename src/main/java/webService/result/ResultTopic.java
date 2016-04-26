package webService.result;

import java.util.List;

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
