package services.commons;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.EnumMap;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import data.Lang;
import edu.stanford.nlp.util.Pair;
import services.readingStrategies.PatternMatching;

public class TextPreprocessing {

	static Logger logger = Logger.getLogger(TextPreprocessing.class);

	// lowercase + eliminate numbers
	private static final Pair[] INITIAL = { new Pair<>(Pattern.compile("\\s+"), " "),
			new Pair<>(Pattern.compile("[-+]?\\d+(\\.\\d+)?"), ""),
			new Pair<>(Pattern.compile("[-+]?\\d+(,\\d+)?"), "") };

	// Language specific
	private static final EnumMap<Lang, Pair[]> LANG_PATTERNS = new EnumMap<>(Lang.class);
	static {
		Pair[] patterns = {
				new Pair<Pattern, String>(Pattern.compile("[^a-zàâäæçéêèëîïôœùûü ,:;'\\-\\.\\!\\?\n]"), " ") };
		LANG_PATTERNS.put(Lang.fr, patterns);

		patterns = new Pair[] { new Pair<>(Pattern.compile("ş"), "ş"), new Pair<>(Pattern.compile("ț"), "ţ"),
				new Pair<>(Pattern.compile("[^a-zăâîşţ ,:;'\\-\\.\\!\\?\n]"), " ") };
		LANG_PATTERNS.put(Lang.ro, patterns);

		patterns = new Pair[] { new Pair<>(Pattern.compile("[^a-zñóéíáúü ,:;'\\-\\.\\!\\?\n]"), " ") };
		LANG_PATTERNS.put(Lang.es, patterns);

		patterns = new Pair[] {
				new Pair<>(Pattern.compile("[^a-zäëÿüïöáéýúíóàèùìòãñõâêûîôç ,:;'\\-\\.\\!\\?\n]"), " ") };
		LANG_PATTERNS.put(Lang.nl, patterns);

		patterns = new Pair[] { new Pair<>(Pattern.compile("[^a-z ,:;'\\-\\.\\!\\?\n]"), " ") };
		LANG_PATTERNS.put(Lang.eng, patterns);

		patterns = new Pair[] { // replace accents
				new Pair<>(Pattern.compile("[áàâä]"), "a"), new Pair<>(Pattern.compile("[èéêë]"), "e"),
				new Pair<>(Pattern.compile("[íìîï]"), "i"), new Pair<>(Pattern.compile("[óòôö]"), "o"),
				new Pair<>(Pattern.compile("[úùûü]"), "u"),
				new Pair<>(Pattern.compile("[^a-z ,:;'\\-\\.\\!\\?\n]"), " ") };
		LANG_PATTERNS.put(Lang.la, patterns);
	}

	private static final Pair[] FINAL_PATTERNS = { new Pair<>(Pattern.compile("'"), "' "),
			new Pair<>(Pattern.compile(","), " , "), new Pair<>(Pattern.compile("\\."), " \\. "),
			new Pair<>(Pattern.compile(";"), " ; "), new Pair<>(Pattern.compile(":"), " : "),
			new Pair<>(Pattern.compile("\\!"), " \\! "), new Pair<>(Pattern.compile("\\?"), " \\? "),
			new Pair<>(Pattern.compile("\\-"), " \\- "), new Pair<>(Pattern.compile(" +"), " ") };

	public static String cleanText(String text, Lang lang) {
		// clean initial text

		// lowercase + eliminate numbers
		String result = text.toLowerCase();
		for (Pair<Pattern, String> p : INITIAL) {
			result = p.first.matcher(result).replaceAll(p.second).trim();
		}

		Pair<Pattern, String>[] patterns;
		if (LANG_PATTERNS.containsKey(lang)) {
			patterns = LANG_PATTERNS.get(lang);
		} else {
			patterns = LANG_PATTERNS.get(Lang.eng);
		}
		for (Pair<Pattern, String> p : patterns) {
			result = p.first.matcher(result).replaceAll(p.second);
		}
		for (Pair<Pattern, String> p : FINAL_PATTERNS) {
			result = p.first.matcher(result).replaceAll(p.second);
		}
		return result;
	}

