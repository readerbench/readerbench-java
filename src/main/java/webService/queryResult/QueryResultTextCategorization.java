package webService.queryResult;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import webService.result.ResultTextCategorization;

@Root(name = "response")
public class QueryResultTextCategorization extends QueryResult {
	
	@Path("data")
	@ElementList(inline = true, entry = "result")
	private ResultTextCategorization data; // list of result sentiments

	public ResultTextCategorization getData() {
		return data;
	}

	public void setData(ResultTextCategorization data) {
		this.data = data;
	}

	public QueryResultTextCategorization() {
		super();
		data = new ResultTextCategorization(null, null);
	}

}