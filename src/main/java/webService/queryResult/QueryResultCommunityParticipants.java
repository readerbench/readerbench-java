package webService.queryResult;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;

/**
 * Created by Dorinela on 1/26/2017.
 */
public class QueryResultCommunityParticipants extends QueryResult{

    @Path("data")
    @ElementList(inline = true, entry = "result")
    private JSONArray participants;

    public JSONArray getData() {
        return participants;
    }

    public void setParticipants(JSONArray participants) {
        this.participants = participants;
    }

    public QueryResultCommunityParticipants() {
        super();
        participants = null;
    }
}
