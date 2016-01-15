package webService.result;

import java.util.List;

public class ResultValence implements Comparable<ResultValence> {

	private String content;
	private double score;

	public ResultValence(String content, double score) {
		super();
		this.content = content;
		this.score = score;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public double getScore() {
		return score;
	}

	@Override
	public int compareTo(ResultValence o) {
		return (int) Math.signum(o.getScore() - this.getScore());
	}
}
