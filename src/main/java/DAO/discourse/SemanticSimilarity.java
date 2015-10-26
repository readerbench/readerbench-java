package DAO.discourse;

import java.io.Serializable;
import java.text.DecimalFormat;

import services.commons.VectorAlgebra;
import services.semanticModels.WordNet.OntologySupport;
import DAO.AnalysisElement;
import DAO.Word;
import cc.mallet.util.Maths;

/**
 * @author Gabriel Gutu
 *
 */
public class SemanticSimilarity implements Serializable {
	
	private static final long serialVersionUID = 7561413289472294392L;
	
	public static final int NO_SIMILARITY_DIMENSIONS = 6;

	public static final int WINDOW_SIZE = 20;
	public static final double WEIGH_WN = 1.0;
	public static final double WEIGH_LSA = 1.0;
	public static final double WEIGH_LDA = 1.0;

	private AnalysisElement source;
	private AnalysisElement destination;
	private double[] ontologySim = new double[OntologySupport.NO_SIMILARITIES];
	private double lsaSim;
	private double ldaSim;

	private double similarity;

	public static double getAggregatedSemanticMeasure(double lsaSim, double ldaSim) {
		double cohesion = 0;
		double divisor = 0;
		if (lsaSim > 0) {
			divisor += WEIGH_LSA;
		} else {
			lsaSim = 0;
		}
		if (ldaSim > 0) {
			divisor += WEIGH_LDA;
		} else {
			ldaSim = 0;
		}
		if (divisor > 0)
			cohesion = (WEIGH_LSA * lsaSim + WEIGH_LDA * ldaSim) / divisor;
		if (cohesion > 0)
			return cohesion;
		return 0;
	}

	public static double getSimilarityMeasure(double WNSim, double lsaSim, double ldaSim) {
		double similarity = (WEIGH_WN * WNSim + WEIGH_LSA * lsaSim + WEIGH_LDA * ldaSim)
				/ (WEIGH_WN + WEIGH_LSA + WEIGH_LDA);
		if (similarity > 0)
			return similarity;
		return 0;
	}

