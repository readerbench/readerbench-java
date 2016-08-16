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

public class ResultNode implements Comparable<ResultNode> {

	private int id;
	private String name;
	private double value;
	private int group;

	public ResultNode(int id, String name, double value, int group) {
		super();
		this.id = id;
		this.name = name;
		this.value = value;
		this.group = group;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getValue() {
		return value;
	}

	public double getGroup() {
		return group;
	}

	@Override
	public int compareTo(ResultNode o) {
		return (int) Math.signum(o.getValue() - this.getValue());
	}
}
