package data.discourse;

import java.text.DecimalFormat;

import cc.mallet.util.Maths;
import data.AnalysisElement;
import data.Word;
import services.commons.VectorAlgebra;
import services.semanticModels.WordNet.OntologySupport;

/**
 * Computes a semantic relatedness value of two analysis element by combining three
 * different semantic relatedness metric techniques: Latent Semantic Analysis, 
 * Latent Dirichlet Allocation and three different WordNet metrics: Leacock Chodorow,
 * Wu Palmer and PathSim.
 * 
 * @author Gabriel Gutu
 *
 */
public class SemanticRelatedness extends SemanticCohesion {
	
	private static final long serialVersionUID = 7561413289472294392L;
	
	/**
	 * Holds a value quantifying semantic relatedness between the two object of 
	 * type AnalysisElement: source and destination. 
	 */
	private double relatedness;

	/**
	 * Combines similarity metrics with different weights in order to compute a 
	 * final semantic relatedness score.  
	 *  
	 * @param WNSim
	 * 			computed WordNet similarity
	 * @param lsaSim
	 * 			computed LatentSemanticAnalysis similarity
	 * @param ldaSim
	 * 			computed Latent Dirichlet similarity
	 * @return
	 * 			computed semantic relatedness score
	 */
	public static double getSimilarityMeasure(double WNSim, double lsaSim, double ldaSim) {
		double relatedness = (WEIGH_WN * WNSim + WEIGH_LSA * lsaSim + WEIGH_LDA * ldaSim)
				/ (WEIGH_WN + WEIGH_LSA + WEIGH_LDA);
		return relatedness > 0 ? relatedness : 0;
	}

