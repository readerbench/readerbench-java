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

public class ResultEneaCustomisation {

	private final List<ResultEneaLesson> lessons;
    private final List<String> recommended;
    private final Integer time;
    private final Double cmePoints;

	public ResultEneaCustomisation(List<ResultEneaLesson> lessons, List<String> recommended, Integer time, Double cmePoints) {
		this.lessons = lessons;
        this.recommended = recommended;
        this.time = time;
        this.cmePoints = cmePoints;
	}

	public List<ResultEneaLesson> getLessons() {
		return lessons;
	}

    public List<String> getRecommended() {
        return recommended;
    }

    public Integer getTime() {
        return time;
    }

    public Double getCmePoints() {
        return cmePoints;
    }
        
}