package webService.services.lak.result;

import java.util.List;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import webService.queryResult.QueryResult;

/**
 *
 * @author ionutparaschiv
 */
public class QueryResultDocumentYears extends QueryResult {

    @Path("data")
    @ElementList(inline = true, entry = "result")
    private List<Integer> data;

    public List<Integer> getData() {
        return data;
    }

    public void setData(List<Integer> data) {
        this.data = data;
    }

    public QueryResultDocumentYears(List<Integer> data) {
        super();
        this.data = data;
    }
}
