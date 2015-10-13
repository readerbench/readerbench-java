package view.models.document;

import javax.swing.table.DefaultTableModel;

public class SentenceTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 1730686324860907760L;

	private Class<?>[] columnTypes = new Class[] { Integer.class, // ID
			Integer.class, // block
			String.class, // text
			Double.class, // Coh LSA Cosine Sim
			Double.class, // Coh LDA Divergence
			Double.class, // Leacok Chodorow similarity
			Double.class, // Wu Palmer similarity
			Double.class, // Path similarity
			Double.class, // Cohesion
			Integer.class, // Cohesion progress bar
	};

	public SentenceTableModel() {
		super(new Object[][] {}, new String[] { "Sentence ID", "Block ID",
				"Text", "Cosine Sim LSA", "JSH Sim LDA", "Leacok Chodorow",
				"Wu Palmer", "Path Sim", "Cohesion", "Cohesion Bar" });
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