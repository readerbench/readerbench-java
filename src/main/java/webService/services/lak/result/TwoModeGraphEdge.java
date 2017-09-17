package webService.services.lak.result;

import services.extendedCNA.distanceStrategies.AuthorDistanceStrategyType;

/**
 *
 * @author ionutparaschiv
 */
public class TwoModeGraphEdge {

    private final AuthorDistanceStrategyType edgeType;
    private final double score;
    private final String sourceUri;
    private final String targetUri;

    public TwoModeGraphEdge(AuthorDistanceStrategyType edgeType, double score, String sourceUri, String targetUri) {
        this.edgeType = edgeType;
        this.score = score;
        this.sourceUri = sourceUri;
        this.targetUri = targetUri;
    }

    public String getSourceUri() {
        return this.sourceUri;
    }

    public String getTargetUri() {
        return this.targetUri;
    }
    
    public double getScore() {
        return this.score;
    }
    
    public AuthorDistanceStrategyType getEdgeType() {
        return this.edgeType;
    }

    @Override
    public String toString() {
        return "{" + this.sourceUri + " - " + this.targetUri + "}";
    }
}
