package services.complexity.flow;

import data.AbstractDocument;
import data.discourse.SemanticCohesion;
import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;

public class DocumentFlowComplexity implements IComplexityFactors {
	private final int semanticDistIndex;
	private final String semanticDistName;
	private final DocumentFlow.Criteria crit;

	public DocumentFlowComplexity(int si, DocumentFlow.Criteria crit) {
		this.semanticDistIndex = si;
		this.semanticDistName = SemanticCohesion.getSemanticDistanceNames()[si];
		this.crit = crit;
	}

	@Override
	public String getClassName() {
		return "Document flow (" + crit + ", " + semanticDistName + ")";
	}

	@Override
	public void setComplexityFactorNames(String[] names) {
		names[ComplexityIndices.DOC_FLOW_ABSOLUTE_POSITION_ACCURACY + semanticDistIndex
				+ SemanticCohesion.NO_COHESION_DIMENSIONS * (crit.equals(DocumentFlow.Criteria.ABOVE_MEAN_PLUS_STDEV)
						? 0 : 1)] = "Absolute position accuracy on topological sort" + " (" + crit + " based on " + semanticDistName + ")";
		names[ComplexityIndices.DOC_FLOW_ABSOLUTE_DISTANCE_ACCURACY + semanticDistIndex
				+ SemanticCohesion.NO_COHESION_DIMENSIONS * (crit.equals(DocumentFlow.Criteria.ABOVE_MEAN_PLUS_STDEV)
						? 0 : 1)] = "Absolute distance accuracy on topological sort" + " (" + crit + " based on " + semanticDistName + ")";
		names[ComplexityIndices.DOC_FLOW_ADJACENCY_ACCURACY + semanticDistIndex
				+ SemanticCohesion.NO_COHESION_DIMENSIONS * (crit.equals(DocumentFlow.Criteria.ABOVE_MEAN_PLUS_STDEV)
						? 0 : 1)] = "Adjacency accuracy" + " (" + crit + " based on " + semanticDistName + ")";
		names[ComplexityIndices.DOC_FLOW_SPEARMAN_CORRELATION + semanticDistIndex
				+ SemanticCohesion.NO_COHESION_DIMENSIONS * (crit.equals(DocumentFlow.Criteria.ABOVE_MEAN_PLUS_STDEV)
						? 0 : 1)] = "Spearman correlation of flow versus initial ordering" + " (" + crit + " based on "
								+ semanticDistName + ")";
		names[ComplexityIndices.DOC_FLOW_MAX_ORDERED_SEQUENCE + semanticDistIndex
				+ SemanticCohesion.NO_COHESION_DIMENSIONS * (crit.equals(DocumentFlow.Criteria.ABOVE_MEAN_PLUS_STDEV)
						? 0 : 1)] = "Maximum flow ordered sequence" + " (" + crit + " based on " + semanticDistName
								+ ")";
		names[ComplexityIndices.DOC_FLOW_AVERAGE_COHESION + semanticDistIndex
				+ SemanticCohesion.NO_COHESION_DIMENSIONS * (crit.equals(DocumentFlow.Criteria.ABOVE_MEAN_PLUS_STDEV)
						? 0 : 1)] = "Average document flow cohesion" + " (" + crit + " based on " + semanticDistName
								+ ")";
	}

