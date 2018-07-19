package com.readerbench.comprehensionmodel.utils.indexer.graphStruct;

/**
 *
 * @author ionutparaschiv
 */
public class CMGraphStatistics {

    private double density;
    private int connectedComponentsCount;
    private double averageClusteringCoefficient;
    private double betweenness;
    private double closeness;
    private double eccentricity;
    private double diameter;
    private double pathLength;

    /**
     * @return the density
     */
    public double getDensity() {
        return density;
    }

    /**
     * @param density the density to set
     */
    public void setDensity(double density) {
        this.density = density;
    }

    /**
     * @return the connectedComponentsCount
     */
    public int getConnectedComponentsCount() {
        return connectedComponentsCount;
    }

    /**
     * @param connectedComponentsCount the connectedComponentsCount to set
     */
    public void setConnectedComponentsCount(int connectedComponentsCount) {
        this.connectedComponentsCount = connectedComponentsCount;
    }

    /**
     * @return the averageClusteringCoefficient
     */
    public double getAverageClusteringCoefficient() {
        return averageClusteringCoefficient;
    }

    /**
     * @param averageClusteringCoefficient the averageClusteringCoefficient to
     * set
     */
    public void setAverageClusteringCoefficient(double averageClusteringCoefficient) {
        this.averageClusteringCoefficient = averageClusteringCoefficient;
    }

    /**
     * @return the betweenness
     */
    public double getBetweenness() {
        return betweenness;
    }

    /**
     * @param betweenness the betweenness to set
     */
    public void setBetweenness(double betweenness) {
        this.betweenness = betweenness;
    }

    /**
     * @return the closeness
     */
    public double getCloseness() {
        return closeness;
    }

    /**
     * @param closeness the closeness to set
     */
    public void setCloseness(double closeness) {
        this.closeness = closeness;
    }

    /**
     * @return the eccentricity
     */
    public double getEccentricity() {
        return eccentricity;
    }

    /**
     * @param eccentricity the eccentricity to set
     */
    public void setEccentricity(double eccentricity) {
        this.eccentricity = eccentricity;
    }

    /**
     * @return the diameter
     */
    public double getDiameter() {
        return diameter;
    }

    /**
     * @param diameter the diameter to set
     */
    public void setDiameter(double diameter) {
        this.diameter = diameter;
    }

    /**
     * @return the pathLength
     */
    public double getPathLength() {
        return pathLength;
    }

    /**
     * @param pathLength the pathLength to set
     */
    public void setPathLength(double pathLength) {
        this.pathLength = pathLength;
    }
}
