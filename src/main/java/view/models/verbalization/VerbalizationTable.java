package view.models.verbalization;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import view.models.WrappedTextCellRenderer;

public class VerbalizationTable extends JTable {

	private static final long serialVersionUID = -1761389342061072875L;

	private WrappedTextCellRenderer wrappedTextRenderer = new WrappedTextCellRenderer();
	private boolean[] isVerbalisation;

	public VerbalizationTable(DefaultTableModel modelContent,
			boolean[] isVerbalisation) {
		super(modelContent);
		this.isVerbalisation = isVerbalisation;
	}

	public TableCellRenderer getCellRenderer(int row, int column) {
		if (column == 0) {
			return wrappedTextRenderer;
		}
		return super.getCellRenderer(row, column);
	}

	public Component prepareRenderer(TableCellRenderer renderer, int Index_row,
			int Index_col) {
		Component comp = super.prepareRenderer(renderer, Index_row, Index_col);
		// even index, selected or not selected
		if (isCellSelected(Index_row, Index_col)) {
			if (Index_col == 0) {
				WrappedTextCellRenderer.updateBackgroundColor(comp, UIManager
						.getColor("Table[Enabled+Selected].textBackground"));
			} else {
				comp.setBackground(UIManager
						.getColor("Table[Enabled+Selected].textBackground"));
			}
			comp.setForeground(Color.WHITE);
		} else {
			if (Index_col == 0) {
				if (isVerbalisation[Index_row]) {
					WrappedTextCellRenderer.updateBackgroundColor(comp,
							Color.LIGHT_GRAY);
				} else {
					WrappedTextCellRenderer.updateBackgroundColor(comp,
							Color.WHITE);
				}
			} else {
				if (isVerbalisation[Index_row]) {
					comp.setBackground(Color.LIGHT_GRAY);
				} else {
					comp.setBackground(Color.WHITE);
				}
			}
			comp.setForeground(UIManager.getColor("Table.textForeground"));
		}
		return comp;
	}
}
