package DAO.complexity;

import java.io.Serializable;

/**
 * 
 * @author Mihai Dascalu
 */
public class Measurement implements Serializable{
	private static final long serialVersionUID = -3372230226203993836L;

	private double inputClass;
	private double[] measurementValues;

	public Measurement(double c, double[] m) {
		inputClass = c;
		measurementValues = m;
	}

	public double getInputClass() {
		return inputClass;
	}

	public void setInputClass(double inputClass) {
		this.inputClass = inputClass;
	}

	public double[] getMeasurementValues() {
		return measurementValues;
	}

	public void setMeasurementValues(double[] measurementValues) {
		this.measurementValues = measurementValues;
	}
}
