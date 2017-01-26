/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webService.queryResult;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import webService.result.ResultTopicAdvanced;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class QueryResultTopicAdvanced extends QueryResult {

    @Path("data")
    @ElementList(inline = true, entry = "result")
    private ResultTopicAdvanced data; // list of result sentiments

    public ResultTopicAdvanced getData() {
        return data;
    }

    public void setData(ResultTopicAdvanced data) {
        this.data = data;
    }

    public QueryResultTopicAdvanced() {
        super();
        data = new ResultTopicAdvanced(null, null);
    }

}
