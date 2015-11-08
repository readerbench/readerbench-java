package services.commons;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import services.readingStrategies.PatternMatching;

import edu.cmu.lti.jawjaw.pobj.Lang;

public class TextPreprocessing {
	static Logger logger = Logger.getLogger(TextPreprocessing.class);

	public static String replaceFrCorpusAdnotations(String s, Lang lang) {
		if (lang.equals(Lang.fr))
			return s.replaceAll("_ei", "é").replaceAll("_eo", "è")
					.replaceAll("_ea", "ê").replaceAll("_ee", "ë")
					.replaceAll("_ao", "à").replaceAll("_ae", "ä")
					.replaceAll("_aa", "â").replaceAll("_ie", "ï")
					.replaceAll("_ia", "î").replaceAll("_oe", "ö")
					.replaceAll("_oa", "ô").replaceAll("_ue", "ü")
					.replaceAll("_ua", "û").replaceAll("_uo", "ù")
					.replaceAll("_cz", "ç");
		return s;
	}

	public static String cleanText(String text, Lang lang) {
		// clean initial text

		// lowercase + eliminate numbers
		String result = text.toLowerCase().replaceAll("\\s+", " ").trim()
				.replaceAll("[-+]?\\d+(\\.\\d+)?", "")
				.replaceAll("[-+]?\\d+(,\\d+)?", "");

		// eliminate non-relevant characters
		switch (lang) {
		case fr:
			result = result.replaceAll(
					"[^a-zàâäæçéêèëîïôœùûü ,:;'\\-\\.\\!\\?\n]", " ");
			break;
		case ro:
			result = result.replaceAll("ş", "ş").replaceAll("ț", "ţ");
			result = result.replaceAll("[^a-zăâîşţ ,:;'\\-\\.\\!\\?\n]", " ");
			break;
		case es:
			result = result.replaceAll("[^a-zñóéíáúü ,:;'\\-\\.\\!\\?\n]", " ");
			break;
		default:
			result = result.replaceAll("[^a-z ,:;'\\-\\.\\!\\?\n]", " ");
			break;
		}
		// add spaces for Stanford Parser
		result = result.replaceAll("'", "' ");
		result = result.replaceAll(",", " , ");
		result = result.replaceAll("\\.", " \\. ");
		result = result.replaceAll(";", " ; ");
		result = result.replaceAll(":", " : ");
		result = result.replaceAll("\\!", " \\! ");
		result = result.replaceAll("\\?", " \\? ");
		result = result.replaceAll("\\-", " \\- ");

		// eliminate surplus of spaces
		result = result.replaceAll(" +", " ");

		return result;
	}

