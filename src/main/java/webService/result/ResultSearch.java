package webService.result;

public class ResultSearch implements Comparable<ResultSearch> {

	private String url;
	private String content;
	private double relevance;

	public ResultSearch(String url, String content, double relevance) {
		this.url = url;
		this.content = content;
		this.relevance = relevance;
	}

	public String getUrl() {
		return url;
	}

	public String getContent() {
		return content;
	}

	public double getRelevance() {
		return relevance;
	}

	@Override
	public int compareTo(ResultSearch o) {
		return (int) Math.signum(o.getRelevance() - this.getRelevance());
	}
}