	public static String cleanVerbalisation(String text) {
		String result = " " + text.toLowerCase().trim() + " ";

		for (Pair<Pattern, String> p : INITIAL) {
			result = p.first.matcher(result).replaceAll(p.second);
		}

		Pair[] cleaning = { new Pair<>(Pattern.compile(" j' "), " je "), new Pair<>(Pattern.compile(" d' "), " de "),
				new Pair<>(Pattern.compile(" e+ "), " "), new Pair<>(Pattern.compile(" (e)+(\\.)+ "), " "),
				new Pair<>(Pattern.compile(" y(y)+ "), " il "), new Pair<>(Pattern.compile("( i)+"), " i"),
				new Pair<>(Pattern.compile(" ben "), " "), new Pair<>(Pattern.compile(" euh "), " "),

				new Pair<>(Pattern.compile(" iza"), " ils a"), new Pair<>(Pattern.compile(" i za"), " ils a"),
				new Pair<>(Pattern.compile(" ize"), " ils e"), new Pair<>(Pattern.compile(" i ze"), " ils e"),
				new Pair<>(Pattern.compile(" izé"), " ils é"), new Pair<>(Pattern.compile(" i zé"), " ils é"),
				new Pair<>(Pattern.compile(" izè"), " ils è"), new Pair<>(Pattern.compile(" i zè"), " ils è"),
				new Pair<>(Pattern.compile(" izi"), " ils i"), new Pair<>(Pattern.compile(" i zi "), " ils y "),
				new Pair<>(Pattern.compile(" i zi"), " ils i"), new Pair<>(Pattern.compile(" izo"), " ils o"),
				new Pair<>(Pattern.compile(" i zo"), " ils o"), new Pair<>(Pattern.compile(" izu"), " ils u"),
				new Pair<>(Pattern.compile(" i zu"), " ils u"), new Pair<>(Pattern.compile(" i+ "), " il "),
				new Pair<>(Pattern.compile(" y z' ont "), " ils ont "),
				new Pair<>(Pattern.compile(" z' ont "), " ils ont "),

				new Pair<>(Pattern.compile(" qui za"), " qu'ils a"),
				new Pair<>(Pattern.compile(" qui ze"), " qu'ils e"),
				new Pair<>(Pattern.compile(" qui zé"), " qu'ils é"),
				new Pair<>(Pattern.compile(" qui zè"), " qu'ils è"),
				new Pair<>(Pattern.compile(" qui zi "), " qu'ils y "),
				new Pair<>(Pattern.compile(" qui zi"), " qu'ils i"),
				new Pair<>(Pattern.compile(" qui zo"), " qu'ils o"),
				new Pair<>(Pattern.compile(" qui zu"), " qu'ils u"),

				new Pair<>(Pattern.compile("qu "), "qu' "), new Pair<>(Pattern.compile("qu' "), "que "),

				// eliminate multiple occurrences
				new Pair<>(Pattern.compile("( qui)+"), " qui"), new Pair<>(Pattern.compile("( la)+"), " la"),
				new Pair<>(Pattern.compile("( le)+"), " le"), new Pair<>(Pattern.compile("( les)+"), " les"),
				new Pair<>(Pattern.compile("( elle)+"), " elle"), new Pair<>(Pattern.compile("( il)+"), " il"),
				new Pair<>(Pattern.compile("( elles)+"), " elles"), new Pair<>(Pattern.compile("( ils)+"), " ils"),
				new Pair<>(Pattern.compile("( du)+"), " du"), new Pair<>(Pattern.compile("( de)+"), " de"),
				new Pair<>(Pattern.compile("( un)+"), " un"), new Pair<>(Pattern.compile("( une)+"), " une"),
				new Pair<>(Pattern.compile("( à)+"), " à"), new Pair<>(Pattern.compile("( et)+"), " et"),
				new Pair<>(Pattern.compile("( dans)+"), " dans"), new Pair<>(Pattern.compile("( mais)+"), " mais"),

				// frequent spell-checking errors
				new Pair<>(Pattern.compile(" entrain "), " en train "),
				new Pair<>(Pattern.compile(" pace "), " parce "), new Pair<>(Pattern.compile(" f' sait "), " faisait "),
				new Pair<>(Pattern.compile(" r' sortait "), " resortait "),
				new Pair<>(Pattern.compile(" d' mander "), " demander "),
				new Pair<>(Pattern.compile(" d' vant "), " devant "),
				new Pair<>(Pattern.compile(" d' venait "), " devenait "),

				// eliminate text within ()
				new Pair<>(Pattern.compile("\\(indicible\\)"), " "),
				new Pair<>(Pattern.compile("\\(d' accord\\)"), " "), new Pair<>(Pattern.compile("\\(.*\\)"), ""),

				// eliminate surplus spaces
				new Pair<>(Pattern.compile("\\s+"), " ") };

		for (Pair<Pattern, String> p : cleaning) {
			result = p.first.matcher(result).replaceAll(p.second);
		}

		for (Pair<Pattern, String> p : FINAL_PATTERNS) {
			result = p.first.matcher(result).replaceAll(p.second);
		}

		for (String pattern : PatternMatching.PATTERNS_CAUSALITY_FR.getWords()) {
			result = result.replaceAll("( " + pattern + ")+", " " + pattern);
		}
		for (String pattern : PatternMatching.PATTERNS_METACOGNITION_FR.getWords()) {
			result = result.replaceAll("( " + pattern + ")+", " " + pattern);
		}

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
						if (pathname.getName().endsWith(".xml")) {
							return true;
						}
						return false;
					}
				});
				for (File f : files) {
					try {
						InputSource input = new InputSource(new FileInputStream(f));
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
								contents = el.getFirstChild().getNodeValue().trim();
								System.out.println(contents + "\n---------------\n"
										+ cleanText(doubleCleanVerbalization(contents), Lang.fr) + "\n");
							}
						}
					} catch (Exception e) {
						logger.error("Error processing " + f.getName() + ": " + e.getMessage());
					}
				}
			}

		} catch (Exception e) {
			System.err.print("Error evaluating input directory " + pathToDirectory + "!");
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();
		System.out.println(cleanVerbalisation(
				"alors parce que i mangent i zarrete devant alors e alors la pace qu pusqu' télé et iii .. i et  ii  puis???"));
		// displayCleaningResults("in/Matilda/MATILDA_CE2/parts");
	}
}
