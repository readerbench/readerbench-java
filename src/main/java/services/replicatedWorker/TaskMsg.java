package services.replicatedWorker;

import java.io.Serializable;

public class TaskMsg implements Serializable {
	private static final long serialVersionUID = -8443977659228408658L;
	private Object[] args;
	private boolean finish;

	public TaskMsg(Object[] args, boolean finish) {
		super();
		this.args = args;
		this.finish = finish;
	}

	public boolean isFinish() {
		return finish;
	}

	public void setFinish(boolean finish) {
		this.finish = finish;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

	public String toString() {
		return args[0].toString();
	}
}
