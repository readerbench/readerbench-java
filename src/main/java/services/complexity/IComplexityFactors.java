/**
 * 
 */
package services.complexity;

import java.util.ResourceBundle;

import data.AbstractDocument;

/**
 * @author Mihai Dascalu
 * 
 */
public abstract class IComplexityFactors {
	public abstract String getClassName();
	
	public abstract void setComplexityIndexDescription(String[] descriptions);
	public abstract void setComplexityIndexAcronym(String[] acronyms);
	
	protected String getComplexityIndexAcronym(String indexName) {
		String text = ResourceBundle.getBundle("services.complexity.index_acronyms").getString(indexName);
		if(text == null || text.length() == 0) {
			return indexName;
		}
		return text;
	}
	
	public abstract int[] getIDs();

	public abstract void computeComplexityFactors(AbstractDocument d);
}
