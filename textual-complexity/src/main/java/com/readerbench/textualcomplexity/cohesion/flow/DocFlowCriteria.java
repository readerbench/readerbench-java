package com.readerbench.textualcomplexity.cohesion.flow;

public enum DocFlowCriteria {
	MAX_VALUE, ABOVE_MEAN_PLUS_STDEV;

	@Override
	public String toString() {
		switch (this) {
		case MAX_VALUE:
			return "Maximum value";
		case ABOVE_MEAN_PLUS_STDEV:
			return "Above mean+stdev";
		default:
			throw new IllegalArgumentException();
		}
	}

	public String getAcronym() {
		switch (this) {
		case MAX_VALUE:
			return "MaxVal";
		case ABOVE_MEAN_PLUS_STDEV:
			return "AbvMeanStdev";
		default:
			throw new IllegalArgumentException();
		}
	}
};