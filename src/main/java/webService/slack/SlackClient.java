/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webService.slack;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * This class manages the integration with Slack for logging purposes. It 
 * contains a logMessage method that sends a message to the Slack endpoint, 
 * which is set by a static parameter in the class - URL. For testing purposes 
 * you may switch the URL variable with the TEST_URL variable. Changing this 
 * will lead to a different channel for messages to be sent to.
 * 
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class SlackClient {
    
    private static final Logger LOGGER = Logger.getLogger("");
    
    /*
    * TEST_URL is the hook URL for the #server-local Slack channel. This is 
    * meant to store messages for requests performed on a local server. Please 
    * change it accordingly everytime when working on localhost or integrate 
    * some automated mechanism to change to this URL when necessary.
    * URL is the hook URL for the #server Slack channel. This is meant to store 
    * messages for requests performed on the public server. By analyzing these 
    * requests, one programmer shall be able to determine what was the last 
    * request performed before a crash, for example.
    */
    private static final String TEST_URL = "https://hooks.slack.com/services/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";
    private static final String URL = "https://hooks.slack.com/services/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";
    
    /**
     * This method parses a string to be sent to Slack, builds the necessary 
     * encapsulation for the message and sends it through a POST request to a 
     * dedicated Slack hook URL, which is linked to a Slack channel.
     * 
     * @param message The message to be displayed within the channel.
     */
    public static void logMessage(String message) {
        if (message == null) return;
        HttpClient httpClient = HttpClientBuilder.create().build(); //Use this instead 
        try {
            HttpPost request = new HttpPost(URL);
            StringEntity params = new StringEntity("{\"text\":\"" + message + "\"} ");
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);
            LOGGER.log(Level.INFO, "Sent request details to Slack. Response: {0}", response);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }
    
}
