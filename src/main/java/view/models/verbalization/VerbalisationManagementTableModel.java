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

import utils.localization.LocalizationUtils;

public class VerbalisationManagementTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 3089645556989916569L;

	private Class<?>[] columnTypes = new Class[] { String.class, // author
			String.class, // document name
			String.class, // LSA space
			String.class // LDA model
	};

	public VerbalisationManagementTableModel() {
		super(new Object[][] {}, new String[] { LocalizationUtils.getTranslation("Author"), 
				LocalizationUtils.getTranslation("Document name"),
				LocalizationUtils.getTranslation("LSA vector space"), 
				LocalizationUtils.getTranslation("LDA model") });
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