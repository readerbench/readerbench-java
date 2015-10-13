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