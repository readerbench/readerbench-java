package webService.services.lak.result;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import webService.queryResult.QueryResult;

public class QueryResultTopicEvolution extends QueryResult {

    @Path("data")
    @ElementList(inline = true, entry = "result")
    private TopicEvolution data;

    public TopicEvolution getData() {
        return data;
    }

    public void setData(TopicEvolution data) {
        this.data = data;
    }

    public QueryResultTopicEvolution(TopicEvolution topicEvolution) {
        super();
        this.data = topicEvolution;
    }
}
