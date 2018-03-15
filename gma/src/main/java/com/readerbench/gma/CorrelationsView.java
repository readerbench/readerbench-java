package com.readerbench.gma;

import com.readerbench.gma.models.Cell;
import com.readerbench.gma.models.CorrelationsTableModel;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;

public class CorrelationsView extends JFrame{
	
	private static final long serialVersionUID = 1L;
	private ArrayList<Cell> gmaCellSet;
	
	//UI
	JTable table;
	CorrelationsTableModel tableModel;
	JScrollPane tablePanel;
	JButton btnSave;
	
	public CorrelationsView(ArrayList<Cell> cellSet) {
		this.gmaCellSet = cellSet;
		
		// UI related
		setTitle("Dependences");

		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		getContentPane().setBackground(Color.WHITE);

		// adjust view to desktop size
		setBounds(30, 50, 800, 500);
		
		generateLayout();
		
	}
	
	
	private void generateLayout(){
		
		tableModel = new CorrelationsTableModel(gmaCellSet);
		table = new JTable(tableModel){
			private static final long serialVersionUID = 1L;

			public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
			{
				Component c = super.prepareRenderer(renderer, row, column);

				if(row < column && column!=0){
					c.setEnabled(false);
				}else{
					c.setEnabled(true);
				}

				return c;
			}
		};
		table.setCellSelectionEnabled(true);
		table.setFillsViewportHeight(true);
		tablePanel = new JScrollPane(table,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		this.add(tablePanel, BorderLayout.PAGE_START);
	}

}
