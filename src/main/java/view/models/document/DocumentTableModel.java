package view.models.document;

import javax.swing.table.DefaultTableModel;

public class DocumentTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 1730686324860907760L;

	private Class<?>[] columnTypes = new Class[] { String.class, // ID
			String.class // content
	};

	public DocumentTableModel() {
		super(new Object[][] {}, new String[] { "ID", "Text" });
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