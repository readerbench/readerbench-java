package webService.services.cscl.result;

import com.readerbench.solr.entities.cscl.Community;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import webService.queryResult.QueryResult;

import java.util.List;
import java.util.Map;

/**
 * Created by Dorinela on 5/27/2017.
 */
public class QueryResultAllCommunities extends QueryResult {

    @Path("data")
    @ElementList(inline = true, entry = "result")
    private Map<String, List<Community>> data;

    public Map<String, List<Community>> getData() {
        return data;
    }

    public void setData(Map<String, List<Community>> data) {
        this.data = data;
    }

    public QueryResultAllCommunities(Map<String, List<Community>> data) {
        super();
        this.data = data;
    }
}