	/**
	 * @param source
	 * 			The first element for which semantic relatedness should be computed.
	 * @param destination
	 * 			The second element for which semantic relatedness should be computed.
	 */
	public SemanticRelatedness(AnalysisElement source, AnalysisElement destination) {
		super(source, destination);
		
		// helper values
		double 
			lowerValue = 0,
			upperValueLsa = 0,
			upperValueLda = 0,
			leftHandValueLsa = 0,
			leftHandValueLda = 0,
			rightHandValueLsa = 0,
			rightHandValueLda = 0;
		
		// helper values
		double
			upperValueOntology[] = new double[OntologySupport.NO_SIMILARITIES],
			leftHandValueOntology[] = new double[OntologySupport.NO_SIMILARITIES],
			rightHandValueOntology[] = new double[OntologySupport.NO_SIMILARITIES];
		
		// iterate through all words of source analysis element
		for (Word w1 : source.getWordOccurences().keySet()) {
			// helper values
			double 
				maxSimLsa = 0,
				maxSimLda = 0;
			double
				maxSimOntology[] = new double[OntologySupport.NO_SIMILARITIES];
			
			// iterate through all words of destination analysis element
			for (Word w2 : destination.getWordOccurences().keySet()) {
				// determine the word of the destination analysis element for whom
				// the Latent Semantic Analysis value is the highest with the word
				// of the source analysis element
				double localSimLsa = VectorAlgebra.cosineSimilarity(w1.getLSAVector(), w2.getLSAVector());
				if (localSimLsa > maxSimLsa) maxSimLsa = localSimLsa;
				
				// determine the word of the destination analysis element for whom
				// the Latent Dirichlet Allocation value is the highest with the word
				// of the source analysis element 
				double localSimLda;
				if (w1.getLDAProbDistribution() == null || w2.getLDAProbDistribution() == null)
					localSimLda = 0;
				else
					localSimLda = 1 - Maths.jensenShannonDivergence(VectorAlgebra.normalize(w1.getLDAProbDistribution()),
							VectorAlgebra.normalize(w2.getLDAProbDistribution()));
				if (localSimLda > maxSimLda) maxSimLda = localSimLda;
				
				// determine the word of the destination analysis element for whom
				// the WordNet similarity value is the highest with the word
				// of the source analysis element, for different algorithms
				double localOntologySim[] = new double[OntologySupport.NO_SIMILARITIES];
				localOntologySim[OntologySupport.LEACOCK_CHODOROW] = OntologySupport.semanticSimilarity(w1, w2,
						OntologySupport.LEACOCK_CHODOROW);
				if (localOntologySim[OntologySupport.LEACOCK_CHODOROW] > maxSimOntology[OntologySupport.LEACOCK_CHODOROW])
					maxSimOntology[OntologySupport.LEACOCK_CHODOROW] = localOntologySim[OntologySupport.LEACOCK_CHODOROW];
				localOntologySim[OntologySupport.WU_PALMER] = OntologySupport.semanticSimilarity(w1, w2, OntologySupport.WU_PALMER);
				if (localOntologySim[OntologySupport.WU_PALMER] > maxSimOntology[OntologySupport.WU_PALMER])
					maxSimOntology[OntologySupport.WU_PALMER] = localOntologySim[OntologySupport.WU_PALMER];
				localOntologySim[OntologySupport.PATH_SIM] = OntologySupport.semanticSimilarity(w1, w2, OntologySupport.PATH_SIM);
				if (localOntologySim[OntologySupport.PATH_SIM] > maxSimOntology[OntologySupport.PATH_SIM])
					maxSimOntology[OntologySupport.PATH_SIM] = localOntologySim[OntologySupport.PATH_SIM];
			}
			
			// TODO: multiply with term-frequency (how to?)
			upperValueLsa += maxSimLsa * w1.getIdf();
			upperValueLda += maxSimLda * w1.getIdf();
			upperValueOntology[OntologySupport.LEACOCK_CHODOROW] += maxSimOntology[OntologySupport.LEACOCK_CHODOROW] * w1.getIdf();
			upperValueOntology[OntologySupport.WU_PALMER] += maxSimOntology[OntologySupport.WU_PALMER] * w1.getIdf();
			upperValueOntology[OntologySupport.PATH_SIM] += maxSimOntology[OntologySupport.PATH_SIM] * w1.getIdf();
			lowerValue += w1.getIdf();
		}
		
		// compute the left hand side of the equation
		leftHandValueLsa = upperValueLsa/lowerValue;
		leftHandValueLda = upperValueLda/lowerValue;
		leftHandValueOntology[OntologySupport.LEACOCK_CHODOROW] = upperValueOntology[OntologySupport.LEACOCK_CHODOROW]/lowerValue;
		leftHandValueOntology[OntologySupport.WU_PALMER] = upperValueOntology[OntologySupport.WU_PALMER]/lowerValue;
		leftHandValueOntology[OntologySupport.PATH_SIM] = upperValueOntology[OntologySupport.PATH_SIM]/lowerValue;
		
		// helper values reset
		lowerValue = 0;
		upperValueLsa = 0;
		upperValueLda = 0;
		upperValueOntology = new double[OntologySupport.NO_SIMILARITIES];
		
		// iterate through all words of destination analysis element
		for (Word w1 : destination.getWordOccurences().keySet()) {
			// helper values
			double 
				maxSimLsa = 0,
				maxSimLda = 0;
			double
				maxSimOntology[] = new double[OntologySupport.NO_SIMILARITIES];
			
			// iterate through all words of source analysis element
			for (Word w2 : source.getWordOccurences().keySet()) {
				// determine the word of the destination analysis element for whom
				// the Latent Semantic Analysis value is the highest with the word
				// of the source analysis element				
				double localSimLsa = VectorAlgebra.cosineSimilarity(w1.getLSAVector(), w2.getLSAVector());
				if (localSimLsa > maxSimLsa) maxSimLsa = localSimLsa;

				// determine the word of the destination analysis element for whom
				// the Latent Dirichlet Allocation value is the highest with the word
				// of the source analysis element 
				double localSimLda;
				if (w1.getLDAProbDistribution() == null || w2.getLDAProbDistribution() == null)
					localSimLda = 0;
				else
					localSimLda = 1 - Maths.jensenShannonDivergence(VectorAlgebra.normalize(w1.getLDAProbDistribution()),
							VectorAlgebra.normalize(w2.getLDAProbDistribution()));
				if (localSimLda > maxSimLda) maxSimLda = localSimLda;

				// determine the word of the destination analysis element for whom
				// the WordNet similarity value is the highest with the word
				// of the source analysis element, for different algorithms
				double localOntologySim[] = new double[OntologySupport.NO_SIMILARITIES];
				localOntologySim[OntologySupport.LEACOCK_CHODOROW] = OntologySupport.semanticSimilarity(w1, w2,
						OntologySupport.LEACOCK_CHODOROW);
				if (localOntologySim[OntologySupport.LEACOCK_CHODOROW] > maxSimOntology[OntologySupport.LEACOCK_CHODOROW])
					maxSimOntology[OntologySupport.LEACOCK_CHODOROW] = localOntologySim[OntologySupport.LEACOCK_CHODOROW];
				localOntologySim[OntologySupport.WU_PALMER] = OntologySupport.semanticSimilarity(w1, w2, OntologySupport.WU_PALMER);
				if (localOntologySim[OntologySupport.WU_PALMER] > maxSimOntology[OntologySupport.WU_PALMER])
					maxSimOntology[OntologySupport.WU_PALMER] = localOntologySim[OntologySupport.WU_PALMER];
				localOntologySim[OntologySupport.PATH_SIM] = OntologySupport.semanticSimilarity(w1, w2, OntologySupport.PATH_SIM);
				if (localOntologySim[OntologySupport.PATH_SIM] > maxSimOntology[OntologySupport.PATH_SIM])
					maxSimOntology[OntologySupport.PATH_SIM] = localOntologySim[OntologySupport.PATH_SIM];

			}
			
			// TODO: multiply with term-frequency (how to?)
			upperValueLsa += maxSimLsa * w1.getIdf();
			upperValueLda += maxSimLda * w1.getIdf();
			upperValueOntology[OntologySupport.LEACOCK_CHODOROW] += maxSimOntology[OntologySupport.LEACOCK_CHODOROW] * w1.getIdf();
			upperValueOntology[OntologySupport.WU_PALMER] += maxSimOntology[OntologySupport.WU_PALMER] * w1.getIdf();
			upperValueOntology[OntologySupport.PATH_SIM] += maxSimOntology[OntologySupport.PATH_SIM] * w1.getIdf();
			lowerValue += w1.getIdf();
		}

		// compute the right hand side of the equation
		rightHandValueLsa = upperValueLsa/lowerValue;
		rightHandValueLda = upperValueLda/lowerValue;
		rightHandValueOntology[OntologySupport.LEACOCK_CHODOROW] = upperValueOntology[OntologySupport.LEACOCK_CHODOROW]/lowerValue;
		rightHandValueOntology[OntologySupport.WU_PALMER] = upperValueOntology[OntologySupport.WU_PALMER]/lowerValue;
		rightHandValueOntology[OntologySupport.PATH_SIM] = upperValueOntology[OntologySupport.PATH_SIM]/lowerValue;
		
		// compute the semantic relatedness values for the three different semantic
		// similarity measurement techniques
		this.lsaSim = 0.5 * (leftHandValueLsa + rightHandValueLsa);
		this.ldaSim = 0.5 * (leftHandValueLda + rightHandValueLda);
		ontologySim[OntologySupport.LEACOCK_CHODOROW] = 
				0.5 * (leftHandValueOntology[OntologySupport.LEACOCK_CHODOROW] +
						rightHandValueOntology[OntologySupport.LEACOCK_CHODOROW]);
		ontologySim[OntologySupport.WU_PALMER] = 
				0.5 * (leftHandValueOntology[OntologySupport.WU_PALMER] +
						rightHandValueOntology[OntologySupport.WU_PALMER]);
		ontologySim[OntologySupport.PATH_SIM] = 
				0.5 * (leftHandValueOntology[OntologySupport.PATH_SIM] +
						rightHandValueOntology[OntologySupport.PATH_SIM]);
		
		// compute the final semantic relatedness value by combining different metrics 
		if (Math.min(source.getWordOccurences().size(), destination.getWordOccurences().size()) > 0) {
			relatedness = getSimilarityMeasure(ontologySim[OntologySupport.WU_PALMER], lsaSim, ldaSim);
		}
	}
	
