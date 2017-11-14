/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package webService.services.essayFeedback;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import webService.queryResult.QueryResult;
import webService.services.essayFeedback.EFResult;

/**
 *
 * @author Robert Botarleanu
 */
public class QueryResultEF extends QueryResult {

    @Path("data")
    @ElementList(inline = true, entry = "result")
    private EFResult data;

    public EFResult getData() {
        return data;
    }

    public void setData(EFResult data) {
        this.data = data;
    }

    public QueryResultEF(EFResult data) {
        super();
        this.data = data;
    }
}
