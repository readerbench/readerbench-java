package webService.services.cscl.result;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import webService.queryResult.QueryResult;

import java.util.List;
import java.util.Map;

/**
 * Created by Dorinela on 5/28/2017.
 */
public class QueryResultParticipants extends QueryResult {

    @Path("data")
    @ElementList(inline = true, entry = "result")
    private List<Map> data;

    public List<Map> getData() {
        return data;
    }

    public void setData(List<Map> data) {
        this.data = data;
    }

    public QueryResultParticipants(List<Map> data) {
        super();
        this.data = data;
    }
}
