package webService.result;

public class ResultReadingStrategy implements Comparable<ResultReadingStrategy> {

	private String name;
	private double score;

	public ResultReadingStrategy(String name, double score) {
		this.name = name;
		this.score = score;
	}

	public String getName() {
		return name;
	}

	public double getScore() {
		return score;
	}

	@Override
	public int compareTo(ResultReadingStrategy o) {
		// Reverse order
		return (int) Math.signum(this.getScore() - o.getScore());
	}

}