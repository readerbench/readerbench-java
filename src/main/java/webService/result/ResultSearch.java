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

public class ResultSearch implements Comparable<ResultSearch> {

	private String url;
	private String content;
	private double relevance;

	public ResultSearch(String url, String content, double relevance) {
		this.url = url;
		this.content = content;
		this.relevance = relevance;
	}

	public String getUrl() {
		return url;
	}

	public String getContent() {
		return content;
	}

	public double getRelevance() {
		return relevance;
	}

	@Override
	public int compareTo(ResultSearch o) {
		return (int) Math.signum(o.getRelevance() - this.getRelevance());
	}
}
