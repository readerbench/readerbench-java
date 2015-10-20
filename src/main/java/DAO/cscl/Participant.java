package DAO.cscl;

import java.io.Serializable;

import DAO.AbstractDocument;

public class Participant implements Comparable<Participant>, Serializable {
	// the interventions for each participant need to be rebuilt each time
	private static final long serialVersionUID = -4515721505776009876L;

	private String name;
	private transient AbstractDocument interventions;
	private transient AbstractDocument significantInterventions;
	private double gradeAnnotator;
	private int noContributions;
	private double overallScore;
	private double personalKB;
	private double socialKB;
	private double degreeInterAnimation;
	private double textualComplexityLevel;
	private double indegree;
	private double outdegree;
	private double betweenness;
	private double closeness;
	private double eccentricity;
	private double noNewThreads;
	private double averageNewThreadsLength;
	private double newThreadsOverallScore;
	private double newThreadsCumulativeInteranimation;
	private double newThreadsCumulativeSocialKB;
	private double relevanceTop10Topics;
	private double noNouns;
	private double noVerbs;

	public Participant(String name, AbstractDocument d) {
		super();
		this.name = name;
		this.interventions = new Conversation(null, d.getLSA(), d.getLDA(),
				d.getLanguage());
		this.significantInterventions = new Conversation(null, d.getLSA(), d.getLDA(),
				d.getLanguage());
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

	public void setSignificantInterventions(
			AbstractDocument significantInterventions) {
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

	public double getOverallScore() {
		return overallScore;
	}

	public void setOverallScore(double overallScore) {
		this.overallScore = overallScore;
	}

	public double getPersonalKB() {
		return personalKB;
	}

	public void setPersonalKB(double personalKB) {
		this.personalKB = personalKB;
	}

	public double getSocialKB() {
		return socialKB;
	}

	public void setSocialKB(double socialKB) {
		this.socialKB = socialKB;
	}

	public int getNoContributions() {
		return noContributions;
	}

	public void setNoContributions(int noIntervention) {
		this.noContributions = noIntervention;
	}

	public double getDegreeInterAnimation() {
		return degreeInterAnimation;
	}

	public void setDegreeInterAnimation(double degreeInterAnimation) {
		this.degreeInterAnimation = degreeInterAnimation;
	}

	public double getIndegree() {
		return indegree;
	}

	public void setIndegree(double indegree) {
		this.indegree = indegree;
	}

	public double getOutdegree() {
		return outdegree;
	}

	public void setOutdegree(double outdegree) {
		this.outdegree = outdegree;
	}

	public double getBetweenness() {
		return betweenness;
	}

	public void setBetweenness(double betweenness) {
		this.betweenness = betweenness;
	}

	public double getCloseness() {
		return closeness;
	}

	public void setCloseness(double closeness) {
		this.closeness = closeness;
	}

	public double getEccentricity() {
		return eccentricity;
	}

	public void setEccentricity(double eccentricity) {
		this.eccentricity = eccentricity;
	}

	public double getNoNewThreads() {
		return noNewThreads;
	}

	public void setNoNewThreads(double noNewThreads) {
		this.noNewThreads = noNewThreads;
	}

	public double getAverageNewThreadsLength() {
		return averageNewThreadsLength;
	}

	public void setAverageNewThreadsLength(double averageNewThreadsLength) {
		this.averageNewThreadsLength = averageNewThreadsLength;
	}

	public double getNewThreadsOverallScore() {
		return newThreadsOverallScore;
	}

	public void setNewThreadsOverallScore(double newThreadsOverallScore) {
		this.newThreadsOverallScore = newThreadsOverallScore;
	}

	public double getNewThreadsCumulativeInteranimation() {
		return newThreadsCumulativeInteranimation;
	}

	public void setNewThreadsCumulativeInteranimation(
			double newThreadsCumulativeInteranimation) {
		this.newThreadsCumulativeInteranimation = newThreadsCumulativeInteranimation;
	}

	public double getNewThreadsCumulativeSocialKB() {
		return newThreadsCumulativeSocialKB;
	}

	public void setNewThreadsCumulativeSocialKB(
			double newThreadsCumulativeSocialKB) {
		this.newThreadsCumulativeSocialKB = newThreadsCumulativeSocialKB;
	}

	public double getRelevanceTop10Topics() {
		return relevanceTop10Topics;
	}

	public void setRelevanceTop10Topics(double relevanceTop10Topics) {
		this.relevanceTop10Topics = relevanceTop10Topics;
	}

	public double getNoNouns() {
		return noNouns;
	}

	public void setNoNouns(double noNouns) {
		this.noNouns = noNouns;
	}

	public double getNoVerbs() {
		return noVerbs;
	}

	public void setNoVerbs(double noVerbs) {
		this.noVerbs = noVerbs;
	}

	public void resetIndices() {
		this.overallScore = 0;
		this.personalKB = 0;
		this.socialKB = 0;
		this.degreeInterAnimation = 0;
		this.textualComplexityLevel = 0;
		this.indegree = 0;
		this.outdegree = 0;
		this.betweenness = 0;
		this.closeness = 0;
		this.eccentricity = 0;
		this.noNewThreads = 0;
		this.averageNewThreadsLength = 0;
		this.newThreadsOverallScore = 0;
		this.newThreadsCumulativeInteranimation = 0;
		this.newThreadsCumulativeSocialKB = 0;
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
