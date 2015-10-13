package view.models.verbalization;

import javax.swing.table.DefaultTableModel;

public class VerbalizationTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 1730686324860907760L;

	private Class<?>[] columnTypes = new Class[] { String.class, // text
			Integer.class, // No Paraphrasing
			Integer.class, // No Causality
			Integer.class, // No Bridging
			Integer.class, // No Text-based Inferences
			Integer.class, // No Control
			Integer.class, // No KI
			String.class, // Cohesion
	};

	public VerbalizationTableModel() {
		super(new Object[][] {}, new String[] { "Text", "Paraphrasing",
				"Causality", "Bridging", "Text-based Inferences",
				"Knowledge Inferences", "Control", "Cohesion" });
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