package services.semanticModels.LDA;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import data.Lang;

public class TrainAoELDAModels {

	public static void trainModels(String path, int noThreads, int noIterations) throws IOException {
		// determine number of classes
		int noGrades = (new File(path)).listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				if (pathname.isDirectory())
					return true;
				return false;
			}
		}).length;

		// proportional method
		// 3612 6530 9078 12022 16810 21824 23378 24912 26499 29465 32160 33409
		// 41866
		// int[] noTopics = { 5, 12, 19, 26, 38, 50, 54, 58, 62, 69, 76, 79,
		// 100 };

		// 5-topics increments
		// int[] noTopics = { 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60,
		// 100 };
		int noTopics = 100;

		for (int i = 0; i < noGrades; i++) {
			String classPath = path + "/grade" + i;
			LDA lda = new LDA(Lang.eng);
			lda.processCorpus(classPath, noTopics, noThreads, noIterations);
			lda.printTopics(classPath, 100);
		}
	}

	public static void main(String[] args) {
		try {
			TrainAoELDAModels.trainModels("resources/in/AoE 100", 8, 20000);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
