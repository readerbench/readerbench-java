package view.models.document;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JEditorPane;
import javax.swing.JTable;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import view.models.ProgressRenderer;
import view.models.WrappedTextCellRenderer;

public class SentenceTable extends JTable {

	private static final long serialVersionUID = -1761389342061072875L;

	private WrappedTextCellRenderer wrappedTextRenderer = new WrappedTextCellRenderer();
	private ProgressRenderer progressRenderer = new ProgressRenderer();
	private int[] utteranceBlockIndex;

	public SentenceTable(DefaultTableModel modelContent,
			int[] utteranceBlockIndex) {
		super(modelContent);
		this.utteranceBlockIndex = utteranceBlockIndex;
	}

	public TableCellRenderer getCellRenderer(int row, int column) {
		if (column == 2) {
			return wrappedTextRenderer;
		} else {
			if (column == 10)
				return progressRenderer;
			else
				return super.getCellRenderer(row, column);
		}
	}

	public Component prepareRenderer(TableCellRenderer renderer, int Index_row,
			int Index_col) {
		Component comp = super.prepareRenderer(renderer, Index_row, Index_col);
		// even index, selected or not selected
		if (isCellSelected(Index_row, Index_col)) {
			if (Index_col == 2) {
				Color bgColor = UIManager
						.getColor("Table[Enabled+Selected].textBackground");
				UIDefaults defaults = new UIDefaults();
				defaults.put("EditorPane[Enabled].backgroundPainter", bgColor);
				JEditorPane component = (JEditorPane) comp;
				component.putClientProperty("Nimbus.Overrides", defaults);
				component.putClientProperty("Nimbus.Overrides.InheritDefaults",
						true);
				component.setBackground(bgColor);
				component.setForeground(Color.WHITE);
			} else {
				comp.setBackground(UIManager
						.getColor("Table[Enabled+Selected].textBackground"));
				comp.setForeground(Color.WHITE);
			}
		} else {
			int index = (Integer) this.getRowSorter().convertRowIndexToModel(
					Index_row);
			if (Index_col == 2) {
				if (utteranceBlockIndex[index] % 2 == 0) {
					WrappedTextCellRenderer.updateBackgroundColor(comp,
							Color.LIGHT_GRAY);
				} else {
					WrappedTextCellRenderer.updateBackgroundColor(comp,
							Color.WHITE);
				}
			} else {
				if (utteranceBlockIndex[index] % 2 == 0) {
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
