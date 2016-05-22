package services.nlp.lemmatizer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import data.Lang;

public class StaticLemmatizer {

	static Logger logger = Logger.getLogger(StaticLemmatizer.class);

	private static Map<String, String> lemmas_en = null;
	private static Map<String, String> lemmas_ro = null;
	private static Map<String, String> lemmas_es = null;
	private static Map<String, String> lemmas_nl = null;
	private static Map<String, String> lemmas_la = null;

	private static Map<String, String> initialize(String path, Lang lang) {
		logger.info("Initializing lemmas from " + path);
		Map<String, String> lemmas = new TreeMap<String, String>();
		BufferedReader in;
		try {
			FileInputStream inputFile = new FileInputStream(path);
			// InputStreamReader ir = new InputStreamReader(inputFile,
			// "ISO-8859-1");
			InputStreamReader ir = new InputStreamReader(inputFile, "UTF-8");
			in = new BufferedReader(ir);
			String str_linie = "";
			StringTokenizer strk;
			while ((str_linie = in.readLine()) != null) {
				strk = new StringTokenizer(str_linie, "\t");
				String lemma = strk.nextToken().replaceAll("[0-9]*", "");
				String inflected = strk.nextToken().replaceAll("[0-9]*", "");;
				String existing = lemmas.get(inflected);
				if (existing == null || lemma.length() < existing.length()) {
					lemmas.put(inflected, lemma);
				}
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lemmas;
	}

	public static void writeLemmas(String fileName, Map<String, String> lemmas)
			throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
		for (Map.Entry<String, String> e : lemmas.entrySet()) {
			out.println(e.getValue() + "\t" + e.getKey());
		}
		out.close();
	}

	public static String lemmaStatic(String w, Lang lang) {
		String lemma = null;
		switch (lang) {
		case eng:
			lemma = getLemmasEn().get(w);
			break;
		case fr:
			lemma = StaticLemmatizerPOS.lemmaStatic(w, null, Lang.fr);
			break;
		case it:
			lemma = StaticLemmatizerPOS.lemmaStatic(w, null, Lang.it);
			break;
		case ro:
			lemma = getLemmasRo().get(w);
			break;
		case es:
			lemma = getLemmasEs().get(w);
			break;
		case nl:
			lemma = getLemmasNl().get(w);
			break;
		case la:
			lemma = getLemmasLa().get(w);
			break;
		default:
			lemma = null;
		}

		if (lemma != null) {
			return lemma;
		}
		return w;
	}

	public static Map<String, String> getLemmasEn() {
		if (lemmas_en == null) {
			lemmas_en = initialize("resources/config/Lemmas/lemmas_en.txt", Lang.eng);
		}
		return lemmas_en;
	}

	public static Map<String, String> getLemmasRo() {
		if (lemmas_ro == null) {
			lemmas_ro = initialize("resources/config/Lemmas/lemmas_ro.txt", Lang.ro);
		}
		return lemmas_ro;
	}

	public static Map<String, String> getLemmasEs() {
		if (lemmas_es == null) {
			lemmas_es = initialize("resources/config/Lemmas/lemmas_es.txt", Lang.es);
		}
		return lemmas_es;
	}

	public static Map<String, String> getLemmasNl() {
		if (lemmas_nl == null) {
			lemmas_nl = initialize("resources/config/Lemmas/lemmas_nl.txt", Lang.nl);
		}
		return lemmas_nl;
	}

	public static Map<String, String> getLemmasLa() {
		if (lemmas_la == null) {
			lemmas_la = initialize("resources/config/Lemmas/lemmas_la.txt", Lang.la);
		}
		return lemmas_la;
	}

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		BasicConfigurator.configure();
//		System.out.println(StaticLemmatizer.lemmaStatic("pointés", Lang.fr));
//		System.out.println(StaticLemmatizer.lemmaStatic("mangio", Lang.it));
//		System.out.println(StaticLemmatizer.lemmaStatic("armas", Lang.es));
//		System.out.println(StaticLemmatizer.lemmaStatic("talmpjes", Lang.nl));
		 Map<String, String> lemmas =
		 StaticLemmatizer.initialize("resources/config/Lemmas/lemmas_la.txt",
		 Lang.la);
		 StaticLemmatizer.writeLemmas("resources/config/Lemmas/lemmas_la_new.txt",
		 lemmas);
	}
}
