package DAO.sentiment;

import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.*;

import edu.stanford.nlp.patterns.GetPatternsFromDataMultiClass;

/**
 * @author Gabriel Gutu
 *
 */
public enum SentimentValence {
	
	ANEW_VALENCE("ANEW Valence", 1),
	ANEW_AROUSAL("ANEW Arousal", 1),
	ANEW_DOMINANCE("ANEW Dominance", 1);
	
	private String name;
	private double weight;
	
	private SentimentValence(String name, double weight) {
		this.name = name;
		this.weight = weight;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public double getWeight() {
		return this.weight;
	}
	
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	
}	
	

	// available valences
	// TODO: add all valences here; recommendation: group them by decimal figure 
	/*public static Integer ANEW_VALENCE	=	11;
	public static Integer ANEW_AROUSAL	=	12;
	public static Integer ANEW_DOMINANCE	=	12;
	
	public static double ANEW_VALENCE_ONE_WEIGHT	=	0.5;
	public static double ANEW_VALENCE_TWO_WEIGHT	= 	0.5;
	
	public static Integer VADER_VALENCE_ONE =	21;
	public static Integer VADER_VALENCE_TWO =	22;
	
	public static double VADER_VALENCE_ONE_WEIGHT	=	0.5;
	public static double VADER_VALENCE_TWO_HEIGHT	=	0.5;
	
	private static Map<Integer, String> valenceNames = null;*/
	
	/**
	 * @param valence
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	/*public static String getValenceName(Integer valence) throws IllegalArgumentException, IllegalAccessException {
	    if (valenceNames == null) {
	    	Map<Integer, String> vNames = new HashMap<Integer, String>();
	    	for (Field field : Valences.class.getDeclaredFields()) {
		        if ((field.getModifiers() & (Modifier.FINAL | Modifier.STATIC)) != 0
		            && Integer.class == field.getType()) {
		            vNames.put((Integer)field.get(null), field.getName());
		       	}
		    }
	    	valenceNames = vNames;
		}
	      
	    return valenceNames.get(valence);
	  }
	*/
