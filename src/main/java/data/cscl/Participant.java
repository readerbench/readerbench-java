package data.cscl;

import java.io.Serializable;
import java.util.EnumMap;

import data.AbstractDocument;

public class Participant implements Comparable<Participant>, Serializable {
	private static final long serialVersionUID = -4515721505776009876L;

	private String name;
	private transient AbstractDocument interventions;
	private transient AbstractDocument significantInterventions;
	private double gradeAnnotator;
	private double textualComplexityLevel;
	private EnumMap<CSCLIndices, Double> indices;

	public Participant(String name, AbstractDocument d) {
		super();
		this.name = name;
		this.interventions = new Conversation(null, d.getLSA(), d.getLDA(), d.getLanguage());
		this.significantInterventions = new Conversation(null, d.getLSA(), d.getLDA(), d.getLanguage());
		this.indices = new EnumMap<>(CSCLIndices.class);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public AbstractDocument getInterventions() {
		return interventions;
	}

	public void setInterventions(AbstractDocument interventions) {
		this.interventions = interventions;
	}

	public AbstractDocument getSignificantInterventions() {
		return significantInterventions;
	}

	public void setSignificantInterventions(AbstractDocument significantInterventions) {
		this.significantInterventions = significantInterventions;
	}

	public double getGradeAnnotator() {
		return gradeAnnotator;
	}

	public void setGradeAnnotator(double gradeAnnotator) {
		this.gradeAnnotator = gradeAnnotator;
	}
	
	public double getTextualComplexityLevel() {
		return textualComplexityLevel;
	}

	public void setTextualComplexityLevel(double textualComplexityLevel) {
		this.textualComplexityLevel = textualComplexityLevel;
	}

	public EnumMap<CSCLIndices, Double> getIndices() {
		return indices;
	}

	public void setIndices(EnumMap<CSCLIndices, Double> indices) {
		this.indices = indices;
	}

	public void resetIndices() {
		for (CSCLIndices index : CSCLIndices.values())
			indices.put(index, 0d);
	}

	@Override
	public String toString() {
		return name;
	}

	public boolean equals(Object obj) {
		Participant p = (Participant) obj;
		return this.getName().equals(p.getName());
	}

	public int compareTo(Participant o) {
		return this.getName().compareTo(o.getName());
	}
}
