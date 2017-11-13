/*************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 *  Copyright 2016 Adobe Systems Incorporated
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by all applicable intellectual property
 * laws, including trade secret and copyright laws.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 **************************************************************************/

package data.cscl;

import java.util.Arrays;

public class ParticipantNormalized {

    private String name;
    private double indegree;
    private double outdegree;
    private double eccentricity;
    private double[] vector;

    public ParticipantNormalized(double indegree, double outdegree, double eccentricity) {
        this.indegree = indegree;
        this.outdegree = outdegree;
        this.eccentricity = eccentricity;
        this.vector = new double[]{indegree, outdegree, eccentricity};
    }

    public ParticipantNormalized(double indegree, double outdegree) {
        this.indegree = indegree;
        this.outdegree = outdegree;
        this.eccentricity = eccentricity;
        this.vector = new double[]{indegree, outdegree};
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

    public double getEccentricity() {
        return eccentricity;
    }

    public void setEccentricity(double eccentricity) {
        this.eccentricity = eccentricity;
    }

    public double[] getVector() {
        return vector;
    }

    public void setVector(double[] vector) {
        this.vector = vector;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name + "," + indegree + "," + outdegree;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParticipantNormalized)) return false;

        ParticipantNormalized that = (ParticipantNormalized) o;

        if (Double.compare(that.eccentricity, eccentricity) != 0) return false;
        if (Double.compare(that.indegree, indegree) != 0) return false;
        if (Double.compare(that.outdegree, outdegree) != 0) return false;
        if (!name.equals(that.name)) return false;
        if (!Arrays.equals(vector, that.vector)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = name.hashCode();
        temp = Double.doubleToLongBits(indegree);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(outdegree);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(eccentricity);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}

