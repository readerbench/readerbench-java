/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webService.queryResult;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;
import webService.result.ResultAnswerMatching;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
@Root(name = "response")
public class QueryResultAnswerMatching extends QueryResult {
    
    @Path("data")
    @ElementList(inline = true, entry = "result")
    private ResultAnswerMatching data;

    public ResultAnswerMatching getData() {
        return data;
    }

    public void setData(ResultAnswerMatching data) {
        this.data = data;
    }

    public QueryResultAnswerMatching() {
        super();
        data = new ResultAnswerMatching(0, 0.0);
    }
    
}