	@Override
	public void computeComplexityFactors(AbstractDocument d) {
		DocumentFlow df = new DocumentFlow(d, semanticDistIndex, crit);
		d.getComplexityIndices()[ComplexityIndices.DOC_FLOW_ABSOLUTE_POSITION_ACCURACY + semanticDistIndex
				+ SemanticCohesion.NO_COHESION_DIMENSIONS
						* (crit.equals(DocumentFlow.Criteria.ABOVE_MEAN_PLUS_STDEV) ? 0 : 1)] = df
								.getAbsolutePositionAccuracy();
		d.getComplexityIndices()[ComplexityIndices.DOC_FLOW_ABSOLUTE_DISTANCE_ACCURACY + semanticDistIndex
				+ SemanticCohesion.NO_COHESION_DIMENSIONS
						* (crit.equals(DocumentFlow.Criteria.ABOVE_MEAN_PLUS_STDEV) ? 0 : 1)] = df
								.getAbsoluteDistanceAccuracy();
		d.getComplexityIndices()[ComplexityIndices.DOC_FLOW_ADJACENCY_ACCURACY + semanticDistIndex
				+ SemanticCohesion.NO_COHESION_DIMENSIONS
						* (crit.equals(DocumentFlow.Criteria.ABOVE_MEAN_PLUS_STDEV) ? 0 : 1)] = df
								.getAdjacencyAccuracy();
		d.getComplexityIndices()[ComplexityIndices.DOC_FLOW_SPEARMAN_CORRELATION + semanticDistIndex
				+ SemanticCohesion.NO_COHESION_DIMENSIONS
						* (crit.equals(DocumentFlow.Criteria.ABOVE_MEAN_PLUS_STDEV) ? 0 : 1)] = df
								.getSpearmanCorrelation();
		d.getComplexityIndices()[ComplexityIndices.DOC_FLOW_MAX_ORDERED_SEQUENCE + semanticDistIndex
				+ SemanticCohesion.NO_COHESION_DIMENSIONS
						* (crit.equals(DocumentFlow.Criteria.ABOVE_MEAN_PLUS_STDEV) ? 0 : 1)] = df
								.getMaxOrderedSequence();
		d.getComplexityIndices()[ComplexityIndices.DOC_FLOW_AVERAGE_COHESION + semanticDistIndex
				+ SemanticCohesion.NO_COHESION_DIMENSIONS
						* (crit.equals(DocumentFlow.Criteria.ABOVE_MEAN_PLUS_STDEV) ? 0 : 1)] = df
								.getAverageFlowCohesion();
	}

	@Override
	public int[] getIDs() {
		return new int[] {
				ComplexityIndices.DOC_FLOW_ABSOLUTE_POSITION_ACCURACY + semanticDistIndex
						+ SemanticCohesion.NO_COHESION_DIMENSIONS
								* (crit.equals(DocumentFlow.Criteria.ABOVE_MEAN_PLUS_STDEV) ? 0 : 1),
				ComplexityIndices.DOC_FLOW_ABSOLUTE_DISTANCE_ACCURACY + semanticDistIndex
						+ SemanticCohesion.NO_COHESION_DIMENSIONS
								* (crit.equals(DocumentFlow.Criteria.ABOVE_MEAN_PLUS_STDEV) ? 0 : 1),
				ComplexityIndices.DOC_FLOW_ADJACENCY_ACCURACY + semanticDistIndex
						+ SemanticCohesion.NO_COHESION_DIMENSIONS
								* (crit.equals(DocumentFlow.Criteria.ABOVE_MEAN_PLUS_STDEV) ? 0 : 1),
				ComplexityIndices.DOC_FLOW_SPEARMAN_CORRELATION + semanticDistIndex
						+ SemanticCohesion.NO_COHESION_DIMENSIONS
								* (crit.equals(DocumentFlow.Criteria.ABOVE_MEAN_PLUS_STDEV) ? 0 : 1),
				ComplexityIndices.DOC_FLOW_MAX_ORDERED_SEQUENCE + semanticDistIndex
						+ SemanticCohesion.NO_COHESION_DIMENSIONS
								* (crit.equals(DocumentFlow.Criteria.ABOVE_MEAN_PLUS_STDEV) ? 0 : 1),
				ComplexityIndices.DOC_FLOW_AVERAGE_COHESION + semanticDistIndex
						+ SemanticCohesion.NO_COHESION_DIMENSIONS
								* (crit.equals(DocumentFlow.Criteria.ABOVE_MEAN_PLUS_STDEV) ? 0 : 1) };

	}
}
