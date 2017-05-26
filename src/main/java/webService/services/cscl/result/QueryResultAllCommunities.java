package webService.services.cscl.result;

import com.readerbench.solr.entities.cscl.Community;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import webService.queryResult.QueryResult;

import java.util.List;

/**
 * Created by Dorinela on 5/27/2017.
 */
public class QueryResultAllCommunities extends QueryResult {

    @Path("data")
    @ElementList(inline = true, entry = "result")
    private List<Community> data;

    public List<Community> getData() {
        return data;
    }

    public void setData(List<Community> data) {
        this.data = data;
    }

    public QueryResultAllCommunities(List<Community> data) {
        super();
        this.data = data;
    }
}
