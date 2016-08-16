package services.mail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import javax.ws.rs.core.MediaType;

public class SendMail {
	
	private static Logger logger = Logger.getLogger(SendMail.class);
	
	private static String apiKey = "key-5e4bf79654ee1197436b79057c6f43d9";
	private static String domainName = "readerbench.com";
	
	public static ClientResponse sendSimpleMessage(HashMap<Object, Object> hm) {
			assert hm.get("from") != null;
			assert hm.get("to") != null;
			assert hm.get("subject") != null;
			assert hm.get("message") != null;
			
	       Client client = Client.create();
	       client.addFilter(new HTTPBasicAuthFilter("api", SendMail.apiKey));
	       WebResource webResource = client.resource("https://api.mailgun.net/v3/" + SendMail.domainName + "/messages");
	       MultivaluedMapImpl formData = new MultivaluedMapImpl();
	       
	       // set FROM field
	       HashMap<String, String> hmFrom = (HashMap<String, String>) hm.get("from");
	       if (hmFrom.get("name") != null) {
	    	   formData.add("from", hmFrom.get("name").toString() + ' ' + '<' + hmFrom.get("email").toString() + '>');
	       }
	       else {
	    	   formData.add("from", hmFrom.get("email"));
	       }
	       
	       // set TO field
	       if (hm.get("to") instanceof ArrayList) {
	    	   // multiple receivers
	    	   List<HashMap<String, String>> hmReceivers = (ArrayList<HashMap<String, String>>) hm.get("to");
	    	   for(HashMap<String, String> hmReceiver : hmReceivers) {
	    		   if (hmReceiver.get("name") != null) {
	    			   formData.add("to", hmReceiver.get("name").toString() + ' ' + '<' + hmReceiver.get("email").toString() + '>');
	    		   }
	    		   else {
	    			   formData.add("to", hmReceiver.get("email").toString());
	    		   }
	    	   }
	       }
	       else {
	    	   // single receiver
	    	   HashMap<String, String> hmTo = (HashMap<String, String>) hm.get("to");
	    	   if (hmTo.get("email") != null) {
		    	   if (hmTo.get("name") != null) {
		    		   formData.add("to", hmTo.get("name").toString() + ' ' + '<' + hmTo.get("email").toString() + '>');
		    	   }
		    	   else {
		    		   formData.add("to", hmTo.get("email").toString());
		    	   }
		       }
	       }
	       
	       formData.add("subject", hm.get("subject"));
	       formData.add("text", hm.get("message"));
	       return webResource.type(MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class, formData);
	}
	
	public static void main(String args[]) throws Exception {
        
		HashMap<Object, Object> hm = new HashMap<Object, Object>();
		HashMap<String, String> hmFrom = new HashMap<String, String>();
		hmFrom.put("name", "Gabi");
		hmFrom.put("email", "gabi.gutu@readerbench.com");
		hm.put("from", hmFrom);
		
		List<HashMap<String, String>> hmReceivers = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> sender1 = new HashMap<String, String>();
		sender1.put("name", "Gabi G");
		sender1.put("email", "gabi.gutu@gmail.com");
		HashMap<String, String> sender2 = new HashMap<String, String>();
		sender2.put("name", "Gabi H");
		sender2.put("email", "havefunwebmaster@gmail.com");
		hmReceivers.add(sender1);
		hmReceivers.add(sender2);
		hm.put("to", hmReceivers);
		hm.put("subject", "Advanced send e-mail");
		hm.put("message", "If you received this message, SMTP works on ReaderBench server and the advanced method was used.");
		SendMail.sendSimpleMessage(hm);
		
		HashMap<String, String> hmSimpleReceiver = new HashMap<String, String>();
		hmSimpleReceiver.put("name", "Gabi G");
		hmSimpleReceiver.put("email", "gabi.gutu@gmail.com");
		hm.put("to", hmSimpleReceiver);
		hm.put("subject", "Simple send e-mail");
		hm.put("message", "If you received this message, SMTP works on ReaderBench server and the advanced method was used, but with simple user.");
		SendMail.sendSimpleMessage(hm);
        
    }

}
