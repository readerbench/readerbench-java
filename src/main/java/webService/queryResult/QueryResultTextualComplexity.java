package webService.queryResult;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import webService.result.ResultTextualComplexity;

@Root(name = "response")
public class QueryResultTextualComplexity extends QueryResult {
	
	@Path("data")
	@ElementList(inline = true, entry = "result")
	private List<ResultTextualComplexity> data; // list of result sentiments

	public List<ResultTextualComplexity> getData() {
		return data;
	}

	public void setData(List<ResultTextualComplexity> data) {
		this.data = data;
	}

	public QueryResultTextualComplexity() {
		super();
		data = new ArrayList<ResultTextualComplexity>();
	}

}
