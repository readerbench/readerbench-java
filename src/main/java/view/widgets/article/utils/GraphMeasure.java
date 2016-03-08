package view.widgets.article.utils;

public class GraphMeasure {
	private Double betwenness;
	private Double eccentricity;
	private Double closeness;
	private Double degree;
	private String uri;
	private GraphNodeItemType nodeType;
	
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
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public void setNodeType(GraphNodeItemType nodeType) {
		this.nodeType = nodeType;
	}
	public GraphNodeItemType getNodeType() {
		return this.nodeType;
	}
	public String getNodeTypeString() {
		switch(this.nodeType) {
		case Article:
			return "Article";
		case Author:
			return "Author";
		}
		return "";
	}
}
