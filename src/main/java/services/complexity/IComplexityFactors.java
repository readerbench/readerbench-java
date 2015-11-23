/**
 * 
 */
package services.complexity;

import data.AbstractDocument;

/**
 * @author Mihai Dascalu
 * 
 */
public interface IComplexityFactors {
	public String getClassName();
	
	public void setComplexityFactorNames(String[] names);
	
	public int[] getIDs();

	public void computeComplexityFactors(AbstractDocument d);
}
