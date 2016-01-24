package webService.result;

public class ResultGraphPoint implements Comparable<ResultGraphPoint> {

	private double x;
	private double y;

	public ResultGraphPoint(double x, double y) {
		super();
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	@Override
	public int compareTo(ResultGraphPoint o) {
		return (int) Math.signum(o.getX() - this.getX());
	}
}
