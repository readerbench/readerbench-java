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
package view.models.verbalization;

import javax.swing.table.DefaultTableModel;

public class VerbalizationsCumulativeTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 5068916492406925880L;

	private Class<?>[] columnTypes = new Class[] { String.class, // Verbalization
			// author
			String.class, // Paraphrasing Annotated / Automatic
			String.class, // Causality Annotated / Automatic
			String.class, // Bridging Annotated / Automatic
			String.class, // Text-based Inferences Annotated / Automatic
			String.class, // Control Annotated / Automatic
			String.class, // KI Annotated / Automatic
			String.class, // Comprehension score
			String.class, // Comprehension class
			String.class // Fluency
	};

	public VerbalizationsCumulativeTableModel() {
		super(new Object[][] {}, new String[] { "Author", "Paraphrasing",
				"Causality", "Bridging", "Text-based Inferences",
				"Knowledge-based Inferences", "Control", "Comprehension score",
				"Comprehension class", "Fluency" });
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return columnTypes[columnIndex];
	}

}