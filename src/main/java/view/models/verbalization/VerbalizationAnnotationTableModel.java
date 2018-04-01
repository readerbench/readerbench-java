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

public class VerbalizationAnnotationTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 1730686324860907760L;
	private boolean[] isVerbalisation;

	private Class<?>[] columnTypes = new Class[] { String.class, // text
			Integer.class, // No Causality
			Integer.class, // No Control
			Integer.class, // No Paraphrasing
			Integer.class, // No Bridging
			Integer.class, // No Knowledge Inferred
	};

	public VerbalizationAnnotationTableModel(boolean[] isVerbalisation) {
		super(new Object[][] {}, new String[] { "Text", "Causality", "Control",
				"Paraphrasing", "Knowledge Inferred", "Bridging" });
		this.isVerbalisation = isVerbalisation;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (isVerbalisation[rowIndex])
			return true;
		return false;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return columnTypes[columnIndex];
	}

}