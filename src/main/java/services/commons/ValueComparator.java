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
package services.commons;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

public class ValueComparator<P> implements Comparator<P>, Serializable {

	private static final long serialVersionUID = 8174264967918565044L;

	Map<P, Double> base;

	public ValueComparator(Map<P, Double> base) {
		this.base = base;
	}

	public int compare(P a, P b) {
		return -((Double) base.get(a)).compareTo((Double) base.get(b));
	}
}
