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

    private final IWordDistanceStrategy wordDistanceStrategy;
    private List<Word> wordList;
    private double[][] distances;

    public WordDistanceIndexer(List<Word> wordList, IWordDistanceStrategy wordDistanceStrategy) {
        this.wordDistanceStrategy = wordDistanceStrategy;
        this.wordList = wordList;
        this.indexDistances();
    }

    public List<Word> getWordList() {
        return wordList;
    }

    public double[][] getDistances() {
        return distances;
    }

    private void indexDistances() {
        this.distances = new double[this.wordList.size()][this.wordList.size()];
        for (int i = 0; i < this.wordList.size(); i++) {
            for (int j = 0; j < this.wordList.size(); j++) {
                if (i == j) {
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
        List<Word> newWordList = new ArrayList<>();
        for (int i = 0; i < this.wordList.size(); i++) {
            double maxWordDist = this.getMaxDistanceValueForWordAtLine(i);
            if (maxWordDist >= threshold) {
                newWordList.add(this.wordList.get(i));
            }
        }
        this.wordList = newWordList;
        this.indexDistances();
    }

    private double getAvgPlusStddevThreshold(double minimumDistance) {
        double totalDist = 0.0, numCompared = 0, stddevPartial = 0.0;

        for (int i = 0; i < wordList.size(); i++) {
            for (int j = i + 1; j < wordList.size(); j++) {
                double distance = this.distances[i][j];
                if (distance >= minimumDistance) {
                    numCompared++;
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
        for (int j = 0; j < this.distances[lineNumber].length; j++) {
            if (this.distances[lineNumber][j] > max) {
                max = this.distances[lineNumber][j];
            }
        }
        return max;
    }

    public CMGraphDO getCMGraph(CMNodeType nodeType) {
        CMGraphDO graph = new CMGraphDO();
        graph.setNodeList(new ArrayList<>());
        graph.setEdgeList(new ArrayList<>());

        Set<CMNodeDO> nodeSet = new HashSet<>();
        for (int i = 0; i < this.distances.length; i++) {
            for (int j = i + 1; j < this.distances[i].length; j++) {
                if (i != j && this.distances[i][j] > 0) {
                    Word w1 = this.wordList.get(i);
                    Word w2 = this.wordList.get(j);
                    CMEdgeDO edge = new CMEdgeDO(new CMNodeDO(w1, nodeType), new CMNodeDO(w2, nodeType), this.wordDistanceStrategy.getCMEdgeType(), this.wordDistanceStrategy.getDistance(w1, w2));
                    if (edge.getScore() > 0) {
                        nodeSet.add(edge.getNode1());
                        nodeSet.add(edge.getNode2());
                        graph.getEdgeList().add(edge);
                    }
                }
            }
        }
        graph.setNodeList((new CMUtils()).convertNodeIteratorToList(nodeSet.iterator()));
        return graph;
    }
}
