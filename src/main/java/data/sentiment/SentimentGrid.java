package data.sentiment;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds an array that stores the weight of 
 * (primary sentiment, RAGE sentiment) pairs 
 * 
 * @author Gabriel Gutu
 *
 */
public class SentimentGrid<V> {

	private V[][] grid;
    private Map<String, Integer> indexes;

    /**
     * Creates the array for the (primary sentiment,
     * RAGE sentiment) array
     * 
     * @param rows
     * 			Number of primary sentiments
     * @param cols
     * 			Number of RAGE sentiments
     */
    @SuppressWarnings("unchecked")
	public SentimentGrid(int rows, int cols) {
        grid = (V[][]) new Object[rows][cols];
        indexes = new HashMap<String, Integer>();
    }

    /**
     * Sets a new index
     * 
     * @param name
     * 			index key
     * @param index
     * 			index value
     */
    public void setIndex(String key, int index) {
        indexes.put(key, index);
    }

    /**
     * Set the weight of a (primary sentiment, RAGE 
     * sentiment) pair
     * 
     * @param row
     * 			the primary sentiment identifier
     * @param col
     * 			the RAGE sentiment identifier
     * @param value
     * 			the weight of the pair
     */
    public void set(String row, String col, V weight) {
        grid[indexes.get(row)][indexes.get(col)] = weight;
    }
    
    /**
     * Gets the weight of a (primary sentiment, RAGE
     * sentiment) pair
     * 
     * @param row
     * 			the primary sentiment identifier
     * @param col
     * 			the RAGE sentiment identifier
     * @return
     * 			the weight of the pair
     */
    public V get(String row, String col) {
        return grid[indexes.get(row)][indexes.get(col)];
    }
}
