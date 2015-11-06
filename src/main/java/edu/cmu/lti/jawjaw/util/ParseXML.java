package edu.cmu.lti.jawjaw.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.cmu.lti.jawjaw.pobj.Lang;
import edu.cmu.lti.jawjaw.pobj.Link;
import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.jawjaw.pobj.Sense;
import edu.cmu.lti.jawjaw.pobj.Synlink;
import edu.cmu.lti.jawjaw.pobj.Synset;
import edu.cmu.lti.jawjaw.pobj.SynsetDef;
import edu.cmu.lti.jawjaw.pobj.WordJAW;

public class ParseXML {
	static Logger logger = Logger.getLogger(ParseXML.class);

	private List<WordJAW> words = new LinkedList<WordJAW>();
	private List<String> lemmas = new LinkedList<String>();
	private Map<String, LinkedList<String>> senseToLexId = new TreeMap<String, LinkedList<String>>();
	private List<Sense> senses = new LinkedList<Sense>();
	private List<Synset> synsets = new LinkedList<Synset>();
	private List<SynsetDef> synsetDefs = new LinkedList<SynsetDef>();
	private List<Synlink> synlinks = new LinkedList<Synlink>();

	private String path;
	private String fileName;
	private String source;
	private Lang lang;

	private Document parseXmlFile(String path, String fileName, String source, Lang lang) {
		this.path = path;
		this.fileName = fileName;
		this.source = source;
		this.lang = lang;
		// parse the XML file
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document dom = null;
		try {
			InputSource input = new InputSource(new FileInputStream(new File(this.path + "/" + this.fileName)));
			input.setEncoding("UTF-8");

			DocumentBuilder db = dbf.newDocumentBuilder();
			dom = db.parse(input);
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return dom;
	}

	public void parseDocument(Document dom) {
		logger.info("Parsing XML file...");
		// get the root element
		Element docEle = dom.getDocumentElement();

		// get a synset
		NodeList nl = docEle.getElementsByTagName("SYNSET");
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {
				Element el = (Element) nl.item(i);
				processSynset(el);
			}
		}
	}

	private void processSynset(Element sysnE1) {
		String synset = getTextValue(sysnE1, "ID");
		String pos = getTextValue(sysnE1, "POS");
		String name = addSenses(sysnE1, synset, pos);
		synsets.add(new Synset(synset, POS.valueOf(pos), name, source));

		addLinks(sysnE1, synset);

		String def = getTextValue(sysnE1, "DEF");
		synsetDefs.add(new SynsetDef(synset, lang, def, 0));
	}

