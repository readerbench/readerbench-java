package view.models;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class ProgressRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = -8837231365025631707L;
	private final JProgressBar b = new JProgressBar(0, 100);

	public ProgressRenderer() {
		super();
		setOpaque(true);
		b.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		Integer i = (Integer) value;
		b.setValue(i);
		return b;
	}
}
