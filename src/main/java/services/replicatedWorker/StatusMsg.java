/* 
 * Copyright 2016 ReaderBench.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package services.replicatedWorker;

import java.io.Serializable;

public class StatusMsg implements Serializable {
	private static final long serialVersionUID = 789409540038596725L;

	public static final int HELLO_MESSAGE = 0;
	public static final int STARTING_MESSAGE = 1;
	public static final int FINISHED_MESSAGE = 2;
	public static final int KEEPALIVE_MESSAGE = 5;

	private String sourceID;
	private int type;
	private String[] arguments;
	private Object result;

	public StatusMsg(String sourceID, int type, String[] arguments,
			Object result) {
		super();
		this.sourceID = sourceID;
		this.type = type;
		this.arguments = arguments;
		this.result = result;
	}

	public String getSourceID() {
		return sourceID;
	}

	public void setSourceID(String sourceID) {
		this.sourceID = sourceID;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String[] getArguments() {
		return arguments;
	}

	public void setArguments(String[] arguments) {
		this.arguments = arguments;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public String toString() {
		String result = null;
		switch (type) {
		case KEEPALIVE_MESSAGE:
			result = sourceID + " keep-alive";
			break;
		case STARTING_MESSAGE:
			result = sourceID + " starting " + arguments[0];
			break;
		case FINISHED_MESSAGE:
			result = sourceID + " finished " + arguments[0];
			break;
		case HELLO_MESSAGE:
			result = sourceID + " hello";
			break;
		}
		return result;
	}
}
