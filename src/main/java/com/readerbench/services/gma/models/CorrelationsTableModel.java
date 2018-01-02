package com.readerbench.services.gma.models;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

public class CorrelationsTableModel extends AbstractTableModel{

	private static final long serialVersionUID = 1L;
	ArrayList<Cell> cellSet;
	
	public CorrelationsTableModel(ArrayList<Cell> cellSet){
		this.cellSet = cellSet;		
	}
	
	@Override
    public String getColumnName(int pos) {       
		if(pos == 0){
			return "";
		}
        return cellSet.get(pos-1).word.getLemma();
    }
	
	

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return cellSet.size();
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return cellSet.size()+1;
	}
	
	public boolean isCellEditable(int row, int column) {
		return (row>=column);
	  }
	
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Class getColumnClass(int column) {
		if(column>0)
			return (Boolean.class);
		return String.class;
    }
    
    public void setValueAt(Object value, int row, int col) {
    	if(col>0){
	    	cellSet.get(row).updateCorrelation(cellSet.get(col-1));
	    	if(cellSet.get(col-1)==null){
	    		System.out.println("the cell is nul for col " + (col -1));
	    		System.out.println("cellSet has " + cellSet.size());
	    	}
	        fireTableCellUpdated(row, col);
    	}
    }
    

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		if(columnIndex == 0){
			return cellSet.get(rowIndex).word.getLemma();
		}else if(rowIndex>=columnIndex){
			return cellSet.get(rowIndex).hasCorrelationWith(cellSet.get(columnIndex-1));
		}
		return null;
	}

}
