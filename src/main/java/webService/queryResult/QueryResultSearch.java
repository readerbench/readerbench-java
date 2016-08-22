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

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import webService.result.ResultSearch;

@Root(name = "response")
public class QueryResultSearch extends QueryResult {

	@Path("data")
	@ElementList(inline = true, entry = "result")
	public List<ResultSearch> data; // list of query results (urls)

	public List<ResultSearch> getData() {
		return data;
	}

	public void setData(List<ResultSearch> data) {
		this.data = data;
	}

	public QueryResultSearch() {
		super();
		data = new ArrayList<ResultSearch>();
	}
}
