/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view.models.document;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import view.models.WrappedTextCellRenderer;

/**
 *
 * @author valentin.sergiu.cioaca@gmail.com
 */
public class CustomRhythmTable extends JTable {
    private final WrappedTextCellRenderer renderer = new WrappedTextCellRenderer();

	public CustomRhythmTable(DefaultTableModel modelContent) {
		super(modelContent);
	}

    @Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		if (column == 1) {
			return renderer;
		}
		return super.getCellRenderer(row, column);
	}

    @Override
	public Component prepareRenderer(TableCellRenderer renderer, int Index_row, int Index_col) {
		Component comp = super.prepareRenderer(renderer, Index_row, Index_col);
		// even index, selected or not selected
		if (isCellSelected(Index_row, Index_col)) {
                    comp.setBackground(UIManager.getColor("Table[Enabled+Selected].textBackground"));
                    comp.setForeground(Color.WHITE);
		} else {
                    if (Index_col == 1) {
                            WrappedTextCellRenderer.updateBackgroundColor(comp, Color.WHITE);
                            comp.setForeground(UIManager.getColor("Table.textForeground"));
                    } else {
                            comp.setBackground(Color.WHITE);
                            comp.setForeground(UIManager.getColor("Table.textForeground"));
                    }
		}
		return comp;
	}
}
