package services.comprehensionModel;

import java.util.List;
import java.util.Map;

import data.Sentence;
import data.Word;
import services.comprehensionModel.utils.indexer.QueryIndexer;
import services.comprehensionModel.utils.indexer.WordDistanceIndexer;

public class ComprehensionModel {
	private QueryIndexer queryIndexer;
	
	public ComprehensionModel(String text) {
		this.queryIndexer = new QueryIndexer(text);
		
	}
	
	public WordDistanceIndexer getSemanticIndexer() {
		return this.queryIndexer.getSemanticIndexer();
	}
	
	public int getTotalNoOfPhrases() {
		return this.queryIndexer.getSyntacticIndexerList().size();
	}
	public Sentence getSentenceAtIndex(int index) {
		return this.queryIndexer.document.getSentencesInDocument().get(index);
	}
	
	public WordDistanceIndexer getSyntacticIndexerAtIndex(int index) {
		return this.queryIndexer.getSyntacticIndexerList().get(index);
	}
	
	public Map<Word, Double> getWordActivationScoreMap() {
		return this.queryIndexer.getWordActivationScoreMap();
	}
	public void updateActivationScoreMapAtIndex(int index) {
		WordDistanceIndexer indexer = this.getSyntacticIndexerAtIndex(index);
		for(int i = 0; i < indexer.wordList.size(); i ++) {
			double score = this.getWordActivationScoreMap().get(indexer.wordList.get(i));
			score++;
			this.getWordActivationScoreMap().put(indexer.wordList.get(i), score);
		}
	}
	
	public static void main(String[] args) {
		ComprehensionModel ciModel = new ComprehensionModel("RAGE aims to develop, transform and enrich advanced technologies from the leisure games industry into self-contained gaming assets (i.e. solutions showing economic value potential) that support game studios at developing applied games easier, faster and more cost-effectively. These assets will be available along with a large volume of high-quality knowledge resources through a self-sustainable Ecosystem, which is a social space that connects research, gaming industries, intermediaries, education providers, policy makers and end-users. RAGE – Realising an Applied Gaming Eco-system,  is a 48-months Technology and Know-How driven Research and Innovation project co-funded by EU Framework Programme for Research and Innovation, Horizon 2020. The EU based industry for non-leisure games – Applied Games – is an emerging business with multiple uses in industry, education, health and the public administration sectors. As such, it is still fragmented and needs critical mass to compete globally. Nevertheless its growth potential is widely recognised and even suggested to exceed the growth potential of the leisure games market. The gaming technology assets gathered along the project lifecycle will be tested and evaluated by gaming companies integrated in the RAGE consortium. These companies will be creating games that will be empirically validated in real world pilots in different application scenarios representing different markets and target groups for the Applied Games industry.");		
		System.exit(0);
	}
}