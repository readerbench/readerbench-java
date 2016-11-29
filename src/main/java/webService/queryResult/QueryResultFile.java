/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webService.queryResult;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import webService.result.ResultFile;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class QueryResultFile extends QueryResult {
    
    @Path("data")
    @ElementList(inline = true, entry = "result")
    private ResultFile data;

    public ResultFile getData() {
        return data;
    }

    public void setData(ResultFile data) {
        this.data = data;
    }

    public QueryResultFile() {
        super();
        data = new ResultFile(null, -1);
    }
    
}
