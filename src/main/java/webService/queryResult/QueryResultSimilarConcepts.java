/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webService.queryResult;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;
import webService.result.ResultSimilarConcepts;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
@Root(name = "response")
public class QueryResultSimilarConcepts extends QueryResult {

    @Path("data")
    @ElementList(inline = true, entry = "result")
    private ResultSimilarConcepts data; // list of result sentiments

    public ResultSimilarConcepts getData() {
        return data;
    }

    public void setData(ResultSimilarConcepts data) {
        this.data = data;
    }

    public QueryResultSimilarConcepts() {
        super();
        data = new ResultSimilarConcepts(null);
    }

}
