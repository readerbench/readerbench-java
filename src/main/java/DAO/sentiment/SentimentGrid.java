package DAO.sentiment;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Gabriel Gutu
 *
 */
public class SentimentGrid<V> {

	private V[][] grid;
    private Map<Integer, Integer> indexes;

    @SuppressWarnings("unchecked")
	public SentimentGrid(int rows, int cols) {
        grid = (V[][]) new Object[rows][cols];
        indexes = new HashMap<Integer, Integer>();
    }

    public void setIndex(Integer name, int index) {
        indexes.put(name, index);
    }

    public void set(Integer row, Integer col, V value) {
        grid[indexes.get(row)][indexes.get(col)] = value;
    }
    
    public V get(Integer row, Integer col) {
        return grid[indexes.get(row)][indexes.get(col)];
    }
	
}
