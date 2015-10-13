package services.commons;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

public class ValueComparator<P> implements Comparator<P>, Serializable {

	private static final long serialVersionUID = 8174264967918565044L;

	Map<P, Double> base;

	public ValueComparator(Map<P, Double> base) {
		this.base = base;
	}

	public int compare(P a, P b) {
		return -((Double) base.get(a)).compareTo((Double) base.get(b));
	}
}
