package services.ageOfExposure;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import services.converters.GenericTasaDocument;
import services.converters.SplitTASA;

public class SplitTASAincrementalFiles {
	static Logger logger = Logger.getLogger(SplitTASAincrementalFiles.class);

	public static void parseTasaFromSingleFile(String input, String path, boolean usePOStagging,
			boolean annotateWithPOS) throws FileNotFoundException, IOException {
		createFolders(path);
		List<GenericTasaDocument> docs = SplitTASA.getTASAdocs(input, path);

		StringBuilder[] outputs = new StringBuilder[SplitTASA.NO_GRADE_LEVELS];
		for (int i = 0; i < SplitTASA.NO_GRADE_LEVELS; i++) {
			outputs[i] = new StringBuilder();
		}

		int complexityClass;
		StringBuilder content;

		for (GenericTasaDocument doc : docs) {
			complexityClass = GenericTasaDocument.get13GradeLevel(doc.getDRPscore());
			if (complexityClass >= 1 && complexityClass <= SplitTASA.NO_GRADE_LEVELS) {
				content = doc.getProcessedContent(usePOStagging, annotateWithPOS);
				for (int i = complexityClass - 1; i < SplitTASA.NO_GRADE_LEVELS; i++) {
					if (content != null)
						outputs[i].append(content + "\n");
				}
			}
		}

		// write all files
		for (int i = 0; i < SplitTASA.NO_GRADE_LEVELS; i++) {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(new File(path + "/grade" + i + "/alltexts[1-" + (i + 1) + "].txt")), "UTF-8"),
					32768);
			out.write(outputs[i].toString());
			out.close();
		}
	}

	private static void createFolders(String path) {
		// delete all potential class folders as well
		for (int i = 0; i < SplitTASA.NO_GRADE_LEVELS; i++) {
			File dir = new File(path + "/grade" + i);
			if (dir.exists()) {
				for (File f : dir.listFiles())
					f.delete();
				dir.delete();
			}
			dir.mkdir();
		}
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();

		try {
			SplitTASAincrementalFiles.parseTasaFromSingleFile("tasa.txt", "resources/in/AoE", false, false);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
