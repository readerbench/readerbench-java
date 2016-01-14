package webService.result;

import java.util.List;

public class ResultSentiment {

	private String content;
	private List<ResultValence> valences;

	public ResultSentiment(String content, List<ResultValence> valences) {
		super();
		this.content = content;
		this.valences = valences;
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