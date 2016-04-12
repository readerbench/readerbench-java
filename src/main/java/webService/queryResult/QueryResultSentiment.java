package webService.queryResult;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import webService.result.ResultSentiment;

@Root(name = "response")
public class QueryResultSentiment extends QueryResult {
	
	@Path("data")
	@ElementList(inline = true, entry = "result")
	private List<ResultSentiment> data; // list of result sentiments

	public List<ResultSentiment> getData() {
		return data;
	}

	public void setData(List<ResultSentiment> data) {
		this.data = data;
	}

	public QueryResultSentiment() {
		super();
		data = new ArrayList<ResultSentiment>();
	}

}
