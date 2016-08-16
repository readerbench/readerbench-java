package webService.queryResult;

import org.json.simple.JSONObject;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;

public class QueryResultMailgun extends QueryResult {
	
	@Path("data")
	@ElementList(inline = true, entry = "result")
	private JSONObject mailgun; 

	public JSONObject getData() {
		return mailgun;
	}

	public void setMailgunResponse(JSONObject mailgun) {
		this.mailgun = mailgun;
	}

	public QueryResultMailgun() {
		super();
		mailgun = null;
	}

}