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
package data.cscl;

import java.util.ResourceBundle;

public enum CSCLIndices {
	NO_CONTRIBUTION(true),
	OVERALL_SCORE(true),
	PERSONAL_KB(false),
	SOCIAL_KB(true),
	INTER_ANIMATION_DEGREE(false),
	INDEGREE(true),
	OUTDEGREE(true),
	BETWEENNESS(true),
	CLOSENESS(true),
	ECCENTRICITY(true),
	RELEVANCE_TOP10_TOPICS(true),
	NO_NOUNS(false),
	NO_VERBS(false),
	NO_NEW_THREADS(false),
	AVERAGE_LENGTH_NEW_THREADS(false),
	NEW_THREADS_OVERALL_SCORE(false),
	NEW_THREADS_INTER_ANIMATION_DEGREE(false),
	NEW_THREADS_CUMULATIVE_SOCIAL_KB(false);

	private final boolean isUsedForTimeModeling;

	private CSCLIndices(boolean isUsedForTimeModeling) {
		this.isUsedForTimeModeling = isUsedForTimeModeling;
	}

	public boolean isUsedForTimeModeling() {
		return isUsedForTimeModeling;
	}

	public String getDescription() {
		return ResourceBundle.getBundle("utils.localization.CSCL_indices_descr").getString(this.name());
	}

	public String getAcronym() {
		return ResourceBundle.getBundle("utils.localization.CSCL_indices_acro").getString(this.name());
	}
}
