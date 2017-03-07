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
package webService.result;

public class ResultValence implements Comparable<ResultValence> {

	private String valence;
	private final double score;

	public ResultValence(String content, double score) {
		super();
		this.valence = content;
		this.score = score;
	}

	public String getValence() {
		return valence;
	}

	public void setValence(String valence) {
		this.valence = valence;
	}

	public double getScore() {
		return score;
	}

	@Override
	public int compareTo(ResultValence o) {
		return (int) Math.signum(o.getScore() - this.getScore());
	}
}
