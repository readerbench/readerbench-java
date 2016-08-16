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

public class ResultEdge implements Comparable<ResultEdge> {

	private String label;
	private int source;
	private int target;
	private double score;

	public ResultEdge(String label, int source, int target, double score) {
		super();
		this.label = label;
		this.source = source;
		this.target = target;
		this.score = score;
	}
	
	public String getLabel() {
		return label;
	}

	public int getSource() {
		return source;
	}

	public int getTarget() {
		return target;
	}

	public double getScore() {
		return score;
	}

	@Override
	public int compareTo(ResultEdge o) {
		return (int) Math.signum(o.getScore() - this.getScore());
	}
}
