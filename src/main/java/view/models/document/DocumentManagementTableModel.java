package view.models.document;

import javax.swing.table.DefaultTableModel;

public class DocumentManagementTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 1730686324860907760L;

	private Class<?>[] columnTypes = new Class[] { String.class, // name
			String.class, // Author
			String.class, // LSA space
			String.class, // LDA model
			Boolean.class // is chat?
	};

	public DocumentManagementTableModel() {
		super(new Object[][] {}, new String[] { "Title", "Author",
				"LSA vector space", "LDA model", "Is chat discussion?" });
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