	public static String cleanVerbalisation(String text) {
		String result = " " + text.toLowerCase().trim() + " ";

		result = result.replaceAll("’", "'");
		result = result.replaceAll("//", " ");
		result = result.replaceAll("\\?/", "/");
		result = result.replaceAll("…", " ");
		result = result.replaceAll("'", "' ");
		result = result.replaceAll("\\.\\.(\\.)+", " ");
		result = result.replaceAll("\\.\\.", "\\.");
		result = result.replaceAll("(\\?)+", "\\?");
		result = result.replaceAll("(!)+", "!");
		result = result.replaceAll(" - ", "");
		result = result.replaceAll(
				"[^a-zàâäæçéêèëîïôœùûü ,:;'\\(\\)\\-\\.\\!\\?\n]", "");

		result = result.replaceAll(" j' ", " je ");
		result = result.replaceAll(" d' ", " de ");
		result = result.replaceAll(" e+ ", " ");
		result = result.replaceAll(" (e)+(\\.)+ ", " ");
		result = result.replaceAll(" y(y)+ ", " il ");
		result = result.replaceAll("( i)+", " i");
		result = result.replaceAll(" ben ", " ");
		result = result.replaceAll(" euh ", " ");

		result = result.replaceAll(" iza", " ils a");
		result = result.replaceAll(" i za", " ils a");
		result = result.replaceAll(" ize", " ils e");
		result = result.replaceAll(" i ze", " ils e");
		result = result.replaceAll(" izé", " ils é");
		result = result.replaceAll(" i zé", " ils é");
		result = result.replaceAll(" izè", " ils è");
		result = result.replaceAll(" i zè", " ils è");
		result = result.replaceAll(" izi", " ils i");
		result = result.replaceAll(" i zi ", " ils y ");
		result = result.replaceAll(" i zi", " ils i");
		result = result.replaceAll(" izo", " ils o");
		result = result.replaceAll(" i zo", " ils o");
		result = result.replaceAll(" izu", " ils u");
		result = result.replaceAll(" i zu", " ils u");
		result = result.replaceAll(" i+ ", " il ");
		result = result.replaceAll(" y z' ont ", " ils ont ");
		result = result.replaceAll(" z' ont ", " ils ont ");

		result = result.replaceAll(" qui za", " qu'ils a");
		result = result.replaceAll(" qui ze", " qu'ils e");
		result = result.replaceAll(" qui zé", " qu'ils é");
		result = result.replaceAll(" qui zè", " qu'ils è");
		result = result.replaceAll(" qui zi ", " qu'ils y ");
		result = result.replaceAll(" qui zi", " qu'ils i");
		result = result.replaceAll(" qui zo", " qu'ils o");
		result = result.replaceAll(" qui zu", " qu'ils u");

		result = result.replaceAll("qu ", "qu' ");
		result = result.replaceAll("qu' ", "que ");

		// eliminate multiple occurrences

		result = result.replaceAll("( qui)+", " qui");
		result = result.replaceAll("( la)+", " la");
		result = result.replaceAll("( le)+", " le");
		result = result.replaceAll("( les)+", " les");
		result = result.replaceAll("( elle)+", " elle");
		result = result.replaceAll("( il)+", " il");
		result = result.replaceAll("( elles)+", " elles");
		result = result.replaceAll("( ils)+", " ils");
		result = result.replaceAll("( du)+", " du");
		result = result.replaceAll("( de)+", " de");
		result = result.replaceAll("( un)+", " un");
		result = result.replaceAll("( une)+", " une");
		result = result.replaceAll("( à)+", " à");
		result = result.replaceAll("( et)+", " et");
		result = result.replaceAll("( dans)+", " dans");
		result = result.replaceAll("( mais)+", " mais");
		for (String pattern : PatternMatching.PATTERNS_CAUSALITY_FR.getWords())
			result = result.replaceAll("( " + pattern + ")+", " " + pattern);
		for (String pattern : PatternMatching.PATTERNS_CONTROL_FR.getWords())
			result = result.replaceAll("( " + pattern + ")+", " " + pattern);

		// frequent spell-checking errors
		result = result.replaceAll(" entrain ", " en train ");
		result = result.replaceAll(" pace ", " parce ");
		result = result.replaceAll(" f' sait ", " faisait ");
		result = result.replaceAll(" r' sortait ", " resortait ");
		result = result.replaceAll(" d' mander ", " demander ");
		result = result.replaceAll(" d' vant ", " devant ");
		result = result.replaceAll(" d' venait ", " devenait ");

		// eliminate text within ()
		result = result.replaceAll("\\(indicible\\)", " ");
		result = result.replaceAll("\\(d' accord\\)", " ");
		result = result.replaceAll("\\(.*\\)", "");

		// eliminate surplus spaces
		result = result.replaceAll("\\s+", " ");

		return result.trim();
	}

	public static String doubleCleanVerbalization(String s) {
		return cleanVerbalisation(cleanVerbalisation(s));
	}

	public static void displayCleaningResults(String pathToDirectory) {
		// parse the XML file
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			File directory = new File(pathToDirectory);
			if (directory.isDirectory()) {
				File[] files = directory.listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						if (pathname.getName().endsWith(".xml"))
							return true;
						return false;
					}
				});
				for (File f : files) {
					try {
						InputSource input = new InputSource(
								new FileInputStream(f));
						input.setEncoding("UTF-8");

						DocumentBuilder db = dbf.newDocumentBuilder();
						org.w3c.dom.Document dom = db.parse(input);

						Element doc = dom.getDocumentElement();

						Element el;
						NodeList nl;
						// get author
						String author = "";
						nl = doc.getElementsByTagName("author");
						if (nl != null && nl.getLength() > 0) {
							el = (Element) nl.item(0);
							author = el.getFirstChild().getNodeValue();
						}
						System.out.println(author);
						// determine contents
						String contents = "";

						nl = doc.getElementsByTagName("verbalisation");
						if (nl != null && nl.getLength() > 0) {
							for (int i = 0; i < nl.getLength(); i++) {
								el = (Element) nl.item(i);
								contents = el.getFirstChild().getNodeValue()
										.trim();
								System.out
										.println(contents
												+ "\n---------------\n"
												+ cleanText(
														doubleCleanVerbalization(contents),
														Lang.fr) + "\n");
							}
						}
					} catch (Exception e) {
						logger.error("Error processing " + f.getName() + ": "
								+ e.getMessage());
					}
				}
			}

		} catch (Exception e) {
			System.err.print("Error evaluating input directory "
					+ pathToDirectory + "!");
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();
		System.out
				.println(cleanVerbalisation("alors parce que i mangent i zarrete devant alors e alors la pace qu pusqu' télé et iii .. i et  ii  puis???"));
		// displayCleaningResults("in/Matilda/MATILDA_CE2/parts");
	}
}
