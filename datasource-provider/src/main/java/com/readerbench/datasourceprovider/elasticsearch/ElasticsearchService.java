package com.readerbench.datasourceprovider.elasticsearch;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.json.simple.JSONObject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dorinela on 24.12.2017.
 */
public class ElasticsearchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchService.class);
    public org.elasticsearch.client.transport.TransportClient client;
    private static final String ELASTICSEARCH_HOST_ADDRESS = "141.85.232.57";

    /**
     * Index participants stats for subcommunities
     *
     * @param participantsStats
     */
    public void indexParticipantsStats(List<Map<String, Object>> participantsStats) {

        try {
            client = new PreBuiltTransportClient(Settings.EMPTY)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ELASTICSEARCH_HOST_ADDRESS), 9300));

            //141.85.232.57
            for (Map p : participantsStats) {
                IndexResponse response = client.prepareIndex("participants", "stats")
                        .setSource(p).execute().get();
            }
            //client.close();
        } catch (InterruptedException | UnknownHostException | ExecutionException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void indexParticipantGraphRepresentation(String index, String type, JSONObject jsonObject) {
        try {
            client = new PreBuiltTransportClient(Settings.EMPTY)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ELASTICSEARCH_HOST_ADDRESS), 9300));

            IndexResponse response = client.prepareIndex(index, type)
                    .setSource(jsonObject).execute().get();

            //client.close();
        } catch (InterruptedException | UnknownHostException | ExecutionException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public ArrayList<Map> searchParticipantsStatsPerWeek(String communityName, Integer week) {
        ArrayList<Map> result = new ArrayList<>();
        try {
            client = new PreBuiltTransportClient(Settings.EMPTY)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ELASTICSEARCH_HOST_ADDRESS), 9300));

            QueryBuilder queryBuilder = QueryBuilders
                    .boolQuery()
                    .must(QueryBuilders.matchPhraseQuery("communityName", communityName))
                    .must(QueryBuilders.matchPhraseQuery("week", week));

            SearchResponse response = client.prepareSearch("participants")
                    .setTypes("stats")
                    .setSize(10000)
                    .setQuery(queryBuilder)
                    .addSort("Contrib", SortOrder.DESC)
                    .addSort("Scr", SortOrder.DESC)
                    .execute()
                    .actionGet();
            SearchHit[] searchHits = response.getHits().getHits();
            for (SearchHit searchHit : searchHits) {
                Map data = searchHit.getSource();
                result.add(data);
            }
        } catch (UnknownHostException e) {
            LOGGER.error(e.getMessage());
        }

        return result;
    }

    public boolean isDuplicate(Map map, List<Map> list) {
        for (Map m : list) {
            if (m.get("participantName").toString().equals(map.get("participantName").toString())) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Map> searchParticipantsGraphRepresentation(String index, String type, String communityName) {
        ArrayList<Map> result = new ArrayList<>();
        try {
            client = new PreBuiltTransportClient(Settings.EMPTY)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ELASTICSEARCH_HOST_ADDRESS), 9300));

            SearchResponse response = client.prepareSearch(index)
                    .setTypes(type)
                    .setSize(500)
                    .setQuery(QueryBuilders.matchPhraseQuery("communityName", communityName))
                    .addSort("week", SortOrder.ASC)
                    .execute()
                    .actionGet();
            SearchHit[] searchHits = response.getHits().getHits();
            for (SearchHit searchHit : searchHits) {
                Map data = searchHit.getSource();
                result.add(data);
            }
        } catch (UnknownHostException e) {
            LOGGER.error(e.getMessage());
        }

        return result;
    }
}
