package webService.services.cscl.result;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import webService.queryResult.QueryResult;
import webService.services.cscl.result.dto.Category;

import java.util.List;

/**
 * Created by Dorinela on 5/27/2017.
 */
public class QueryResultAllCommunities extends QueryResult {

    @Path("data")
    @ElementList(inline = true, entry = "result")
    private List<Category> data;

    public List<Category> getData() {
        return data;
    }

    public void setData(List<Category> data) {
        this.data = data;
    }

    public QueryResultAllCommunities(List<Category> data) {
        super();
        this.data = data;
    }
}
