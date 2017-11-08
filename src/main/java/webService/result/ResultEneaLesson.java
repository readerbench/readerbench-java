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

import java.util.HashSet;
import java.util.Set;
import webService.enea.LessonDescriptives;

public class ResultEneaLesson {

    private final String id;
    private final String title;
    private final String uri;
    private final Integer time;
    private final Double similarityScore;
	private final Set<String> prerequisites;
    private final Set<String> postrequisites;

	public ResultEneaLesson(LessonDescriptives ld, String title, String uri, Integer time, Double similarityScore, Set<LessonDescriptives> prerequisites, Set<LessonDescriptives> postrequisites) {
        this.id = ld.toString().trim();
        this.title = title;
		this.uri = uri;
        this.time = time;
        this.similarityScore = similarityScore;
        this.prerequisites = new HashSet<>();
        for (LessonDescriptives p : prerequisites) {
            String s = p.toString().trim();
            if (s.compareTo("0.0.0") != 0) this.prerequisites.add(p.toString().trim());
        }
        this.postrequisites = new HashSet<>();
        for (LessonDescriptives p : postrequisites) {
            String s = p.toString().trim();
            if (s.compareTo("0.0.0") != 0) this.postrequisites.add(p.toString().trim());
        }
	}

    public String getId() {
        return id;
    }
    
    public String getTitle() {
        return title;
    }

    public String getUri() {
        return uri;
    }

    public Integer getTime() {
        return time;
    }
    
    public Double getSimilarityScore() {
        return similarityScore;
    }

    public Set<String> getPrerequisites() {
        return prerequisites;
    }

    public Set<String> getPostrequisites() {
        return postrequisites;
    }
    
}