package webService.result;

import java.util.List;

public class ResultTopic {

	private List<ResultNode> nodes;
	private List<ResultEdge> links;

	public ResultTopic(List<ResultNode> nodes, List<ResultEdge> links) {
		this.nodes = nodes;
		this.links = links;
	}

}
