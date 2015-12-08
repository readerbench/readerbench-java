package services.complexity.flow;

import data.AbstractDocument;
import data.discourse.SemanticCohesion;
import services.complexity.ComplexityIndices;
import services.complexity.IComplexityFactors;
import utils.localization.LocalizationUtils;

public class DocumentFlowComplexity extends IComplexityFactors {
	private final int semanticDistIndex;
	private final String semanticDistName;
	private final DocumentFlow.Criteria crit;

	public DocumentFlowComplexity(int si, DocumentFlow.Criteria crit) {
		this.semanticDistIndex = si;
		this.semanticDistName = SemanticCohesion.getSemanticDistanceNames()[si];
		this.crit = crit;
	}

	
	public String getClassName() {
		return LocalizationUtils.getTranslation("Document flow") + " (" + crit + ", " + semanticDistName + ")";
	}

	public void setComplexityIndexDescription(String[] descriptions) {
		descriptions[ComplexityIndices.DOC_FLOW_ABSOLUTE_POSITION_ACCURACY + semanticDistIndex
				+ SemanticCohesion.NO_COHESION_DIMENSIONS * (crit.equals(DocumentFlow.Criteria.ABOVE_MEAN_PLUS_STDEV)
						? 0 : 1)] = LocalizationUtils.getTranslation("Absolute position accuracy on topological sort") + " (" + crit + " " +  LocalizationUtils.getTranslation("based on") + " " + semanticDistName + ")";
		descriptions[ComplexityIndices.DOC_FLOW_ABSOLUTE_DISTANCE_ACCURACY + semanticDistIndex
				+ SemanticCohesion.NO_COHESION_DIMENSIONS * (crit.equals(DocumentFlow.Criteria.ABOVE_MEAN_PLUS_STDEV)
						? 0 : 1)] = LocalizationUtils.getTranslation("Absolute distance accuracy on topological sort") + " (" + crit + " " +  LocalizationUtils.getTranslation("based on") + " " + semanticDistName + ")";
		descriptions[ComplexityIndices.DOC_FLOW_ADJACENCY_ACCURACY + semanticDistIndex
				+ SemanticCohesion.NO_COHESION_DIMENSIONS * (crit.equals(DocumentFlow.Criteria.ABOVE_MEAN_PLUS_STDEV)
						? 0 : 1)] = LocalizationUtils.getTranslation("Adjacency accuracy") + " (" + crit + " " +  LocalizationUtils.getTranslation("based on") + " " + semanticDistName + ")";
		descriptions[ComplexityIndices.DOC_FLOW_SPEARMAN_CORRELATION + semanticDistIndex
				+ SemanticCohesion.NO_COHESION_DIMENSIONS * (crit.equals(DocumentFlow.Criteria.ABOVE_MEAN_PLUS_STDEV)
						? 0 : 1)] = LocalizationUtils.getTranslation("Spearman correlation of flow versus initial ordering") + " (" + crit + " " +  LocalizationUtils.getTranslation("based on") + " "
								+ semanticDistName + ")";
		descriptions[ComplexityIndices.DOC_FLOW_MAX_ORDERED_SEQUENCE + semanticDistIndex
				+ SemanticCohesion.NO_COHESION_DIMENSIONS * (crit.equals(DocumentFlow.Criteria.ABOVE_MEAN_PLUS_STDEV)
						? 0 : 1)] = LocalizationUtils.getTranslation("Maximum flow ordered sequence") + " (" + crit + " " +  LocalizationUtils.getTranslation("based on") + " " + semanticDistName
								+ ")";
		descriptions[ComplexityIndices.DOC_FLOW_AVERAGE_COHESION + semanticDistIndex
				+ SemanticCohesion.NO_COHESION_DIMENSIONS * (crit.equals(DocumentFlow.Criteria.ABOVE_MEAN_PLUS_STDEV)
						? 0 : 1)] = LocalizationUtils.getTranslation("Average document flow cohesion") + " (" + crit + " " +  LocalizationUtils.getTranslation("based on") + " " + semanticDistName
								+ ")";
	}
	public void setComplexityIndexAcronym(String[] acronyms) {
		acronyms[ComplexityIndices.DOC_FLOW_ABSOLUTE_POSITION_ACCURACY + semanticDistIndex
				+ SemanticCohesion.NO_COHESION_DIMENSIONS * (crit.equals(DocumentFlow.Criteria.ABOVE_MEAN_PLUS_STDEV)
						? 0 : 1)] = this.getComplexityIndexAcronym("DOC_FLOW_ABSOLUTE_POSITION_ACCURACY");
		acronyms[ComplexityIndices.DOC_FLOW_ABSOLUTE_DISTANCE_ACCURACY + semanticDistIndex
				+ SemanticCohesion.NO_COHESION_DIMENSIONS * (crit.equals(DocumentFlow.Criteria.ABOVE_MEAN_PLUS_STDEV)
						? 0 : 1)] = this.getComplexityIndexAcronym("DOC_FLOW_ABSOLUTE_DISTANCE_ACCURACY");
		acronyms[ComplexityIndices.DOC_FLOW_ADJACENCY_ACCURACY + semanticDistIndex
				+ SemanticCohesion.NO_COHESION_DIMENSIONS * (crit.equals(DocumentFlow.Criteria.ABOVE_MEAN_PLUS_STDEV)
						? 0 : 1)] = this.getComplexityIndexAcronym("DOC_FLOW_ADJACENCY_ACCURACY");
		acronyms[ComplexityIndices.DOC_FLOW_SPEARMAN_CORRELATION + semanticDistIndex
				+ SemanticCohesion.NO_COHESION_DIMENSIONS * (crit.equals(DocumentFlow.Criteria.ABOVE_MEAN_PLUS_STDEV)
						? 0 : 1)] = this.getComplexityIndexAcronym("DOC_FLOW_SPEARMAN_CORRELATION");
		acronyms[ComplexityIndices.DOC_FLOW_MAX_ORDERED_SEQUENCE + semanticDistIndex
				+ SemanticCohesion.NO_COHESION_DIMENSIONS * (crit.equals(DocumentFlow.Criteria.ABOVE_MEAN_PLUS_STDEV)
						? 0 : 1)] = this.getComplexityIndexAcronym("DOC_FLOW_MAX_ORDERED_SEQUENCE");
		acronyms[ComplexityIndices.DOC_FLOW_AVERAGE_COHESION + semanticDistIndex
				+ SemanticCohesion.NO_COHESION_DIMENSIONS * (crit.equals(DocumentFlow.Criteria.ABOVE_MEAN_PLUS_STDEV)
						? 0 : 1)] = this.getComplexityIndexAcronym("DOC_FLOW_AVERAGE_COHESION");
	}

	
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
