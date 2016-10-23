package webService.services.lak.result;

import view.widgets.article.utils.distanceStrategies.AuthorDistanceStrategyType;

/**
 *
 * @author ionutparaschiv
 */
public class TwoModeGraphEdge {

    AuthorDistanceStrategyType edgeType;
    double score;
    String sourceUri;
    String targetUri;

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

    @Override
    public String toString() {
        return "{" + this.sourceUri + " - " + this.targetUri + "}";
    }
}