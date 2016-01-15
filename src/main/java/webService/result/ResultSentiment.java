package webService.result;

import java.util.List;

public class ResultSentiment {

	private String content;
	private List<ResultValence> valences;
	private List<ResultSentiment> innerObjects;

	public ResultSentiment(String content, List<ResultValence> valences, List<ResultSentiment> innerObjects) {
		super();
		this.content = content;
		this.valences = valences;
		this.innerObjects = innerObjects;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public List<ResultValence> getValences() {
		return valences;
	}
}