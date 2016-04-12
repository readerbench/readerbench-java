package webService.queryResult;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import webService.result.ResultSearch;

@Root(name = "response")
public class QueryResultSearch extends QueryResult {

	@Path("data")
	@ElementList(inline = true, entry = "result")
	public List<ResultSearch> data; // list of query results (urls)

	public List<ResultSearch> getData() {
		return data;
	}

	public void setData(List<ResultSearch> data) {
		this.data = data;
	}

	public QueryResultSearch() {
		super();
		data = new ArrayList<ResultSearch>();
	}
}
