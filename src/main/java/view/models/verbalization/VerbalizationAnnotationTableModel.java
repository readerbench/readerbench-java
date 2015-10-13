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