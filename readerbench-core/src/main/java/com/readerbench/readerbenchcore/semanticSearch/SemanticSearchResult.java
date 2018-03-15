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
package com.readerbench.readerbenchcore.semanticSearch;

import com.readerbench.data.AbstractDocument;
import java.io.Serializable;

public class SemanticSearchResult implements Comparable<SemanticSearchResult>, Serializable {
	private AbstractDocument doc;
	private double relevance;
        private static final long serialVersionUID = 2L;

	public SemanticSearchResult(AbstractDocument doc, double relevance) {
		super();
		this.doc = doc;
		this.relevance = relevance;
	}

	public AbstractDocument getDoc() {
		return doc;
	}

	public void setDoc(AbstractDocument doc) {
		this.doc = doc;
	}

	public double getRelevance() {
		return relevance;
	}

	public void setRelevance(double relevance) {
		this.relevance = relevance;
	}

	@Override
	public int compareTo(SemanticSearchResult o) {
		return (int) Math.signum(o.getRelevance() - this.getRelevance());
	}

}
