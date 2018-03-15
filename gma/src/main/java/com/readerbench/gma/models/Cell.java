package com.readerbench.gma.models;

import com.readerbench.data.Word;
import com.readerbench.readerbenchcore.data.discourse.Keyword;

import java.util.ArrayList;


public class Cell{
	
	public int row;
	public int col;
	public Word word;
	public Keyword underlyingTopic;
	private ArrayList<Cell> correlatedCells;
	
	public Cell(int row, int col){
		this.row = row;
		this.col = col;
	}
	
	public Cell(int row, int col, Word word, Keyword underlyingTopic){
		this.row = row;
		this.col = col;
		this.word = word;
		this.underlyingTopic = underlyingTopic;
	}
	
	public String toString(){
		String print = "Cell: " + row + " " + col;
		return print;
	}
	
	public boolean hasSameUnderlyingTopic(Cell cell){
		return cell.underlyingTopic.getWord().getLemma()
				.equalsIgnoreCase(this.underlyingTopic.getWord().getLemma());
	}
	
	public boolean equals(Cell cell){
		return this.row==cell.row && this.col == cell.col;
	}
	
	public void addToCorrelatedCells(Cell cell){
		if(correlatedCells == null){
			correlatedCells = new ArrayList<Cell>();
		}
		
		correlatedCells.add(cell);
	}
	
	public ArrayList<Cell> getCorrelatedCells(){
		return correlatedCells;
	}
	
	public void updateCorrelation(Cell cell){
		
		if(correlatedCells!=null){
			for(int i = 0; i<correlatedCells.size(); i++){
				if(correlatedCells.get(i).equals(cell)){
					// make sure the current cell does not exist in the 
					// correlated cell list of the other cell
					correlatedCells.get(i).removeCorrelate(this);
					correlatedCells.remove(i);
					return;
				}
			}
		}
		
		addToCorrelatedCells(cell);
		cell.addToCorrelatedCells(this);
	}
	
	private void removeCorrelate(Cell cell){
		for(int i = 0; i<correlatedCells.size(); i++){
			if(correlatedCells.get(i).equals(cell)){
				correlatedCells.remove(i);
				return;
			}
		}
	}
	
	public Boolean hasCorrelationWith(Cell cell){
		if(correlatedCells!=null){
			for(Cell corrlatedCell: correlatedCells){
				if(cell.equals(corrlatedCell)){
					return Boolean.TRUE;
				}
			}
		}
		
		return Boolean.FALSE;
	}
}
