package services.elasticsearch;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Dorinela on 5/27/2017.
 */
public class ElasticsearchService {

    public TransportClient client;

    /**
     * Index participants stats for subcommunities
     *
     * @param participantsStats
     */
    public void indexParticipantsStats(List<Map<String, Object>> participantsStats) {

        try {

            client = new PreBuiltTransportClient(Settings.EMPTY)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("192.168.100.3"), 9300));

            for (Map p : participantsStats) {
                IndexResponse response = client.prepareIndex("participants", "stats")
                        .setSource(p).execute().get();
            }
            //client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public ArrayList<Map> searchByCommunityAndWeek(String communityName, Integer week) {
        ArrayList<Map> result = new ArrayList<Map>();
        try {
            client = new PreBuiltTransportClient(Settings.EMPTY)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("192.168.100.3"), 9300));

            SearchResponse response = client.prepareSearch("participants")
                    .setTypes("stats")
                    .setSize(500)
                    .setQuery(QueryBuilders.matchPhraseQuery("communityName", communityName))
                    .setQuery(QueryBuilders.matchPhraseQuery("week", week))
                    .addSort("Contrib", SortOrder.DESC)
                    .execute()
                    .actionGet();
            SearchHit[] searchHits = response.getHits().getHits();
            for (SearchHit searchHit : searchHits) {
                Map data = searchHit.getSource();
                result.add(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static void main(String[] args) {
        Map<String, Object> test = new HashMap<>();
        test.put("name", "dorinela");
        test.put("age", 24);

        List<Map<String, Object>> list = new ArrayList<>();
        list.add(test);

        ElasticsearchService elasticsearchService = new ElasticsearchService();
        //elasticsearchService.indexParticipantsStats(list);
        System.out.println(elasticsearchService.searchByCommunityAndWeek("CallOfDuty", 1).size());

    }
}
