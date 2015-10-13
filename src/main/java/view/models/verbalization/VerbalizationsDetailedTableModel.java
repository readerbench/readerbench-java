package view.models.verbalization;

import javax.swing.table.DefaultTableModel;

public class VerbalizationsDetailedTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 5068916492406925880L;

	private Class<?>[] columnTypes = new Class[] { String.class, // Verbalization
			// author
			String.class, // Verbalization ID
			String.class, // Paraphrasing Annotated / Automatic
			String.class, // Causality Annotated / Automatic
			String.class, // Bridging Annotated / Automatic
			String.class, // Text-based Inferences Annotated / Automatic
			String.class, // KI Annotated / Automatic
			String.class, // Control Annotated / Automatic
	};

	public VerbalizationsDetailedTableModel() {
		super(new Object[][] {}, new String[] { "Author", "Verbalization ID",
				"Paraphrasing", "Causality", "Bridging",
				"Text-based Inferences", "Knowledge-based Inferences",
				"Control" });
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