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