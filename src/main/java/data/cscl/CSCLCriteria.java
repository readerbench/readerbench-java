package data.cscl;

import java.util.ResourceBundle;

import services.commons.VectorAlgebra;

public enum CSCLCriteria {
	AVERAGE, STDEV, SLOPE, ENTROPY, UNIFORMITY, LOCAL_EXTREME, RECURRENCE_AVERAGE, RECURRENCE_STDEV;

	public String getDescription() {
		return ResourceBundle.getBundle("utils.localization.CSCL_criteria").getString(this.name());
	}

	/**
	 * Apply a certain criteria on the input vector
	 * 
	 * @param crit
	 * @param v
	 * @return
	 */
	public static double getValue(CSCLCriteria crit, double[] v) {
		switch (crit) {
		case AVERAGE:
			return VectorAlgebra.avg(v);
		case STDEV:
			return VectorAlgebra.stdev(v);
		case SLOPE:
			return VectorAlgebra.slope(v);
		case ENTROPY:
			return VectorAlgebra.entropy(v);
		case UNIFORMITY:
			return VectorAlgebra.uniformity(v);
		case LOCAL_EXTREME:
			return VectorAlgebra.localExtremeDetection(v);
		case RECURRENCE_AVERAGE:
			return VectorAlgebra.avg(VectorAlgebra.getRecurrence(v));
		case RECURRENCE_STDEV:
			return VectorAlgebra.stdev(VectorAlgebra.getRecurrence(v));
		default:
			return -1;
		}
	}
}
