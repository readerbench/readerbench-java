/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webService;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openide.util.Exceptions;
import spark.Request;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class LoggerHelper {
    
    public static String requestToString(Request request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Server request received:\n");
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM YYYY HH:mm:ss.S");
        String formattedDate = dateFormat.format(date);
        String ipAddress = request.headers("X-FORWARDED-FOR");  
        if (ipAddress == null || ipAddress.isEmpty()) {  
           ipAddress = request.ip();
        }
        sb.append("Datetime: `").append(formattedDate).append("`\n");
        sb.append("IP address: `").append(ipAddress).append("`\n");
        sb.append("URI: `").append(request.uri()).append("`\n");
        sb.append("Body: ```");
        if (request.body().isEmpty()) sb.append("null\n");
        else {
            sb.append("\n");
            try {
                JSONObject json = (JSONObject) new JSONParser().parse(request.body());
                sb.append(printJsonObject(json));
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        sb.append("```");
        return sb.toString();
    }
    
    public static String printJsonObject(JSONObject jsonObj) {
        StringBuilder sb = new StringBuilder();
        for (Object key : jsonObj.keySet()) {
            String keyStr = (String)key;
            Object keyvalue = jsonObj.get(keyStr);
            sb.append(keyStr).append(" = ").append(keyvalue).append("\n");
            if (keyvalue instanceof JSONObject)
                printJsonObject((JSONObject)keyvalue);
        }
        return sb.toString();
    }
    
}
