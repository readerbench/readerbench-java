package view.models.document;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import view.models.WrappedTextCellRenderer;

public class ChatTable extends JTable {

	private static final long serialVersionUID = -1761389342061072875L;

	private WrappedTextCellRenderer renderer = new WrappedTextCellRenderer();

	public ChatTable(DefaultTableModel modelContent) {
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
			if (Index_col == 1) {
				WrappedTextCellRenderer.updateBackgroundColor(comp,
						UIManager.getColor("Table[Enabled+Selected].textBackground"));
				comp.setForeground(Color.WHITE);
			} else {
				comp.setBackground(UIManager.getColor("Table[Enabled+Selected].textBackground"));
				comp.setForeground(Color.WHITE);
			}
		} else {
			if (Index_row % 2 == 1) {
				if (Index_col == 1) {
					WrappedTextCellRenderer.updateBackgroundColor(comp, UIManager.getColor("Table.alternateRowColor"));
				} else {
					comp.setBackground(UIManager.getColor("Table.alternateRowColor"));
				}
			} else {
				if (Index_col == 1) {
					WrappedTextCellRenderer.updateBackgroundColor(comp, Color.WHITE);
				} else {
					comp.setBackground(Color.WHITE);
				}
			}
			comp.setForeground(UIManager.getColor("Table.textForeground"));
		}
		return comp;
	}
}
