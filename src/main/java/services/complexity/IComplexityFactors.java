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
package services.complexity;

import java.util.ResourceBundle;

import data.AbstractDocument;

/**
 * @author Mihai Dascalu
 * 
 */
public abstract class IComplexityFactors {
	public abstract String getClassName();
	
	public abstract void setComplexityIndexDescription(String[] descriptions);
	public abstract void setComplexityIndexAcronym(String[] acronyms);
	
	protected String getComplexityIndexAcronym(String indexName) {
		String text = ResourceBundle.getBundle("services.complexity.index_acronyms").getString(indexName);
		if(text == null || text.length() == 0) {
			return indexName;
		}
		return text;
	}
	
	public abstract int[] getIDs();

	public abstract void computeComplexityFactors(AbstractDocument d);
}
