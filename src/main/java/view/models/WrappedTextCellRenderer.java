package view.models;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JEditorPane;
import javax.swing.JTable;
import javax.swing.UIDefaults;
import javax.swing.table.TableCellRenderer;

public class WrappedTextCellRenderer extends JEditorPane implements
		TableCellRenderer {
	private static final long serialVersionUID = 6692116929632805735L;

	public static void updateBackgroundColor(Component c, Color bgColor) {
		UIDefaults defaults = new UIDefaults();
		defaults.put("EditorPane[Enabled].backgroundPainter", bgColor);
		JEditorPane component = (JEditorPane) c;
		component.putClientProperty("Nimbus.Overrides", defaults);
		component.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
		component.setBackground(bgColor);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		this.setEditable(false);
		this.setContentType("text/html");
		this.setText((String) value);

		// set the JEditorPane to the width of the table column
		setSize(table.getColumnModel().getColumn(column).getWidth(),
				getPreferredSize().height);
		if (table.getRowHeight(row) != getPreferredSize().height) {
			// set the height of the table row to the calculated height of the
			// JEditorPane
			table.setRowHeight(row, getPreferredSize().height);
		}
		return this;
	}

}
