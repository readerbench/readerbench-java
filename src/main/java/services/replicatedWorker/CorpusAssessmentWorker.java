/* 
 * Copyright 2016 ReaderBench.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package services.replicatedWorker;

import java.io.File;
import java.io.Serializable;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import data.cscl.Conversation;
import services.semanticModels.LDA.LDA;
import services.semanticModels.LSA.LSA;

public class CorpusAssessmentWorker extends Worker {
	static Logger logger = Logger.getLogger(CorpusAssessmentWorker.class);
	private LSA lsa;
	private LDA lda;

	public CorpusAssessmentWorker() {
		// load also LSA vector space and LDA model
		lsa = LSA.loadLSA(CorpusAssessmentMaster.PATH_TO_LSA, CorpusAssessmentMaster.PROCESSING_LANGUAGE);
		lda = LDA.loadLDA(CorpusAssessmentMaster.PATH_TO_LDA, CorpusAssessmentMaster.PROCESSING_LANGUAGE);
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
			send(new StatusMsg(workerID, StatusMsg.STARTING_MESSAGE, new String[] { taskMsg.getArgs()[0].toString() },
					null));

			Conversation d = Conversation.load(new File(taskMsg.getArgs()[0].toString()), lsa, lda,
					CorpusAssessmentMaster.PROCESSING_LANGUAGE, CorpusAssessmentMaster.USE_POS_TAGGING,
					CorpusAssessmentMaster.CLEAN_INPUT);
			if (CorpusAssessmentMaster.PROCESS_INPUT) {
				d.computeAll(CorpusAssessmentMaster.COMPUTE_DIALOGISM, CorpusAssessmentMaster.PATH_TO_COMPLEXITY_MODEL,
						CorpusAssessmentMaster.SELECTED_COMPLEXITY_FACTORS, CorpusAssessmentMaster.SAVE_OUTPUT);
			}

			send(new StatusMsg(workerID, StatusMsg.FINISHED_MESSAGE, new String[] { taskMsg.getArgs()[0].toString() },
					null));
		} catch (Exception e) {
			e.printStackTrace();
			send(new StatusMsg(workerID, StatusMsg.FINISHED_MESSAGE, new String[] { taskMsg.getArgs()[0].toString() },
					null));
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
