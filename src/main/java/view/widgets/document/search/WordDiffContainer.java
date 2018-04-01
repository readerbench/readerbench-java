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
package view.widgets.document.search;

import java.util.List;

import data.Word;

public class WordDiffContainer implements Comparable<WordDiffContainer> {
	private Word wRef;
	private Word wSim;
	private double similarity;

	public WordDiffContainer(Word wRef, Word wSim, double similarity) {
		this.wRef = wRef;
		this.wSim = wSim;
		this.similarity = similarity;
	}

	public static double getScore(List<WordDiffContainer> l, Word wRef, Word wSim) {
		for (WordDiffContainer c : l) {
			if (c.getWRef().equals(wRef) && c.getWSim().equals(wSim))
				return c.getSimilarity();
		}
		return 0.0;
	}

	@Override
	public int compareTo(WordDiffContainer o) {
		return new Double(o.getSimilarity()).compareTo(new Double(this.getSimilarity()));
	}

	public Word getWRef() {
		return wRef;
	}

	public void setWRef(Word wRef) {
		this.wRef = wRef;
	}

	public Word getWSim() {
		return wSim;
	}

	public void setWSim(Word wSim) {
		this.wSim = wSim;
	}

	public double getSimilarity() {
		return similarity;
	}

	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}

}