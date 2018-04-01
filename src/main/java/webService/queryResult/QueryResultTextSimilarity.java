/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webService.queryResult;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import webService.result.ResultTextSimilarity;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
@Root(name = "response")
public class QueryResultTextSimilarity extends QueryResult {

    @Path("data")
    @ElementList(inline = true, entry = "result")
    private ResultTextSimilarity data;

    public ResultTextSimilarity getData() {
        return data;
    }

    public void setData(ResultTextSimilarity data) {
        this.data = data;
    }

    public QueryResultTextSimilarity() {
        super();
        data = new ResultTextSimilarity(Double.NEGATIVE_INFINITY);
    }

}
