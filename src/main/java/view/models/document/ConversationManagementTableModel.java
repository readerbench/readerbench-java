package view.models.document;

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
				LocalizationUtils.getTranslation("Title"),
				LocalizationUtils.getTranslation("LSA vector space"), LocalizationUtils.getTranslation("LDA model") });
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