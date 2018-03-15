package com.readerbench.gma.models;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;


public class GmaCellRenderer extends DefaultTableCellRenderer {
	
	private static final long serialVersionUID = 1L;
	ArrayList<Cell> inputCells;
	
	public GmaCellRenderer(ArrayList<Cell> inputCells){		
		this.inputCells = inputCells;
	}
	
	@Override
    public Component getTableCellRendererComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 boolean hasFocus,
                                                 int row,
                                                 int col) {
        
        Component c = super.getTableCellRendererComponent(table, value,
                                            isSelected, hasFocus, row, col);
    
            if (checkInputCellsContain(row, col)){
                c.setBackground(new Color(57, 105, 138));
                c.setForeground(Color.WHITE);
            }
            
            else if (checkOutputCellsContain(row, col)){
                c.setBackground(new Color(108,150,117));
                c.setForeground(Color.WHITE);
	        }else{
	        	//force the default state
	        	c.setBackground(Color.WHITE);
	        	c.setForeground(Color.BLACK);
	        }
        
        return c;
    }
    
    
    boolean checkInputCellsContain(int row, int column) {
        for (Cell cell : inputCells)
            if (cell.row == row && cell.col == column) 
                return true;
        
        return false;
    }
    
    boolean checkOutputCellsContain(int row, int col) {
    	
    	for (Cell inputCell : inputCells){
    		if(inputCell.getCorrelatedCells()!=null){
	    		for(Cell outputCell : inputCell.getCorrelatedCells()){
	    			if(outputCell.row == row && outputCell.col == col){
	    				return true;
	    			}
	    		}
    		}
    	}
    	

		return false;
    }
    
//	private void printCells(ArrayList<Cell> cells){
//		System.out.println("====================================================");
//		if(cells!=null){
//			for(Cell cell:cells){
//				System.out.println(cell.row + " " + cell.col + " " + cell.word.getLemma());
//				
//				if(cell.getCorrelatedCells()!=null){
//					for (Cell correlatedCell : cell.getCorrelatedCells()){
//						System.out.println("\t\t\t" + correlatedCell.row + " " + correlatedCell.col + " " + correlatedCell.word.getLemma());
//					}
//				}
//			}
//		}
//	}
    
}
