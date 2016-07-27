package services.comprehensionModel.utils.distanceStrategies;

import services.comprehensionModel.utils.CMUtils;
import services.comprehensionModel.utils.indexer.graphStruct.CMEdgeType;
import data.Lang;
import data.Word;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;

public class SyntacticWordDistanceStrategy implements IWordDistanceStrategy, java.io.Serializable {
	private static final long serialVersionUID = -8051746464532082314L;
	private SemanticGraph semanticGraph;
	private Lang lang;
	private CMUtils cMUtils;
	
	public SyntacticWordDistanceStrategy(SemanticGraph semanticGraph, Lang lang) {
		this.semanticGraph = semanticGraph;
		this.lang = lang;
		this.cMUtils = new CMUtils();
	}
	
	public double getDistance(Word w1, Word w2) {
		for (SemanticGraphEdge edge : semanticGraph.edgeListSorted()) {
			Word dependentEdge = this.cMUtils.convertToWord(edge.getDependent(), this.lang);
			Word governorEdge = this.cMUtils.convertToWord(edge.getGovernor(), this.lang);
			
			if( (dependentEdge.equals(w1) && governorEdge.equals(w2)) ||
					(dependentEdge.equals(w2) && governorEdge.equals(w1))) {
				return 1.0;
			}
		}
		return 0.0;
	}
	public CMEdgeType getCiEdgeType() {
		return CMEdgeType.Syntactic;
	}
}