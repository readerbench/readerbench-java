package services.comprehensionModel.utils.indexer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import services.comprehensionModel.utils.CMUtils;
import services.comprehensionModel.utils.distanceStrategies.IWordDistanceStrategy;
import services.comprehensionModel.utils.indexer.graphStruct.CMEdgeDO;
import services.comprehensionModel.utils.indexer.graphStruct.CMGraphDO;
import services.comprehensionModel.utils.indexer.graphStruct.CMNodeDO;
import services.comprehensionModel.utils.indexer.graphStruct.CMNodeType;
import data.Word;

public class WordDistanceIndexer implements java.io.Serializable {
	private static final long serialVersionUID = 5625856114036715717L;
	
	private IWordDistanceStrategy wordDistanceStrategy;
	
	public List<Word> wordList;
	public double[][] distances;
	
	public WordDistanceIndexer(List<Word> wordList, IWordDistanceStrategy wordDistanceStrategy) {
		this.wordDistanceStrategy = wordDistanceStrategy;
		this.wordList = wordList;
		this.indexDistances();
	}
	private void indexDistances() {
		this.distances = new double[this.wordList.size()][this.wordList.size()];
		for(int i = 0; i < this.wordList.size(); i ++) {
			for(int j = 0; j < this.wordList.size(); j ++) {
				if(i == j) {
					this.distances[i][j] = this.distances[j][i] = 1; 
					continue;
				}
				Word w1 = this.wordList.get(i);
				Word w2 = this.wordList.get(j);
				
				this.distances[i][j] = this.distances[j][i] = wordDistanceStrategy.getDistance(w1, w2);
			}
		}
	}
	public void cutByAvgPlusStddev(double minimumDistance) {
		double threshold = this.getAvgPlusStddevThreshold(minimumDistance);
		List<Word> newWordList = new ArrayList<Word>();
		for(int i = 0; i < this.wordList.size(); i ++) {
			double maxWordDist = this.getMaxDistanceValueForWordAtLine(i);
			if(maxWordDist >= threshold) {
				newWordList.add(this.wordList.get(i));
			}
		}
		this.wordList = newWordList;
		this.indexDistances();
	}
	
	private double getAvgPlusStddevThreshold(double minimumDistance) {
		double totalDist = 0.0, numCompared = 0, stddevPartial = 0.0;
		
		for(int i = 0; i < wordList.size(); i++) {
			for(int j = i+1; j < wordList.size(); j ++) {
				double distance = this.distances[i][j];
				if(distance >= minimumDistance) {
					numCompared ++;
					totalDist += distance;
					stddevPartial += Math.pow(distance, 2);
				}
			}
		}
		if (numCompared != 0) {
			double avg = totalDist / numCompared;
			double stddev = Math.sqrt(numCompared * stddevPartial - Math.pow(totalDist, 2)) / numCompared;
			return avg - stddev;
		}
		return 0.0;
	}
	private double getMaxDistanceValueForWordAtLine(int lineNumber) {
		double max = 0.0;
		for(int j = 0; j < this.distances[lineNumber].length; j++)  {
			if(this.distances[lineNumber][j] > max) {
				max = this.distances[lineNumber][j];
			}
		}
		return max;
	}
	
	public CMGraphDO getCiGraph(CMNodeType nodeType) {
		CMGraphDO graph = new CMGraphDO();
		graph.nodeList = new ArrayList<CMNodeDO>();
		graph.edgeList = new ArrayList<CMEdgeDO>();
		
		Set<CMNodeDO> nodeSet = new HashSet<CMNodeDO>();
		for(int i = 0; i < this.distances.length; i ++) {
			for(int j = i + 1; j < this.distances[i].length; j ++) {
				if(i != j && this.distances[i][j] > 0) {
					Word w1 = this.wordList.get(i);
					Word w2 = this.wordList.get(j);
					
					CMEdgeDO edge = new CMEdgeDO();
					
					edge.node1 = new CMNodeDO();
					edge.node1.nodeType = nodeType;
					edge.node1.word = w1;
					
					edge.node2 = new CMNodeDO();
					edge.node2.nodeType = nodeType;
					edge.node2.word = w2;
					
					edge.edgeType = this.wordDistanceStrategy.getCiEdgeType();
					
					graph.edgeList.add(edge);
					nodeSet.add(edge.node1);
					nodeSet.add(edge.node2);
				}
			}
		}
		graph.nodeList = (new CMUtils()).convertNodeIteratorToList(nodeSet.iterator());
		return graph;
	}
}