	private void addLinks(Element sysnE1, String synset) {
		NodeList nl = sysnE1.getElementsByTagName("ILR");
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {
				Element el = (Element) nl.item(i);
				String type = getTextValue(el, "TYPE");
				String id = el.getTextContent();
				if (id.indexOf('E') >= 0) {
					id = id.substring(id.indexOf('E'));
					synlinks.add(new Synlink(synset, id, Link.valueOf(type), source));
				} else
					logger.error("Error processing " + el.getTextContent() + "...");
			}
		}
	}

	private String addSenses(Element sysnE1, String synset, String pos) {
		String name = "";
		String lemma;
		String sense;
		int lexid = 0;
		NodeList nl = sysnE1.getElementsByTagName("SYNONYM");
		if (nl != null && nl.getLength() > 0) {
			Element el = (Element) nl.item(0);
			NodeList litList = el.getElementsByTagName("LITERAL");
			if (litList != null && litList.getLength() > 0)
				for (int i = 0; i < litList.getLength(); i++) {
					Element e = (Element) litList.item(i);
					lemma = e.getFirstChild().getTextContent().toLowerCase();

					NodeList senseList = e.getElementsByTagName("SENSE");
					sense = "";
					if (senseList != null && senseList.getLength() > 0) {
						sense = senseList.item(0).getTextContent();
					}

					if (!lemmas.contains(lemma)) {
						// create corresponding word
						lemmas.add(lemma);
						words.add(new WordJAW(lemmas.indexOf(lemma), lang, lemma, null, POS.valueOf(pos)));
						senseToLexId.put(lemma, new LinkedList<String>());
					}
					// check if sense already exists
					if (!senseToLexId.get(lemma).contains(sense)) {
						senseToLexId.get(lemma).add(sense);
					}
					lexid = senseToLexId.get(lemma).indexOf(sense) + 1;
					senses.add(new Sense(synset, lemmas.indexOf(lemma), lang, 0, lexid, 0, source));
					name = lemma;
				}
		}
		return name;
	}

	private String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0) {
			Element el = (Element) nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}

		return textVal;
	}

	private void writeAllParseResults() throws Exception {
		logger.info("Creating SQL lite DB...");

		Class.forName("org.sqlite.JDBC");
		Connection conn = DriverManager.getConnection("jdbc:sqlite:" + path + "/" + fileName.replace(".xml", ".db"));

		// write words
		Statement stat = conn.createStatement();
		stat.executeUpdate("drop table if exists word;");
		stat.executeUpdate("create table word (wordid, lang, lemma, pron, pos);");
		stat.executeUpdate("drop table if exists sense;");
		stat.executeUpdate("create table sense (synset, wordid, lang, rank, lexid, freq, src);");
		stat.executeUpdate("drop table if exists synset;");
		stat.executeUpdate("create table synset (synset, pos, name, src);");
		stat.executeUpdate("drop table if exists synset_def;");
		stat.executeUpdate("create table synset_def (synset, lang, def, sid);");
		stat.executeUpdate("drop table if exists synlink;");
		stat.executeUpdate("create table synlink (synset1, synset2, link, src);");
		PreparedStatement prep = conn.prepareStatement("insert into word values (?, ?, ?, ?, ?);");

		for (WordJAW w : words) {
			prep.setInt(1, w.getWordid());
			prep.setString(2, w.getLang().toString());
			prep.setString(3, w.getLemma());
			prep.setString(4, w.getPron());
			prep.setString(5, w.getPos().toString());
			prep.addBatch();
		}

		conn.setAutoCommit(false);
		prep.executeBatch();
		conn.setAutoCommit(true);

		prep = conn.prepareStatement("insert into sense values (?, ?, ?, ?, ?, ?, ?);");

		for (Sense s : senses) {
			prep.setString(1, s.getSynset());
			prep.setInt(2, s.getWordid());
			prep.setString(3, s.getLang().toString());
			prep.setInt(4, s.getRank());
			prep.setInt(5, s.getLexid());
			prep.setInt(6, s.getFreq());
			prep.setString(7, s.getSrc());
			prep.addBatch();
		}

		conn.setAutoCommit(false);
		prep.executeBatch();
		conn.setAutoCommit(true);

		prep = conn.prepareStatement("insert into synset values (?, ?, ?, ?);");

		for (Synset s : synsets) {
			prep.setString(1, s.getSynset());
			prep.setString(2, s.getPos(lang).toString());
			prep.setString(3, s.getName(lang));
			prep.setString(4, s.getSrc());
			prep.addBatch();
		}

		conn.setAutoCommit(false);
		prep.executeBatch();
		conn.setAutoCommit(true);

		prep = conn.prepareStatement("insert into synset_def values (?, ?, ?, ?);");

		for (SynsetDef s : synsetDefs) {
			prep.setString(1, s.getSynset());
			prep.setString(2, s.getLang().toString());
			prep.setString(3, s.getDef());
			prep.setInt(4, s.getSid());
			prep.addBatch();
		}

		conn.setAutoCommit(false);
		prep.executeBatch();
		conn.setAutoCommit(true);

		prep = conn.prepareStatement("insert into synlink values (?, ?, ?, ?);");

		for (Synlink s : synlinks) {
			prep.setString(1, s.getSynset1());
			prep.setString(2, s.getSynset2());
			prep.setString(3, s.getLink().toString());
			prep.setString(4, s.getSrc());
			prep.addBatch();
		}

		conn.setAutoCommit(false);
		prep.executeBatch();
		conn.setAutoCommit(true);

		conn.close();
	}

	public void parse(String path, String fileName, String source, Lang lang) throws Exception {
		parseDocument(parseXmlFile(path, fileName, source, lang));
		writeAllParseResults();
	}

	public static void main(String[] args) throws Exception {
		BasicConfigurator.configure();

		ParseXML parser = new ParseXML();
		parser.parse("resources/config/WN", "wnrom.xml", "WNROM", Lang.ro);

		// parser.parse("resources/config/WN", "wolf-0.1.6.xml", "WOLF", Lang.fr);
	}

}
