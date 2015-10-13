package view.models.complexity;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class HeaderRenderer extends JCheckBox implements TableCellRenderer {
	private static final long serialVersionUID = 7110716111560547597L;

	private final JLabel label = new JLabel();
	private int targetColumnIndex;
	private boolean[] selected;
	private boolean[] isEditable;

	public HeaderRenderer(JTableHeader header, int index, boolean[] selected,
			boolean[] isEditable) {
		super((String) null);
		this.targetColumnIndex = index;
		this.selected = selected;
		this.isEditable = isEditable;
		setOpaque(false);
		setFont(header.getFont());
		header.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JTableHeader header = (JTableHeader) e.getSource();
				JTable table = header.getTable();
				TableColumnModel columnModel = table.getColumnModel();
				int vci = columnModel.getColumnIndexAtX(e.getX());
				int mci = table.convertColumnIndexToModel(vci);
				if (mci == targetColumnIndex) {
					TableColumn column = columnModel.getColumn(vci);
					Object v = column.getHeaderValue();
					boolean b = Status.DESELECTED.equals(v) ? true : false;
					TableModel m = table.getModel();
					for (int i = 0; i < m.getRowCount(); i++) {
						int index = (Integer) m.getValueAt(i, 0);
						if (HeaderRenderer.this.isEditable[index]) {
							m.setValueAt(b, i, mci);
							HeaderRenderer.this.selected[index] = b;
						}
					}
					column.setHeaderValue(b ? Status.SELECTED
							: Status.DESELECTED);
				}
			}
		});
	}

	@Override
	public Component getTableCellRendererComponent(JTable tbl, Object val,
			boolean isS, boolean hasF, int row, int col) {
		TableCellRenderer r = tbl.getTableHeader().getDefaultRenderer();
		JLabel l = (JLabel) r.getTableCellRendererComponent(tbl, val, isS,
				hasF, row, col);
		if (targetColumnIndex == tbl.convertColumnIndexToModel(col)) {
			if (val instanceof Status) {
				switch ((Status) val) {
				case SELECTED:
					setSelected(true);
					setEnabled(true);
					break;
				case DESELECTED:
					setSelected(false);
					setEnabled(true);
					break;
				case INDETERMINATE:
					setSelected(true);
					setEnabled(false);
					break;
				}
			} else {
				setSelected(true);
				setEnabled(false);
			}
			label.setIcon(new ComponentIcon(this));
			l.setIcon(new ComponentIcon(label));
			l.setText(null);
			l.setHorizontalAlignment(SwingConstants.CENTER);
		} else {
			l.setHorizontalAlignment(SwingConstants.LEFT);
		}

		return l;
	}

	@Override
	public void updateUI() {
		setText(null);
		super.updateUI();
	}

}
