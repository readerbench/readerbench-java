/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webService.services.lak.result;

import java.util.List;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import services.extendedCNA.GraphMeasure;
import webService.queryResult.QueryResult;

/**
 *
 * @author ionutparaschiv
 */
public class QueryResultGraphMeasures extends QueryResult {

    @Path("data")
    @ElementList(inline = true, entry = "result")
    private List<GraphMeasure> data;

    public List<GraphMeasure> getData() {
        return data;
    }

    public void setData(List<GraphMeasure> data) {
        this.data = data;
    }

    public QueryResultGraphMeasures(List<GraphMeasure> data) {
        super();
        this.data = data;
    }
}
