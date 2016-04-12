package webService.queryResult;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import webService.result.ResultSemanticAnnotation;

@Root(name = "response")
public class QueryResultSemanticAnnotation extends QueryResult {
	
	@Path("data")
	@ElementList(inline = true, entry = "result")
	private ResultSemanticAnnotation data; // list of result sentiments

	public ResultSemanticAnnotation getData() {
		return data;
	}

	public void setData(ResultSemanticAnnotation data) {
		this.data = data;
	}

	public QueryResultSemanticAnnotation() {
		super();
		data = new ResultSemanticAnnotation(null, 0, 0, 0, null, null);
	}

}