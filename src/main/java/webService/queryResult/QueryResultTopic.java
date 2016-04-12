package webService.queryResult;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import webService.result.ResultTopic;

@Root(name = "response")
public class QueryResultTopic extends QueryResult {
	
	@Path("data")
	@ElementList(inline = true, entry = "result")
	private ResultTopic data; // list of result sentiments

	public ResultTopic getData() {
		return data;
	}

	public void setData(ResultTopic data) {
		this.data = data;
	}

	public QueryResultTopic() {
		super();
		data = new ResultTopic(null, null);
	}

}
