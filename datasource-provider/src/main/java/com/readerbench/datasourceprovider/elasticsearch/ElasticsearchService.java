package com.readerbench.datasourceprovider.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.binding.IntegerBinding;
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
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.Query;

/**
 * Created by dorinela on 24.12.2017.
 */
public class ElasticsearchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchService.class);
    public static TransportClient client;
    private static final String ELASTICSEARCH_HOST_ADDRESS = "127.0.0.1";
    private static final Integer ELSTICSEARCH_PORT = 9300;

    static {
        try {
            Settings settings = Settings.builder()
                    .put("cluster.name", "docker-cluster").build();
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

    public static ArrayList<Game> getAllGames() {
        ArrayList<Game> result = new ArrayList<>();

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
                Integer metascore = Integer.valueOf(data.get("metascore").toString());
                if(data.get("userScore") != null) {
                    Float userscore = Float.valueOf(data.get("userScore").toString());

                    Game game = new Game(review, metascore, userscore);
                    result.add(game);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return result;
    }

    public ArrayList<JSONObject> getDiscussionThreads(String communityIndex, String communityType) {
        ArrayList<JSONObject> result = new ArrayList<>();

        try {
            QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
            SearchResponse response = client.prepareSearch(communityIndex)
                    .setTypes(communityType)
                    .setQuery(queryBuilder)
                    .setSize(5000)
                    .get();

            SearchHit[] searchHits = response.getHits().getHits();
            for (SearchHit searchHit : searchHits) {
                result.add(new JSONObject(searchHit.getSourceAsMap()));
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

    public Set<Review> searchByFieldWithScore(String field, String value) {
        Set<Review> result = new LinkedHashSet<>();
        String newValue = "\"" + value + "\"";
        try {
            SearchResponse response = client.prepareSearch("reviews")
                    .setTypes("metacritic")
                    //.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                    .setSize(10000)
                    .setQuery(QueryBuilders.matchPhraseQuery(field, newValue))
                    //.setQuery(QueryBuilders.termQuery(field, value))
                    .get();
            SearchHit[] searchHits = response.getHits().getHits();
            for (SearchHit searchHit : searchHits) {
                Map data = new HashMap();
                data = searchHit.getSourceAsMap();
                String review = data.get("review").toString();
                Integer score = Integer.valueOf(data.get("rate").toString());
                Review r = new Review(review, score);
                result.add(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * for Alina experiments
     * @param fileName
     * @param reviews
     */
    public void writeReviewsToFile(String fileName, Set<Review> reviews) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

            // Loop over the elements in the string array and write each line.
            for (Review review : reviews) {
                writer.write(review.getScore()+ "\t" + review.getReview() );
                writer.newLine();
            }
            //writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void writeReviewsScoreToFile(String fileName, List<Review> reviews) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

            // Loop over the elements in the string array and write each line.
            for (Review review : reviews) {
                writer.write(review.getScore().toString());
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void writeMetascoreGamesToFile(String fileName, List<Game> games) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

            // Loop over the elements in the string array and write each line.
            for (Game game : games) {
                writer.write(game.getMetascore().toString());
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void writeUserscoreGamesToFile(String fileName, List<Game> games) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

            // Loop over the elements in the string array and write each line.
            for (Game game : games) {
                writer.write(game.getUserscore().toString());
                writer.newLine();
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args) {
        //writeMetaAndUserScore();
        writeReviewsForAllGamesToFile();
    }

    private static void writeReviewsForAllGamesToFile() {
        ElasticsearchService elasticsearchService = new ElasticsearchService();

        List<Game> games = getAllGames();
        System.out.println("Total number of games are: " + games.size());

        Set<Review> allReviews = new HashSet<>();
        for (Game game : games) {

            Set<Review> reviews = elasticsearchService.searchByFieldWithScore("game", game.getName());
            System.out.println("Write " + reviews.size() + " reviews for game " + game.getName());
            if (reviews != null && reviews.size() > 0) {
//                for (Review review : reviews) {
//                    if (!allReviews.contains(review)) {
//                        allReviews.add(review);
//                    }
//                }
                allReviews.addAll(reviews);
            }

        }

        elasticsearchService.writeReviewsToFile("C:\\Users\\Administrator\\ownCloud\\ReaderBench\\in\\Metacritic reviews\\reviews-09-july-2018\\metacritic-reviews.txt", allReviews);

        System.out.println("-------Finish---------");
    }

    private static void writeMetaAndUserScore() {
        ElasticsearchService elasticsearchService = new ElasticsearchService();

        List<Game> games = elasticsearchService.getAllGames();
        List<Game> remainedGames = new ArrayList<>();
        System.out.println("Total number of games: " + games.size());

        for (Game game : games) {
            Set<Review> reviews = elasticsearchService.searchByFieldWithScore("game", game.getName());
            System.out.println("Total Number of reviews for game " + game.getName() + " are " + reviews.size());

            if (reviews != null && !reviews.isEmpty()) {
                remainedGames.add(game);
            }
        }

        elasticsearchService.writeMetascoreGamesToFile("C:\\Users\\Administrator\\ownCloud\\ReaderBench\\in\\Metacritic reviews\\reviews-08-july-2018\\games-metascore.txt", remainedGames);
        elasticsearchService.writeUserscoreGamesToFile("C:\\Users\\Administrator\\ownCloud\\ReaderBench\\in\\Metacritic reviews\\reviews-08-july-2018\\games-userscore.txt", remainedGames);
    }
}
