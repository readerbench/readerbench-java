package services.converters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Selector;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;

class Author {
	public String authorUri;
	public String authorName = "";
	public String affiliationUri = "";
	public String affiliationName = "";
	
	public Author(String authorUri, Model rdfModel) {
		this.authorUri = authorUri;
		this.buildAuthorDataFromModel(rdfModel);
	}
	private void buildAuthorDataFromModel(Model rdfModel) {
		this.buildAuthorName(rdfModel);
		this.buildAuthorUniversityUri(rdfModel);
		this.buildAffiliationName(rdfModel);
	}
	private void buildAuthorName(Model rdfModel) {
		Property typeProperty = rdfModel.getProperty(RdfToDocumentParser.FoafNS + "name");
		StmtIterator stmtIt = rdfModel.listStatements(null, typeProperty, (RDFNode)null);
		while(stmtIt.hasNext()) {
			Statement s = stmtIt.next();
			if(s.getSubject().getURI().equals(this.authorUri)) {
				this.authorName = s.getObject().toString();
				break;
			}
		}
	}
	private void buildAuthorUniversityUri(Model rdfModel) {
		Property typeProperty = rdfModel.getProperty(RdfToDocumentParser.FoafNS + "member");
		StmtIterator stmtIt = rdfModel.listStatements(null, typeProperty, (RDFNode)null);
		while(stmtIt.hasNext()) {
			Statement s = stmtIt.next();
			if(s.getObject().toString().equals(this.authorUri)) {
				this.affiliationUri = s.getSubject().getURI();
				break;
			}
		}
	}
	private void buildAffiliationName(Model rdfModel) {
		Property typeProperty = rdfModel.getProperty(RdfToDocumentParser.RDFSchemaNS + "label");
		StmtIterator stmtIt = rdfModel.listStatements(null, typeProperty, (RDFNode)null);
		while(stmtIt.hasNext()) {
			Statement s = stmtIt.next();
			if(s.getSubject().getURI().equals(this.affiliationUri)) {
				this.affiliationName = s.getObject().toString();
				break;
			}
		}
	}
	
	@Override
	public String toString() {
		return "{" + this.authorUri + ", " + this.authorName + ", " + this.affiliationUri + ", " + this.affiliationName + "}";
	}
}

public class RdfToDocumentParser {
	public static String RdfNS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static String OntowareNS = "http://swrc.ontoware.org/ontology#";
	public static String PureNS = "http://purl.org/dc/elements/1.1/";
	public static String FoafNS = "http://xmlns.com/foaf/0.1/";
	public static String RDFSchemaNS = "http://www.w3.org/2000/01/rdf-schema#";
	public static String TermsNS = "http://ns.nature.com/terms/";
	
	private Model rdfModel;
	private String outputDirectory;
	
	public RdfToDocumentParser(String rdfFilePath, String outputDirectory) {
		this.rdfModel= FileManager.get().loadModel(rdfFilePath);
		this.outputDirectory = outputDirectory;
	}
	
