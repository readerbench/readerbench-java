package view.models.complexity;

import javax.swing.table.DefaultTableModel;

public class ComplexityFactorTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 3089645556989916569L;

	private Class<?>[] columnTypes = new Class[] { Integer.class, // identifier
			String.class, // class name
			String.class, // factor name
			Boolean.class, // selected
	};

	public ComplexityFactorTableModel() {
		super(new Object[][] {}, new String[] { "ID", "Class name",
				"Factor name", "Selected" });
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex == 3)
			return true;
		return false;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return columnTypes[columnIndex];
	}

}