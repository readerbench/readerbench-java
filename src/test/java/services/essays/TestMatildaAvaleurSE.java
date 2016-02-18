package services.essays;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import data.document.Document;
import data.document.Metacognition;
import edu.cmu.lti.jawjaw.pobj.Lang;
import services.complexity.ComplexityIndices;
import services.discourse.selfExplanations.VerbalizationAssessment;
import services.readingStrategies.ReadingStrategies;
import view.widgets.selfexplanation.ReadingStrategiesIndicesView;
import webService.ReaderBenchServer;

public class TestMatildaAvaleurSE {
	static Logger logger = Logger.getLogger(TestMatildaAvaleurSE.class);

	public static List<Metacognition> compute(String filename, String folder) {
		List<Metacognition> verbalizations = new ArrayList<Metacognition>();
		Document doc = Document.load(filename, "resources/config/LSA/lemonde_fr", "resources/config/LDA/lemonde_fr",
				Lang.fr, true, true);
		File verbFolder = new File(folder);
		for (File f : verbFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
				// return name.endsWith(".ser");
			}
		})) {
			Metacognition v = Metacognition.loadVerbalization(f.getAbsolutePath(), doc, true, true);
			// Metacognition v = (Metacognition)
			// Metacognition.loadSerializedDocument(f.getAbsolutePath());
			v.computeAll(true, true);
			verbalizations.add(v);
		}
		return verbalizations;
	}

	public static List<Metacognition> load(String folder) {
		List<Metacognition> verbalizations = new ArrayList<Metacognition>();
		File verbFolder = new File(folder);
		for (File f : verbFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".ser");
			}
		})) {
			Metacognition v = (Metacognition) Metacognition.loadSerializedDocument(f.getAbsolutePath());
			VerbalizationAssessment.detRefBlockSimilarities(v);
			ReadingStrategies.detReadingStrategies(v);

			ComplexityIndices.computeComplexityFactors(v);
			v.determineComprehesionIndices();
			verbalizations.add(v);
		}
		return verbalizations;
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.INFO);
		ReaderBenchServer.initializeDB();

		try {
			String folder = "resources/in/Matilda & Avaleur";

			List<Metacognition> verbalizations = new ArrayList<Metacognition>();
			BufferedWriter out = new BufferedWriter(new FileWriter(folder + "/output.csv"));

			out.write("Filename,Comprehension score,Comprehension class,Fluency");
			for (String s : ReadingStrategies.STRATEGY_NAMES)
				out.write(",Annotated " + s);
			for (String s : ComplexityIndices.TEXTUAL_COMPLEXITY_INDEX_ACRONYMS)
				out.write("," + s);
			for (String s : ReadingStrategiesIndicesView.READING_STRATEGY_INDEX_NAMES)
				out.write(",Automated " + s);

			// verbalizations.addAll(compute(folder + "/Matilda.xml",folder +
			// "/Matilda verbalizations"));
			// verbalizations.addAll(compute(folder + "/L'avaleur de
			// nuages.xml",folder + "/Avaleur verbalizations"));

			verbalizations.addAll(load(folder + "/Matilda verbalizations"));
			verbalizations.addAll(load(folder + "/Avaleur verbalizations"));

			for (Metacognition v : verbalizations) {
				out.write("\n" + (new File(v.getPath()).getName()) + "," + v.getAnnotatedComprehensionScore() + ","
						+ v.getComprehensionClass() + "," + v.getAnnotatedFluency());
				for (double value : v.getAnnotatedStrategies())
					out.write("," + value);
				for (double value : v.getComprehensionIndices())
					out.write("," + value);
			}

			out.close();
			logger.info("Finished all files for processing!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
