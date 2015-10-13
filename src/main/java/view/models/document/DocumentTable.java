package view.models.document;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import view.models.WrappedTextCellRenderer;

public class DocumentTable extends JTable {

	private static final long serialVersionUID = -7068592090884255889L;

	private WrappedTextCellRenderer renderer = new WrappedTextCellRenderer();

	public DocumentTable(DefaultTableModel modelContent) {
		super(modelContent);
	}

	public TableCellRenderer getCellRenderer(int row, int column) {
		if (column == 1) {
			return renderer;
		}
		return super.getCellRenderer(row, column);
	}

	public Component prepareRenderer(TableCellRenderer renderer, int Index_row, int Index_col) {
		Component comp = super.prepareRenderer(renderer, Index_row, Index_col);
		// even index, selected or not selected
		if (isCellSelected(Index_row, Index_col)) {
			if (Index_row % 2 == 0 && Index_col == 1) {
				WrappedTextCellRenderer.updateBackgroundColor(comp,
						UIManager.getColor("Table[Enabled+Selected].textBackground"));
			} else {
				comp.setBackground(UIManager.getColor("Table[Enabled+Selected].textBackground"));
			}
			comp.setForeground(Color.WHITE);
		} else {
			if (Index_row % 2 == 1) {
				comp.setBackground(UIManager.getColor("Table.alternateRowColor"));
				comp.setForeground(Color.RED);
			} else {
				if (Index_col == 1) {
					WrappedTextCellRenderer.updateBackgroundColor(comp, Color.WHITE);
					comp.setForeground(UIManager.getColor("Table.textForeground"));
				} else {
					comp.setBackground(Color.WHITE);
					comp.setForeground(UIManager.getColor("Table.textForeground"));
				}
			}
		}
		return comp;
	}
}
