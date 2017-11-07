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

import webService.enea.LessonDescriptives;

public class ResultEneaLesson {

    private final String untModLes;
    private final String title;
    private final String uri;
    private final Integer time;
    private final Double similarityScore;
	private final String pre;
    private final String post;

	public ResultEneaLesson(LessonDescriptives ld, String title, String uri, Integer time, Double similarityScore, LessonDescriptives pre, LessonDescriptives post) {
        this.untModLes = ld.toString();
        this.title = title;
		this.uri = uri;
        this.time = time;
        this.similarityScore = similarityScore;
        this.pre = pre.toString();
        this.post = post.toString();
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

    public String getPrerequisites() {
        return pre;
    }

    public String getPostrequisites() {
        return post;
    }
    
}