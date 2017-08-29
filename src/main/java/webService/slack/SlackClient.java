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
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class SlackClient {
    
    private static final Logger LOGGER = Logger.getLogger("");
    
    private static final String TEST_URL = "https://hooks.slack.com/services/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";
    private static final String URL = "https://hooks.slack.com/services/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";
    
    public static void logMessage(String message) {
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
