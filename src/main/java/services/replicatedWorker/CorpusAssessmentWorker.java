package services.replicatedWorker;

import java.io.File;
import java.io.Serializable;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;
import DAO.chat.Chat;

public class CorpusAssessmentWorker extends Worker {
	static Logger logger = Logger.getLogger(CorpusAssessmentWorker.class);
	private LSA lsa;
	private LDA lda;

	public CorpusAssessmentWorker() {
		// load also LSA vector space and LDA model
		lsa = LSA.loadLSA(CorpusAssessmentMaster.PATH_TO_LSA,
				CorpusAssessmentMaster.PROCESSING_LANGUAGE);
		lda = LDA.loadLDA(CorpusAssessmentMaster.PATH_TO_LDA,
				CorpusAssessmentMaster.PROCESSING_LANGUAGE);
	}

	public void performTask(Serializable task) throws Exception {
		TaskMsg taskMsg = (TaskMsg) task;
		if (taskMsg.isFinish()) {
			logger.info(workerID + " finished working. Good bye");
			connection.close();
			System.exit(-1);
		}

		// Map<String, Integer> wordOccurences = new TreeMap<String, Integer>();
		try {
			send(new StatusMsg(workerID, StatusMsg.STARTING_MESSAGE,
					new String[] { taskMsg.getArgs()[0].toString() }, null));

			Chat d = Chat.load(
					new File(taskMsg.getArgs()[0].toString()), lsa, lda,
					CorpusAssessmentMaster.PROCESSING_LANGUAGE,
					CorpusAssessmentMaster.USE_POS_TAGGING,
					CorpusAssessmentMaster.CLEAN_INPUT);
			if (CorpusAssessmentMaster.PROCESS_INPUT) {
				d.computeAll(CorpusAssessmentMaster.PATH_TO_COMPLEXITY_MODEL,
						CorpusAssessmentMaster.SELECTED_COMPLEXITY_FACTORS,
						CorpusAssessmentMaster.SAVE_OUTPUT);
			}

			send(new StatusMsg(workerID, StatusMsg.FINISHED_MESSAGE,
					new String[] { taskMsg.getArgs()[0].toString() }, null));
		} catch (Exception e) {
			e.printStackTrace();
			send(new StatusMsg(workerID, StatusMsg.FINISHED_MESSAGE,
					new String[] { taskMsg.getArgs()[0].toString() }, null));
		}

		logger.info("Finished analysing " + taskMsg.getArgs()[0].toString());
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.INFO);

		Worker consumerTool = new CorpusAssessmentWorker();
		consumerTool.run();
	}

}
