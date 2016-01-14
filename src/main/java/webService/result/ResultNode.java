package webService.result;

public class ResultNode implements Comparable<ResultNode> {

	private int id;
	private String name;
	private double value;
	private int group;

	public ResultNode(int id, String name, double value, int group) {
		super();
		this.id = id;
		this.name = name;
		this.value = value;
		this.group = group;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getValue() {
		return value;
	}

	public double getGroup() {
		return group;
	}

	@Override
	public int compareTo(ResultNode o) {
		return (int) Math.signum(o.getValue() - this.getValue());
	}
}
