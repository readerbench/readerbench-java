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

    private final String untModLes;
    private final String title;
    private final String uri;
    private final Integer time;
    private final Double similarityScore;
	private final Set<String> pre;
    private final Set<String> post;

	public ResultEneaLesson(LessonDescriptives ld, String title, String uri, Integer time, Double similarityScore, Set<LessonDescriptives> pre, Set<LessonDescriptives> post) {
        this.untModLes = ld.toString().trim();
        this.title = title;
		this.uri = uri;
        this.time = time;
        this.similarityScore = similarityScore;
        this.pre = new HashSet<>();
        for (LessonDescriptives p : pre) {
            String s = p.toString().trim();
            if (s.compareTo("0.0.0") != 0) this.pre.add(p.toString().trim());
        }
        this.post = new HashSet<>();
        for (LessonDescriptives p : post) {
            String s = p.toString().trim();
            if (s.compareTo("0.0.0") != 0) this.post.add(p.toString().trim());
        }
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
        return pre;
    }

    public Set<String> getPostrequisites() {
        return post;
    }
    
}