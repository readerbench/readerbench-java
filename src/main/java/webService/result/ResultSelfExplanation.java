package webService.result;

import java.util.List;

public class ResultSelfExplanation {

	private String selfExplanationColored;
	private List<ResultReadingStrategy> strategies;

	public ResultSelfExplanation(String selfExplanationColored, List<ResultReadingStrategy> strategies) {
		this.selfExplanationColored = selfExplanationColored;
		this.strategies = strategies;
	}

}
