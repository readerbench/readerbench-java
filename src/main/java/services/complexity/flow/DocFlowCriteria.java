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
package services.complexity.flow;

public enum DocFlowCriteria {
	MAX_VALUE, ABOVE_MEAN_PLUS_STDEV;

	@Override
	public String toString() {
		switch (this) {
		case MAX_VALUE:
			return "Maximum value";
		case ABOVE_MEAN_PLUS_STDEV:
			return "Above mean+stdev";
		default:
			throw new IllegalArgumentException();
		}
	}

	public String getAcronym() {
		switch (this) {
		case MAX_VALUE:
			return "MaxVal";
		case ABOVE_MEAN_PLUS_STDEV:
			return "AbvMeanStdev";
		default:
			throw new IllegalArgumentException();
		}
	}
};