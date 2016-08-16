/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webService.queryResult;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;
import webService.result.ResultPdfToText;

/**
 *
 * @author Gabriel Gutu
 */
@Root(name = "response")
public class QueryResultPdfToText {

    @Element
    private boolean success;

    @Element(name = "errormsg")
    private String errorMsg; // custom error message (optional)

    @Path("data")
    @ElementList(inline = true, entry = "result")
    private ResultPdfToText data;

    private QueryResultPdfToText() {
        success = true;
        errorMsg = "";
        data = new ResultPdfToText("");
    }
}
