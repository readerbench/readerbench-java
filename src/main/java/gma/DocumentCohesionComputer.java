package gma;

import data.AnalysisElement;
import data.discourse.SemanticCohesion;
import data.document.Document;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.jfree.util.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



public class DocumentCohesionComputer {
	private static final long serialVersionUID = 1L;

	private final String PATH_TO_DOC = "in/wiki_doc.xml";
	private final String DOC_FORMAT = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
			+ "<document language=\"EN\">"
			+ "<meta>"
			+ "<title>Blog</title>"
			+ "<author>Unknown</author>"
			+ "<date>16-08-2012</date>"
			+ "<source>Online texts</source>"
			+ "<complexity_level>K-1</complexity_level>"
			+ "</meta>"
			+ "<body>"
			+ "<p id=\"0\">%s</p>" + "</body>" + "</document>";

	private double cohesion = 0;

	public DocumentCohesionComputer(String content, AnalysisElement doc) {

		long initTime = System.currentTimeMillis();
		convertToXML(content);
		long timeOfSerialize  = System.currentTimeMillis();
		System.out.println("TIME TO SERIALIZE = " + (((timeOfSerialize - initTime)/1000)/60) );
		cohesion = new SemanticCohesion(loadDoc(), doc).getCohesion(); //@daniela todo here was a 1
		cohesion = Math.round(cohesion * 100.0) / 100.0;
		long timeOfCohesionCalculus  = System.currentTimeMillis();
		System.out.println("TIME OF COHESION CALCULUS = " + (((timeOfCohesionCalculus - timeOfSerialize)/1000)/60) );
		System.out.println("Document cohesion: " + cohesion);

	}

	//NO-UCD
	public static String getArticleContent(String url) {
		org.jsoup.nodes.Document doc = null;
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Elements paragraphs = doc.select(".mw-content-ltr p");

		Element firstParagraph = paragraphs.first();
		Element lastParagraph = paragraphs.last();
		Element p;
		int i = 1;
		p = firstParagraph;

		// get first paragraph
		String content = p.text();
		// get second paragraph
		p = paragraphs.get(1);
		content += p.text();
		// while (p != lastParagraph) {
		// p = paragraphs.get(i);
		// content += p.text();
		// i++;
		// }

		return content;
	}

	private void convertToXML(String content) {
		String xml = String.format(DOC_FORMAT, content);
		System.out.println(xml);
		
		PrintWriter writer;
		try {
			writer = new PrintWriter(PATH_TO_DOC, "UTF-8");
			writer.println(xml);
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	private Document loadDoc() {
            
            Document doc =  Document.load(new File("in/wiki_doc.xml"), new ArrayList<>(), null, false);
//		Document doc = Document.load("in/wiki_doc.xml", "config/LSA/tasa_en",
//				"config/LDA/tasa_en", Lang.eng, true, true);
//                //todo @Daniela
		//doc.computeAll(null, null, true);

		return doc;
	}

	public double getCohesion() {
		return cohesion;
	}

}
