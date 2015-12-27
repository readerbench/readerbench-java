package data.discourse;

import java.io.Serializable;
import java.text.DecimalFormat;

import services.commons.VectorAlgebra;
import services.semanticModels.WordNet.OntologySupport;
import data.AnalysisElement;
import data.Word;
import cc.mallet.util.Maths;

/**
 * @author Mihai Dascalu
 *
 */
public class SemanticCohesion implements Serializable {
	private static final long serialVersionUID = 7561413289472294392L;

	public static final int NO_COHESION_DIMENSIONS = 6;

	public static final int WINDOW_SIZE = 20;
	public static final double WEIGH_WN = 1.0;
	public static final double WEIGH_LSA = 1.0;
	public static final double WEIGH_LDA = 1.0;

	protected AnalysisElement source;
	protected AnalysisElement destination;
	protected double[] ontologySim = new double[OntologySupport.NO_SIMILARITIES];
	protected double lsaSim;
	protected double ldaSim;

	private double cohesion;

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

	public static double getCohesionMeasure(double WNSim, double lsaSim, double ldaSim) {
		double cohesion = (WEIGH_WN * WNSim + WEIGH_LSA * lsaSim + WEIGH_LDA * ldaSim)
				/ (WEIGH_WN + WEIGH_LSA + WEIGH_LDA);
		if (cohesion > 0)
			return cohesion;
		return 0;
	}

	/**
	 * @param source
	 * @param destination
	 */
	public SemanticCohesion(AnalysisElement source, AnalysisElement destination) {
		this.source = source;
		this.destination = destination;
		this.lsaSim = VectorAlgebra.cosineSimilarity(source.getLSAVector(), destination.getLSAVector());
		if (source.getLDAProbDistribution() == null || destination.getLDAProbDistribution() == null)
			this.ldaSim = 0;
		else
			this.ldaSim = 1 - Maths.jensenShannonDivergence(VectorAlgebra.normalize(source.getLDAProbDistribution()),
					VectorAlgebra.normalize(destination.getLDAProbDistribution()));

		ontologySim[OntologySupport.LEACOCK_CHODOROW] = getOntologySim(source, destination,
				OntologySupport.LEACOCK_CHODOROW);
		ontologySim[OntologySupport.WU_PALMER] = getOntologySim(source, destination, OntologySupport.WU_PALMER);
		ontologySim[OntologySupport.PATH_SIM] = getOntologySim(source, destination, OntologySupport.PATH_SIM);

		if (Math.min(source.getWordOccurences().size(), destination.getWordOccurences().size()) > 0) {
			cohesion = getCohesionMeasure(ontologySim[OntologySupport.WU_PALMER], lsaSim, ldaSim);
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

	public double getCohesion() {
		return cohesion;
	}

	public double[] getSemanticDistances() {
		return new double[] { ontologySim[OntologySupport.LEACOCK_CHODOROW], ontologySim[OntologySupport.WU_PALMER],
				ontologySim[OntologySupport.PATH_SIM], lsaSim, ldaSim, cohesion };
	}

	public static String[] getSemanticDistanceNames() {
		// Normalized Leackock-Chodorow by log(2*ontology depth)
		return new String[] { "Leackock-Chodorow", "Wu-Palmer", "Inverse path length", "LSA", "LDA",
				"Aggregated score" };
	}

	public static String[] getSemanticDistanceAcronyms() {
		return new String[] { "LckChodo", "WuPalmer", "InvPathLen", "LSA", "LDA", "Aggreg" };
	}

	public void setCohesion(double cohesion) {
		this.cohesion = cohesion;
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
		return "Cohesion [ Leacock-Chodorow=" + formatter.format(ontologySim[OntologySupport.LEACOCK_CHODOROW])
				+ "; WU-Palmer=" + formatter.format(ontologySim[OntologySupport.WU_PALMER]) + "; Path="
				+ formatter.format(ontologySim[OntologySupport.PATH_SIM]) + "; cos(LSA)=" + formatter.format(lsaSim)
				+ "; sim(LDA)=" + formatter.format(ldaSim) + "]=" + formatter.format(cohesion);
	}

	public String print() {
		DecimalFormat formatter = new DecimalFormat("#.######");
		return formatter.format(lsaSim) + "," + formatter.format(ldaSim) + ","
				+ formatter.format(ontologySim[OntologySupport.LEACOCK_CHODOROW]) + ","
				+ formatter.format(ontologySim[OntologySupport.WU_PALMER]) + ","
				+ formatter.format(ontologySim[OntologySupport.PATH_SIM]) + "," + formatter.format(cohesion);
	}
}
