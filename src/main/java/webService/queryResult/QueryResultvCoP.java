package webService.queryResult;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import webService.result.ResultTextCategorization;
import webService.result.ResultvCoP;

@Root(name = "response")
public class QueryResultvCoP extends QueryResult {
	
	@Path("data")
	@ElementList(inline = true, entry = "result")
	private ResultvCoP data; 

	public ResultvCoP getData() {
		return data;
	}

	public void setData(ResultvCoP data) {
		this.data = data;
	}

	public QueryResultvCoP() {
		super();
		data = new ResultvCoP(null, null, null);
	}

}