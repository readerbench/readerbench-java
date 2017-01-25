/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webService.queryResult;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;
import webService.result.ResultTextSimilarities;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
@Root(name = "response")
public class QueryResultTextSimilarities extends QueryResult {
    
    @Path("data")
    @ElementList(inline = true, entry = "result")
    private ResultTextSimilarities data;

    public ResultTextSimilarities getData() {
        return data;
    }

    public void setData(ResultTextSimilarities data) {
        this.data = data;
    }

    public QueryResultTextSimilarities() {
        super();
        data = new ResultTextSimilarities(null);
    }
    
}
