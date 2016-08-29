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

import java.util.Comparator;

public class ResultCategory implements Comparable<ResultCategory> {

	private String name;
	private double relevance;
    private Integer type;

	public ResultCategory(String name, double relevance, Integer type) {
		this.name = name;
		this.relevance = relevance;
        this.type = type;
	}

	public String getName() {
		return name;
	}

	public double getRelevance() {
		return relevance;
	}
    
    public Integer getType() {
        return type;
    }

	@Override
	public int compareTo(ResultCategory o) {
		// Reverse order
		return (int) Math.signum(this.getRelevance() - o.getRelevance());
	}

	public static Comparator<ResultCategory> ResultCategoryRelevanceComparator = new Comparator<ResultCategory>() {

		public int compare(ResultCategory o1, ResultCategory o2) {
			// descending order
			return o2.compareTo(o1);
		}

	};

}
