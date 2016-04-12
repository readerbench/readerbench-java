package webService.queryResult;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import webService.result.ResultCscl;

@Root(name = "response")
public class QueryResultCscl extends QueryResult {
	
	@Path("data")
	@ElementList(inline = true, entry = "result")
	private ResultCscl data; // list of result sentiments

	public ResultCscl getData() {
		return data;
	}

	public void setData(ResultCscl data) {
		this.data = data;
	}

	public QueryResultCscl() {
		super();
		data = new ResultCscl(null, null, null, null, null, null, null);
	}

}