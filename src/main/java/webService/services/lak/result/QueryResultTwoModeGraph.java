/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webService.services.lak.result;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import webService.queryResult.QueryResult;

/**
 *
 * @author ionutparaschiv
 */
public class QueryResultTwoModeGraph extends QueryResult {

    @Path("data")
    @ElementList(inline = true, entry = "result")
    private TwoModeGraph data; // list of result sentiments

    public TwoModeGraph getData() {
        return data;
    }

    public void setData(TwoModeGraph data) {
        this.data = data;
    }

    public QueryResultTwoModeGraph(TwoModeGraph twoModeGraph) {
        super();
        data = twoModeGraph;
    }
}
