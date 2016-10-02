/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webService.services.lak.result;

import java.util.List;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import webService.queryResult.QueryResult;

/**
 *
 * @author ionutparaschiv
 */
public class QueryResultTwoModeGraphNodes extends QueryResult {

    @Path("data")
    @ElementList(inline = true, entry = "result")
    private List<TwoModeGraphNode> data;

    public List<TwoModeGraphNode> getData() {
        return data;
    }

    public void setData(List<TwoModeGraphNode> data) {
        this.data = data;
    }

    public QueryResultTwoModeGraphNodes(List<TwoModeGraphNode> twoModeGraphNodeList) {
        super();
        data = twoModeGraphNodeList;
    }
}