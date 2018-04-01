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

import javax.swing.table.DefaultTableModel;

public class TopicsTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 1730686324860907760L;

	private Class<?>[] columnTypes = new Class[] { String.class, // topic
			Double.class, // relevance
	};

	public TopicsTableModel() {
		super(new Object[][] {}, new String[] { "Topics", "Relevance" });
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