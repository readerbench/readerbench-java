package webService.result;

public class ResultGraphPoint implements Comparable<ResultGraphPoint> {

	private String nodeName;
	private double x;
	private double y;

	public ResultGraphPoint(String nodeName, double x, double y) {
		super();
		this.nodeName = nodeName;
		this.x = x;
		this.y = y;
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
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
