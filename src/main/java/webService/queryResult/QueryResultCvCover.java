package webService.queryResult;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import webService.result.ResultCvCover;

@Root(name = "response")
public class QueryResultCvCover extends QueryResult {
	
	@Path("data")
	@ElementList(inline = true, entry = "result")
	private ResultCvCover data; // list of result sentiments

	public ResultCvCover getData() {
		return data;
	}

	public void setData(ResultCvCover data) {
		this.data = data;
	}

	public QueryResultCvCover() {
		super();
		data = new ResultCvCover(null, null);
	}

}