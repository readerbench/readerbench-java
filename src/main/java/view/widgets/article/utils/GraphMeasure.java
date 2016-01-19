package view.widgets.article.utils;

public class GraphMeasure {
	private Double betwenness;
	private Double eccentricity;
	private Double closeness;
	private Double degree;
	private String authorUri;
	
	public Double getBetwenness() {
		return betwenness;
	}
	public void setBetwenness(Double betwenness) {
		this.betwenness = betwenness;
	}
	public Double getCloseness() {
		return closeness;
	}
	public void setCloseness(Double closeness) {
		this.closeness = closeness;
	}
	public Double getDegree() {
		return degree;
	}
	public void setDegree(Double degree) {
		this.degree = degree;
	}
	public Double getEccentricity() {
		return eccentricity;
	}
	public void setEccentricity(Double eccentricity) {
		this.eccentricity = eccentricity;
	}
	public String getAuthorUri() {
		return authorUri;
	}
	public void setAuthorUri(String authorUri) {
		this.authorUri = authorUri;
	}
}
