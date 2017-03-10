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

public class ResultSentiment {

	private String level;
	private final List<ResultValence> valences;
	private final List<ResultSentiment> children;
    private final String text;

	public ResultSentiment(String content, List<ResultValence> valences, List<ResultSentiment> children, String text) {
		super();
		this.level = content;
		this.valences = valences;
		this.children = children;
        this.text = text;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public List<ResultValence> getValences() {
		return valences;
	}

    public List<ResultSentiment> getChildren() {
        return children;
    }
    
    public String getText() {
        return text;
    }
    
}