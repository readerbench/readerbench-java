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
package view.models.document;

import java.util.ResourceBundle;
import javax.swing.table.DefaultTableModel;
import utils.localization.LocalizationUtils;

public class ConversationManagementTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 1730686324860907760L;

	private Class<?>[] columnTypes = new Class[] { String.class, // name
			String.class, // LSA space
			String.class, // LDA model
	};

	public ConversationManagementTableModel() {
		super(new Object[][] {}, new String[] { 
				ResourceBundle.getBundle("utils.localization.messages")
                .getString("TableModel.Title.title"),
				ResourceBundle.getBundle("utils.localization.messages")
                .getString("TableModel.LSAspace.text"), 
				ResourceBundle.getBundle("utils.localization.messages")
                .getString("TableModel.LDAvector.text") });
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