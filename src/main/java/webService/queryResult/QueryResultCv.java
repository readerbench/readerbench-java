package webService.queryResult;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import webService.result.ResultCv;
import webService.result.ResultCvCover;

@Root(name = "response")
public class QueryResultCv extends QueryResult {
	
	@Path("data")
	@ElementList(inline = true, entry = "result")
	private ResultCv data; // list of result sentiments

	public ResultCv getData() {
		return data;
	}

	public void setData(ResultCv data) {
		this.data = data;
	}

	public QueryResultCv() {
		super();
		data = new ResultCv();
	}

}