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
package runtime.semanticModels;



import data.Lang;
import java.util.logging.Logger;
import services.semanticModels.LDA.LDA;

public class RunHDP {
	static Logger logger = Logger.getLogger("");

	public static void main(String[] args) {

		try {
			LDA lda = new LDA(Lang.en);
			String path = "resources/in/AoE/grade" + 0;
			int noTopics = lda.createHDPModel(path, 100, 20000);
			logger.info("Inferred optimal number of topics is for " + path + " is " + noTopics);

			// lda.printTopics(path, 300);
			// Word w1 = Word.getWordFromConcept("mailman", lang);
			// Word w2 = Word.getWordFromConcept("man", lang);
			// System.out.println(lda.getSimilarity(w2, w1));
			// lda.findDeepLearningRules(w1, w2, 0.5);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.severe("Error during learning process");
		}
	}
}
