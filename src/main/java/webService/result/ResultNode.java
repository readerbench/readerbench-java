/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webService.result;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class ResultNode implements Serializable, Comparable<ResultNode> {

    private int id;
	private String name;
	private double value;
	private int group;
    
    private String lemma;
    private String pos;
    private int noOcc;
    private int noLinks;
    private double degree;
    private double tf;
    private double idf;

    private double averageDistanceToHypernymTreeRoot;
    private double maxDistanceToHypernymTreeRoot;
    private int polysemyCount;

    private List<ResultValence> semanticSimilarities;

    public ResultNode(int id, String name, double value, int group) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.group = group;
        semanticSimilarities = new ArrayList<>();
    }
    
    public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getValue() {
		return value;
	}

	public double getGroup() {
		return group;
	}

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public int getNoOcc() {
        return noOcc;
    }

    public void setNoOcc(int noOcc) {
        this.noOcc = noOcc;
    }
    
    public int getNoLinks() {
        return noLinks;
    }

    public void setNoLinks(int noLinks) {
        this.noLinks = noLinks;
    }
    
    public double getDegree() {
        return degree;
    }

    public void setDegree(double degree) {
        this.degree = degree;
    }
    
    public double getTf() {
        return tf;
    }

    public void setTf(double tf) {
        this.tf = tf;
    }

    public double getIdf() {
        return idf;
    }

    public void setIdf(double idf) {
        this.idf = idf;
    }

    public double getAverageDistanceToHypernymTreeRoot() {
        return averageDistanceToHypernymTreeRoot;
    }

    public void setAverageDistanceToHypernymTreeRoot(double averageDistanceToHypernymTreeRoot) {
        this.averageDistanceToHypernymTreeRoot = averageDistanceToHypernymTreeRoot;
    }

    public double getMaxDistanceToHypernymTreeRoot() {
        return maxDistanceToHypernymTreeRoot;
    }

    public void setMaxDistanceToHypernymTreeRoot(double maxDistanceToHypernymTreeRoot) {
        this.maxDistanceToHypernymTreeRoot = maxDistanceToHypernymTreeRoot;
    }

    public int getPolysemyCount() {
        return polysemyCount;
    }

    public void setPolysemyCount(int polysemyCount) {
        this.polysemyCount = polysemyCount;
    }

    public List<ResultValence> getSemanticSimilarities() {
        return semanticSimilarities;
    }

    public void setSemanticSimilarities(List<ResultValence> semanticSimilarities) {
        this.semanticSimilarities = semanticSimilarities;
    }

    public void addSemanticSimilarity(String similarity, double score) {
        semanticSimilarities.add(new ResultValence(similarity, score));
    }
    
    @Override
	public int compareTo(ResultNode o) {
		return (int) Math.signum(o.getValue() - this.getValue());
	}

}
