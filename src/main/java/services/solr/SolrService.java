package services.solr;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created by Dorinela on 3/12/2017.
 */
public class SolrService {

    private static Properties SOLR_PROPERTIES = getProperties("solr.properties");
    private static String SOLR_ADDRESS = SOLR_PROPERTIES.getProperty("solr.address");
    private static String SOLR_COLLECTION = SOLR_PROPERTIES.getProperty("solr.collection");
    private static Integer MAX_RECORDS_NUMBER = 50000;

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SolrService.class);

    /**
     * Get data from SOLR collection
     * @param query - query (eg. "name:John")
     * @param rowsNumber - number of records from SOLR collection
     * @return
     */
    private SolrDocumentList getDataByQuery(String query, Integer rowsNumber) {

        SolrDocumentList solrDocuments = new SolrDocumentList();
        SolrClient solr = new HttpSolrClient.Builder(SOLR_ADDRESS + SOLR_COLLECTION).build();
        SolrQuery solrQuery = new SolrQuery().setRows(rowsNumber);

        solrQuery.set("q", query);

        try {
            QueryResponse response = solr.query(solrQuery);
            solrDocuments = response.getResults();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return solrDocuments;

    }

    /**
     * Get data by community name
     * @param communityName
     * @return
     */
    public SolrDocumentList getDataByCommunityName(String communityName) {
        String query = "subreddit:" + communityName;
        SolrDocumentList solrDocuments = getDataByQuery(query, MAX_RECORDS_NUMBER);
        LOGGER.info("The number of SolrDocuments for query: " + "\"" + query + "\"" + " is: " +
                solrDocuments.getNumFound());

        return solrDocuments;
    }

    /**
     * read data from .properties files
     * @param resourceName
     * @return
     */
    public static Properties getProperties (String resourceName) {

        Properties properties = new Properties();
        InputStream input;
        try {

            input = new SolrService().getClass().getClassLoader().getResourceAsStream(resourceName);
            if (input == null) {
                throw new RuntimeException("Configuration file missing: " + resourceName);
            }
            properties.load(input);

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return properties;
    }

    public static void main(String[] args) {

        SolrService solrService = new SolrService();
        solrService.getDataByQuery("subreddit:skyrim", 20000);

    }
}
