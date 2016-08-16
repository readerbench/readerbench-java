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
package services.semanticModels.word2vec;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

public class TrainAoEWord2VecModels {

	public static void trainModels(String path) throws IOException {
		// determine number of classes
		int noGrades = (new File(path)).listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				if (pathname.isDirectory())
					return true;
				return false;
			}
		}).length;

		for (int i = 0; i < noGrades; i++) {
			String classPath = path + "/grade" + i;
			Word2VecModel word2Vec = new Word2VecModel();
			word2Vec.processCorpus(classPath);
		}
	}

	public static void main(String[] args) {
		try {
			TrainAoEWord2VecModels.trainModels("resources/in/AoE 100");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}