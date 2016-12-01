package view.widgets.chat.models;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class WikiResult {

	private String snippet;
	private String title;
	private String link;
	public double cohesion = 0.0;

	public WikiResult(String title, String snippet) {
		this.snippet = snippet;
		this.title = title;
		try {
			link = "http://en.wikipedia.org/wiki/"+ URLEncoder.encode(title.replaceAll(" ", "_"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getLink() {
		return link;
	}
	
	public String getSnippet() {
		return snippet;
	}
	
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return ((WikiResult)obj).getLink().equalsIgnoreCase(link);
	}

}
