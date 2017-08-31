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

import java.util.List;

public class ResultCvOrCover {
    
    private ResultTopic concepts;
	private List<ResultSentiment> sentiments;

	public ResultTopic getConcepts() {
		return concepts;
	}
	public void setConcepts(ResultTopic concepts) {
		this.concepts = concepts;
	}
	public List<ResultSentiment> getSentiments() {
		return sentiments;
	}
	public void setSentiments(List<ResultSentiment> sentiments) {
		this.sentiments = sentiments;
	}
	public ResultCvOrCover(ResultTopic concepts, List<ResultSentiment> sentiments) {
		super();
		this.concepts = concepts;
		this.sentiments = sentiments;
	}
		
}