	private double getMaxSemOntologySim(AnalysisElement u1, AnalysisElement u2, int typeOfSimilarity) {
		double distance = 0;
		double sum = 0;
		// determine asymmetric measure of similarity as sum of all max
		// distances
		for (Word w1 : u1.getWordOccurences().keySet()) {
			double factor = 1 + Math.log(u1.getWordOccurences().get(w1));
			sum += factor;
			distance += factor * getMaxSemOntologySim(w1, u2, typeOfSimilarity);
		}
		// apply normalization with regards to the number of words
		if (sum > 0)
			return distance / sum;
		return 0;
	}

	/**
	 * Returns computed semantic relatedness of the two analysis elements.
	 * 
	 * @return
	 * 			semantic relatedness
	 */
	public double getRelatedness() {
		return relatedness;
	}

	/**
	 * Sets semantic relatedness of the two analysis elements.
	 * 
	 * @param relatedness
	 * 			semantic relatedness
	 */
	public void setRelatedness(double relatedness) {
		this.relatedness = relatedness;
	}

	@Override
	public String toString() {
		DecimalFormat formatter = new DecimalFormat("#.##");
		return "Semantic relatedness [ Leacock-Chodorow=" + formatter.format(ontologySim[OntologySupport.LEACOCK_CHODOROW])
				+ "; WU-Palmer=" + formatter.format(ontologySim[OntologySupport.WU_PALMER]) + "; Path="
				+ formatter.format(ontologySim[OntologySupport.PATH_SIM]) + "; cos(LSA)=" + formatter.format(lsaSim)
				+ "; sim(LDA)=" + formatter.format(ldaSim) + "]=" + formatter.format(relatedness);
	}

	public String print() {
		DecimalFormat formatter = new DecimalFormat("#.######");
		return formatter.format(lsaSim) + "," + formatter.format(ldaSim) + ","
				+ formatter.format(ontologySim[OntologySupport.LEACOCK_CHODOROW]) + ","
				+ formatter.format(ontologySim[OntologySupport.WU_PALMER]) + ","
				+ formatter.format(ontologySim[OntologySupport.PATH_SIM]) + "," + formatter.format(relatedness);
	}
}
