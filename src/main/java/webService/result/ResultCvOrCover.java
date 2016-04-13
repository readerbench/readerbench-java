package webService.result;

import java.util.List;
import java.util.Map;

import data.Word;

public class ResultCvOrCover {

	public ResultTopic getConcepts() {
		return concepts;
	}
	public void setConcepts(ResultTopic concepts) {
		this.concepts = concepts;
	}
	public List<ResultSentiment> getSentiments() {
		return sentiments;
	}
	public void setSentiments(List<ResultSentiment> sentiments) {
		this.sentiments = sentiments;
	}
	public ResultCvOrCover(ResultTopic concepts, List<ResultSentiment> sentiments) {
		super();
		this.concepts = concepts;
		this.sentiments = sentiments;
	}
	private ResultTopic concepts;
	private List<ResultSentiment> sentiments;
		
}
