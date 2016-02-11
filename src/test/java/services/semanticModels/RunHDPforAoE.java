package services.semanticModels;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import edu.cmu.lti.jawjaw.pobj.Lang;
import services.semanticModels.LDA.LDA;

public class RunHDPforAoE {
	static Logger logger = Logger.getLogger(RunHDP.class);

	private static class RunHDPGradeLevel implements Runnable {
		private int gradeLevel;

		public RunHDPGradeLevel(int gradeLevel) {
			this.gradeLevel = gradeLevel;
		}

		@Override
		public void run() {
			try {
				LDA lda = new LDA(Lang.eng);
				String path = "resources/in/AoE/grade" + gradeLevel;
				int noTopics = lda.createHDPModel(path, 5, 200000);
				logger.info("Inferred optimal number of topics for " + path + " is " + noTopics);
			} catch (Exception e) {
				logger.error("Error processing grade level " + gradeLevel + ": " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();

		int noThreads = 5;
		ExecutorService executor = Executors.newFixedThreadPool(noThreads);

		for (int i = 0; i <= 12; i++) {
			RunHDPGradeLevel task = new RunHDPGradeLevel(i);
			executor.execute(task);
		}
		executor.shutdown();

		while (!executor.isTerminated()) {
		}
		System.out.println("Finished all threads");
	}
}
