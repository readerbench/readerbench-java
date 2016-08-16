/* 
 * Copyright 2016 ReaderBench.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
