/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webService.services.cimodel.result;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import webService.queryResult.QueryResult;

/**
 *
 * @author ionutparaschiv
 */
public class QueryResultCM extends QueryResult {

    @Path("data")
    @ElementList(inline = true, entry = "result")
    private CMResult data;

    public CMResult getData() {
        return data;
    }

    public void setData(CMResult data) {
        this.data = data;
    }

    public QueryResultCM(CMResult data) {
        super();
        this.data = data;
    }
}
