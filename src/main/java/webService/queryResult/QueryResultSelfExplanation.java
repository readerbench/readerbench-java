package webService.queryResult;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import webService.result.ResultSelfExplanation;

@Root(name = "response")
public class QueryResultSelfExplanation extends QueryResult {
	
	@Path("data")
	@ElementList(inline = true, entry = "result")
	private ResultSelfExplanation data; // list of result sentiments

	public ResultSelfExplanation getData() {
		return data;
	}

	public void setData(ResultSelfExplanation data) {
		this.data = data;
	}

	public QueryResultSelfExplanation() {
		super();
		data = new ResultSelfExplanation(null, null);
	}

}