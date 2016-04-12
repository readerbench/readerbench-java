package webService.queryResult;

import java.io.StringWriter;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Root(name = "response")
public class QueryResult {

	@Element
	private boolean success;

	@Element(name = "errormsg")
	private String errorMsg; // custom error message (optional)

	protected QueryResult() {
		success = true;
		errorMsg = "";
	}
	
	public String convertToJson() {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
		String json = gson.toJson(this);
		return json;
	}
	
	private String convertToXml() {
		Serializer serializer = new Persister();
		StringWriter result = new StringWriter();
		try {
			serializer.write(this, result);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return result.toString();
	}
	
}
