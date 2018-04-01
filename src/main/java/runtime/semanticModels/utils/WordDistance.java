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
package runtime.semanticModels.utils;

public class WordDistance implements Comparable<WordDistance>, java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String word1;
	private String word2;
	private double similarity;

	public WordDistance(String word1, String word2, double similarity) {
		super();
		this.word1 = word1;
		this.word2 = word2;
		this.similarity = similarity;
	}

	public String getWord1() {
		return word1;
	}

	public String getWord2() {
		return word2;
	}

	public double getSimilarity() {
		return similarity;
	}

	@Override
	public int compareTo(WordDistance otherDistance) {
		if(this.similarity < otherDistance.similarity) {
			return -1;
		}
		if(this.similarity > otherDistance.similarity) {
			return 1;
		}
		return 0;
	}
}