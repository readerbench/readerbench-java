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
public class ResultNodeAdvanced extends ResultNode implements Serializable {

    private String lemma;
    private double tf;
    private double idf;

    private double averageDistanceToHypernymTreeRoot;
    private double maxDistanceToHypernymTreeRoot;
    private int polysemyCount;

    private List<ResultValence> semanticSimilarities;

    public ResultNodeAdvanced(int id, String name, double value, int group) {
        super(id, name, value, group);
        semanticSimilarities = new ArrayList<>();
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
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

}