	public void parseRdf() {
		List<String> articleUris = this.indexArticleURI();
		for(int i=0; i<articleUris.size(); i++) {
			String articleUri = articleUris.get(i);
			String title = this.getArticleTitle(articleUri);
			List<Author> authorList = getAuthors(articleUri);
			List<String> topics = getTopics(articleUri);
			String articleAbstract = getArticleAbstract(articleUri);
			String articleYear = getArticleYear(articleUri);
			List<String> citationUris = getCitationUris(articleUri);
			this.saveArticle(i, articleUri, title, authorList, topics, articleAbstract, articleYear, citationUris);
		}
	}
	private List<String> indexArticleURI() {
		Property typeProperty = rdfModel.getProperty(RdfNS + "type");
		Resource articleResource = rdfModel.getResource(OntowareNS + "InProceedings");
		StmtIterator stmtIt = rdfModel.listStatements(null, typeProperty, (RDFNode)null);
		HashSet<String> articleUriSet = new HashSet<String>();
		while(stmtIt.hasNext()) {
			Statement s = stmtIt.next();
			if(s.getResource().equals(articleResource)) {
				articleUriSet.add(s.getSubject().getURI());
			}
		}
		return new ArrayList<String>(articleUriSet);
	}
	private String getArticleTitle(String articleUri) {
		Property typeProperty = rdfModel.getProperty(PureNS + "title");
		StmtIterator stmtIt = rdfModel.listStatements(null, typeProperty, (RDFNode)null);
		while(stmtIt.hasNext()) {
			Statement s = stmtIt.next();
			if(s.getSubject().getURI().equals(articleUri)) {
				return s.getObject().toString();
			}
		}
		return "";
	}
	private List<Author> getAuthors(String articleUri) {
		String authorListUri = articleUri + "/authorlist";
		StmtIterator stmtIt = rdfModel.listStatements();
		HashSet<String> authorUriSet = new HashSet<String>();
		while(stmtIt.hasNext()) {
			Statement s = stmtIt.next();
			if(s.getSubject().getURI().equals(authorListUri)) {
				authorUriSet.add(s.getObject().toString());
			}
		}
		List <Author> authorList = new ArrayList<Author>();
		for(String authorUri : authorUriSet) {
			authorList.add(new Author(authorUri, this.rdfModel));
		}
		return authorList;
	}
	private List<String> getTopics(String articleUri) {
		List<String> topics = new ArrayList<String>();
		Property typeProperty = rdfModel.getProperty(PureNS + "subject");
		StmtIterator stmtIt = rdfModel.listStatements(null, typeProperty, (RDFNode)null);
		while(stmtIt.hasNext()) {
			Statement s = stmtIt.next();
			if(s.getSubject().getURI().equals(articleUri)) {
				RDFNode node = s.getObject();
				if (!node.isURIResource()) {
					topics.add(s.getObject().toString());
				}
			}
		}
		return topics;
	}
	private String getArticleAbstract(String articleUri) {
		Property typeProperty = rdfModel.getProperty(OntowareNS + "abstract");
		StmtIterator stmtIt = rdfModel.listStatements(null, typeProperty, (RDFNode)null);
		while(stmtIt.hasNext()) {
			Statement s = stmtIt.next();
			if(s.getSubject().getURI().equals(articleUri)) {
				return s.getObject().toString();
			}
		}
		return "";
	}
	private String getArticleYear(String articleUri) {
		Property typeProperty = rdfModel.getProperty(OntowareNS + "year");
		StmtIterator stmtIt = rdfModel.listStatements(null, typeProperty, (RDFNode)null);
		while(stmtIt.hasNext()) {
			Statement s = stmtIt.next();
			if(s.getSubject().getURI().equals(articleUri)) {
				return s.getObject().toString();
			}
		}
		return "";
	}
	private List<String> getCitationUris(String articleUri) {
		HashSet<String> citations = new HashSet<String>();
		Property typeProperty = rdfModel.getProperty(TermsNS + "hasCitation");
		StmtIterator stmtIt = rdfModel.listStatements(null, typeProperty, (RDFNode)null);
		while(stmtIt.hasNext()) {
			Statement s = stmtIt.next();
			if(s.getSubject().getURI().equals(articleUri)) {
				citations.add(s.getObject().toString());
			}
		}
		return new ArrayList<String>(citations);
	}
	private void saveArticle(int index, String articleUri, String title, List<Author> authorList, 
			List<String> topics, String articleAbstract, String articleYear, List<String> citationUris) {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
				"<document language=\"EN\">\n" + 
				"<meta>\n" + 
				"<title>" + title + "</title>\n" +
				"<authors>\n";
		for(Author author : authorList) {
			xml += 	"<author>" + 
						"<name>" + author.authorName + "</name>" +
						"<uri>" + author.authorUri + "</uri>" +
						"<affiliationName>" + author.affiliationName + "</affiliationName>" +
						"<affiliationUri>" + author.affiliationUri + "</affiliationUri>" +
					"</author>\n";
		}
		xml += "</authors>\n" +
				"<date>" + articleYear + "</date>\n";
		xml += 	"<source>LAK</source>\n" + 
				"<complexity_level></complexity_level>\n" + 
				"<uri>" + articleUri + "</uri>\n" + 
				"<Topics>\n";
		for(String topic : topics) {
			xml += "<Topic>" + topic + "</Topic>\n";
		}
		xml += "</Topics>\n";
		xml += "<Citations>";
		for(String citationUri : citationUris) {
			xml += "<uri>" + citationUri + "</uri>\n";
		}
		xml += "</Citations>";
		xml += 	"</meta>\n" + 
				"<body>\n" + 
				"<p id=\"" + 0 + "\">" + articleAbstract + "</p>\n" + 
				"</body>\n" +
				"</document>";
		this.writeToFile(this.outputDirectory + File.separator + index + ".xml", xml);
	}
	private void writeToFile(String fileName, String content) {
		try {
			FileWriter fw = new FileWriter(new File(fileName));
			BufferedWriter bfrw = new BufferedWriter(fw);
			bfrw.write(content);			
			bfrw.close();
			fw.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		RdfToDocumentParser parser = new RdfToDocumentParser("in/LAK_corpus/LAK-DATASET-DUMP.rdf", "in/LAK_corpus/parsed-documents");
		parser.parseRdf();
	}
}