	/**
	 * @param source
	 * @param destination
	 */
	public SemanticSimilarity(AnalysisElement source, AnalysisElement destination) {
		this.source = source;
		this.destination = destination;
		
		double
			lowerValue = 0,
			upperValueLsa = 0,
			upperValueLda = 0,
			leftHandValueLsa = 0,
			leftHandValueLda = 0,
			rightHandValueLsa = 0,
			rightHandValueLda = 0;
		double
			upperValueOntology[] = new double[OntologySupport.NO_SIMILARITIES],
			leftHandValueOntology[] = new double[OntologySupport.NO_SIMILARITIES],
			rightHandValueOntology[] = new double[OntologySupport.NO_SIMILARITIES];
		
		// iterate through all words of analysis element
		for (Word w1 : source.getWordOccurences().keySet()) {
			
			double 
				maxSimLsa = 0,
				maxSimLda = 0;
			double
				maxSimOntology[] = new double[OntologySupport.NO_SIMILARITIES];
			
			for (Word w2 : destination.getWordOccurences().keySet()) {
				double localSimLsa = VectorAlgebra.cosineSimilarity(w1.getLSAVector(), w2.getLSAVector());
				if (localSimLsa > maxSimLsa) maxSimLsa = localSimLsa;
				
				double localSimLda;
				if (w1.getLDAProbDistribution() == null || w2.getLDAProbDistribution() == null)
					localSimLda = 0;
				else
					localSimLda = 1 - Maths.jensenShannonDivergence(VectorAlgebra.normalize(w1.getLDAProbDistribution()),
							VectorAlgebra.normalize(w2.getLDAProbDistribution()));
				if (localSimLda > maxSimLda) maxSimLda = localSimLda;
				
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
			
			// TODO: multiply with tf?
			upperValueLsa += maxSimLsa * w1.getIdf();
			upperValueLda += maxSimLda * w1.getIdf();
			upperValueOntology[OntologySupport.LEACOCK_CHODOROW] += maxSimOntology[OntologySupport.LEACOCK_CHODOROW] * w1.getIdf();
			upperValueOntology[OntologySupport.WU_PALMER] += maxSimOntology[OntologySupport.WU_PALMER] * w1.getIdf();
			upperValueOntology[OntologySupport.PATH_SIM] += maxSimOntology[OntologySupport.PATH_SIM] * w1.getIdf();
			lowerValue += w1.getIdf();
			
		}
		
		leftHandValueLsa = upperValueLsa/lowerValue;
		leftHandValueLda = upperValueLda/lowerValue;
		leftHandValueOntology[OntologySupport.LEACOCK_CHODOROW] = upperValueOntology[OntologySupport.LEACOCK_CHODOROW]/lowerValue;
		leftHandValueOntology[OntologySupport.WU_PALMER] = upperValueOntology[OntologySupport.WU_PALMER]/lowerValue;
		leftHandValueOntology[OntologySupport.PATH_SIM] = upperValueOntology[OntologySupport.PATH_SIM]/lowerValue;
		
		lowerValue = 0;
		upperValueLsa = 0;
		upperValueLda = 0;
		upperValueOntology = new double[OntologySupport.NO_SIMILARITIES];
		
		// iterate through all words of destination analysis element
		for (Word w1 : destination.getWordOccurences().keySet()) {
					
			double 
				maxSimLsa = 0,
				maxSimLda = 0;
			double
				maxSimOntology[] = new double[OntologySupport.NO_SIMILARITIES];
			
			for (Word w2 : source.getWordOccurences().keySet()) {
				double localSimLsa = VectorAlgebra.cosineSimilarity(w1.getLSAVector(), w2.getLSAVector());
				if (localSimLsa > maxSimLsa) maxSimLsa = localSimLsa;
				
				double localSimLda;
				if (w1.getLDAProbDistribution() == null || w2.getLDAProbDistribution() == null)
					localSimLda = 0;
				else
					localSimLda = 1 - Maths.jensenShannonDivergence(VectorAlgebra.normalize(w1.getLDAProbDistribution()),
							VectorAlgebra.normalize(w2.getLDAProbDistribution()));
				if (localSimLda > maxSimLda) maxSimLda = localSimLda;
				
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
			
			// TODO: multiply with tf?
			upperValueLsa += maxSimLsa * w1.getIdf();
			upperValueLda += maxSimLda * w1.getIdf();
			upperValueOntology[OntologySupport.LEACOCK_CHODOROW] += maxSimOntology[OntologySupport.LEACOCK_CHODOROW] * w1.getIdf();
			upperValueOntology[OntologySupport.WU_PALMER] += maxSimOntology[OntologySupport.WU_PALMER] * w1.getIdf();
			upperValueOntology[OntologySupport.PATH_SIM] += maxSimOntology[OntologySupport.PATH_SIM] * w1.getIdf();
			lowerValue += w1.getIdf();
			
		}
		
		rightHandValueLsa = upperValueLsa/lowerValue;
		rightHandValueLda = upperValueLda/lowerValue;
		rightHandValueOntology[OntologySupport.LEACOCK_CHODOROW] = upperValueOntology[OntologySupport.LEACOCK_CHODOROW]/lowerValue;
		rightHandValueOntology[OntologySupport.WU_PALMER] = upperValueOntology[OntologySupport.WU_PALMER]/lowerValue;
		rightHandValueOntology[OntologySupport.PATH_SIM] = upperValueOntology[OntologySupport.PATH_SIM]/lowerValue;
		
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
		
		if (Math.min(source.getWordOccurences().size(), destination.getWordOccurences().size()) > 0) {
			similarity = getSimilarityMeasure(ontologySim[OntologySupport.WU_PALMER], lsaSim, ldaSim);
		}
	}
	
	public AnalysisElement getSource() {
		return source;
	}

	public void setSource(AnalysisElement source) {
		this.source = source;
	}

	public AnalysisElement getDestination() {
		return destination;
	}

	public void setDestination(AnalysisElement destination) {
		this.destination = destination;
	}

	// compute semantic distance between word and Analysis Element
	public double getMaxSemOntologySim(Word w1, AnalysisElement u2, int typeOfSimilarity) {
		double maxLocalDist = 0;
		// identify closest concept
		for (Word w2 : u2.getWordOccurences().keySet()) {
			if (w1.getLemma().equals(w2.getLemma()) || w1.getStem().equals(w2.getStem())) {
				return 1;
			} else {
				maxLocalDist = Math.max(maxLocalDist, OntologySupport.semanticSimilarity(w1, w2, typeOfSimilarity));
			}
		}
		return maxLocalDist;
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

	// compute symmetric measure of similarity
	private double getOntologySim(AnalysisElement u1, AnalysisElement u2, int typeOfSimilarity) {
		return 1.0d / 2
				* (getMaxSemOntologySim(u1, u2, typeOfSimilarity) + getMaxSemOntologySim(u2, u1, typeOfSimilarity));
	}

	public double getLSASim() {
		return lsaSim;
	}

	public void setLSASim(double lsaSim) {
		this.lsaSim = lsaSim;
	}

	public double getLDASim() {
		return ldaSim;
	}

	public void setLDASim(double ldaSim) {
		this.ldaSim = ldaSim;
	}

	public double getSimilarity() {
		return similarity;
	}

	public double[] getSemanticDistances() {
		return new double[] { ontologySim[OntologySupport.LEACOCK_CHODOROW], ontologySim[OntologySupport.WU_PALMER],
				ontologySim[OntologySupport.PATH_SIM], lsaSim, ldaSim, similarity };
	}

	public static String[] getSemanticDistanceNames() {
		// Normalized Leackock-Chodorow by log(2*ontology depth)
		return new String[] { "Leackock-Chodorow", "Wu-Palmer", "Inverse path length", "LSA", "LDA",
				"Aggregated score" };
	}

	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}

	public double[] getOntologySim() {
		return ontologySim;
	}

	public void setOntologySim(double[] ontologySim) {
		this.ontologySim = ontologySim;
	}

	@Override
	public String toString() {
		DecimalFormat formatter = new DecimalFormat("#.##");
		// TODO: Why was it cohesion here?
		return "Similarity [ Leacock-Chodorow=" + formatter.format(ontologySim[OntologySupport.LEACOCK_CHODOROW])
				+ "; WU-Palmer=" + formatter.format(ontologySim[OntologySupport.WU_PALMER]) + "; Path="
				+ formatter.format(ontologySim[OntologySupport.PATH_SIM]) + "; cos(LSA)=" + formatter.format(lsaSim)
				+ "; sim(LDA)=" + formatter.format(ldaSim) + "]=" + formatter.format(similarity);
	}

	public String print() {
		DecimalFormat formatter = new DecimalFormat("#.######");
		return formatter.format(lsaSim) + "," + formatter.format(ldaSim) + ","
				+ formatter.format(ontologySim[OntologySupport.LEACOCK_CHODOROW]) + ","
				+ formatter.format(ontologySim[OntologySupport.WU_PALMER]) + ","
				+ formatter.format(ontologySim[OntologySupport.PATH_SIM]) + "," + formatter.format(similarity);
	}
}
