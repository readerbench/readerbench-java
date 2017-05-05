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
package webService.queryResult;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;
import webService.result.ResultJobQuest;

@Root(name = "response")
public class QueryResultJobQuest extends QueryResult {

    @Path("data")
    @ElementList(inline = true, entry = "result")
    private ResultJobQuest data; // list of result sentiments

    public ResultJobQuest getData() {
        return data;
    }

    public void setData(ResultJobQuest data) {
        this.data = data;
    }

    public QueryResultJobQuest() {
        super();
        data = new ResultJobQuest();
    }

}