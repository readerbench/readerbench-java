/**
 * 
 */
package services.complexity;

import DAO.AbstractDocument;

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
