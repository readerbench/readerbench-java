package webService.result;

public class ResultEdge implements Comparable<ResultEdge> {

	private int source;
	private int target;
	private double score;

	public ResultEdge(int source, int target, double score) {
		super();
		this.source = source;
		this.target = target;
		this.score = score;
	}

	public int getSource() {
		return source;
	}

	public int getTarget() {
		return target;
	}

	public double getScore() {
		return score;
	}

	@Override
	public int compareTo(ResultEdge o) {
		return (int) Math.signum(o.getScore() - this.getScore());
	}
}
