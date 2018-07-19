package com.readerbench.datasourceprovider.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.json.simple.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dorinela on 24.12.2017.
 */
public class ElasticsearchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchService.class);
    public static TransportClient client;
    private static final String ELASTICSEARCH_HOST_ADDRESS = "141.85.232.48";
    private static final Integer ELSTICSEARCH_PORT = 9300;

    static {
        try {
            Settings settings = Settings.builder()
                    .put("cluster.name", "elasticsearch-readerbench").build();
            client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new TransportAddress(InetAddress.getByName(ELASTICSEARCH_HOST_ADDRESS), ELSTICSEARCH_PORT));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Index participants stats for subcommunities
     *
     * @param participantsStats
     */
    public void indexParticipantsStats(String index, String type, List<Map<String, Object>> participantsStats) {

        try {
            //141.85.232.57
            for (Map participant : participantsStats) {
                // instance a json mapper
                ObjectMapper mapper = new ObjectMapper(); // create once, reuse

                // generate json
                byte[] json = mapper.writeValueAsBytes(participant);

                IndexResponse response = client.prepareIndex(index, type)
                        .setSource(json, XContentType.JSON).get();

            }
            //client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void indexParticipantGraphRepresentation(String index, String type, JSONObject jsonObject) {
        try {
            // instance a json mapper
            ObjectMapper mapper = new ObjectMapper(); // create once, reuse

            // generate json
           // byte[] json = mapper.writeValueAsBytes(jsonObject);

            IndexResponse response = client.prepareIndex(index, type)
                    .setSource(jsonObject, XContentType.JSON).get();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public ArrayList<Map> searchParticipantsStatsPerWeek(String communityName, Integer week) {
//        ArrayList<Map> result = new ArrayList<>();
//        try {
//            client = new PreBuiltTransportClient(Settings.EMPTY)
//                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ELASTICSEARCH_HOST_ADDRESS), 9300));
//
//            QueryBuilder queryBuilder = QueryBuilders
//                    .boolQuery()
//                    .must(QueryBuilders.matchPhraseQuery("communityName", communityName))
//                    .must(QueryBuilders.matchPhraseQuery("week", week));
//
//            SearchResponse response = client.prepareSearch("participants")
//                    .setTypes("stats")
//                    .setSize(10000)
//                    .setQuery(queryBuilder)
//                    .addSort("Contrib", SortOrder.DESC)
//                    .addSort("Scr", SortOrder.DESC)
//                    .execute()
//                    .actionGet();
//            SearchHit[] searchHits = response.getHits().getHits();
//            for (SearchHit searchHit : searchHits) {
//                Map data = searchHit.getSource();
//                result.add(data);
//            }
//        } catch (UnknownHostException e) {
//            LOGGER.error(e.getMessage());
//        }
//
//        return result;
//    }

    public boolean isDuplicate(Map map, List<Map> list) {
        for (Map m : list) {
            if (m.get("participantName").toString().equals(map.get("participantName").toString())) {
                return true;
            }
        }
        return false;
    }

//    public ArrayList<Map> searchParticipantsGraphRepresentation(String index, String type, String communityName) {
//        ArrayList<Map> result = new ArrayList<>();
//        try {
//            client = new PreBuiltTransportClient(Settings.EMPTY)
//                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ELASTICSEARCH_HOST_ADDRESS), 9300));
//
//            SearchResponse response = client.prepareSearch(index)
//                    .setTypes(type)
//                    .setSize(500)
//                    .setQuery(QueryBuilders.matchPhraseQuery("communityName", communityName))
//                    .addSort("week", SortOrder.ASC)
//                    .execute()
//                    .actionGet();
//            SearchHit[] searchHits = response.getHits().getHits();
//            for (SearchHit searchHit : searchHits) {
//                Map data = searchHit.getSource();
//                result.add(data);
//            }
//        } catch (UnknownHostException e) {
//            LOGGER.error(e.getMessage());
//        }
//
//        return result;
//    }

    public ArrayList<String> getAllGamesName() {
        ArrayList<String> result = new ArrayList<>();

        try {
            QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
            SearchResponse response = client.prepareSearch("games")
                    .setTypes("metacritic")
                    //.setQuery(queryBuilder)
                    .setSize(5000)
                    .get();

            SearchHit[] searchHits = response.getHits().getHits();
            for (SearchHit searchHit : searchHits) {
                Map data = new HashMap();
                data = searchHit.getSourceAsMap();
                String review = data.get("name").toString();
                result.add(review);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return result;
    }
    public ArrayList<String> searchByField(String field, String value) {
        ArrayList<String> result = new ArrayList<>();
        try {
            SearchResponse response = client.prepareSearch("reviews")
                    .setTypes("metacritic")
                    //.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                    .setSize(10000)
                    .setQuery(QueryBuilders.matchPhraseQuery(field, value))
                    .get();
            SearchHit[] searchHits = response.getHits().getHits();
            for (SearchHit searchHit : searchHits) {
                Map data = new HashMap();
                data = searchHit.getSourceAsMap();
                String review = data.get("review").toString();
                result.add(review);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public void writeReviewsToFile(String fileName, List<String> reviews) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

            // Loop over the elements in the string array and write each line.
            for (String line : reviews) {
                writer.write(line);
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        ElasticsearchService elasticsearchService = new ElasticsearchService();

        List<String> games = elasticsearchService.getAllGamesName();
        System.out.println("Total number of games: " + games.size());

        for (String game : games) {
            ArrayList<String> reviews = elasticsearchService.searchByField("game", game);
            System.out.println("Total Number of reviews for game " + game + " are " + reviews.size());

            if (reviews != null && !reviews.isEmpty()) {
                String gameName = game.replaceAll(":", "")
                        .replaceAll("-", "").replaceAll("\\?", "")
                        .replaceAll("!", "").replaceAll("/", "").replaceAll("/", "")
                        .replaceAll("\'", "");
                elasticsearchService.writeReviewsToFile("C:\\Users\\Administrator\\Desktop\\projects\\resources\\reviews\\" + gameName + ".txt", reviews);
            }
        }

    }
}
