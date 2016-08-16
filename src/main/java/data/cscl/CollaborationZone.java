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
package data.cscl;

import java.io.Serializable;

import services.commons.Formatting;

/**
 * 
 * @author Mihai Dascalu
 */
public class CollaborationZone implements Serializable,
		Comparable<CollaborationZone> {
	private static final long serialVersionUID = 1479979327098147567L;
	
	private int start;
	private int end;
	private double averageCollaboration;
	private int noBlocks;
	private double zoneImportance;

	public CollaborationZone(int start, int end, double averageCollaboration,
			int noBlocks) {
		this.start = start;
		this.end = end;
		this.averageCollaboration = averageCollaboration;
		this.noBlocks = noBlocks;
		this.zoneImportance = this.noBlocks * this.averageCollaboration;
	}

	public CollaborationZone(int start, int end) {
		this.start = start;
		this.end = end;
	}

	public String toStringDetailed() {
		return "[" + start + "; " + end + "] - " + noBlocks
				+ " utterances with "
				+ Formatting.formatNumber(averageCollaboration)
				+ " average values ("
				+ Formatting.formatNumber(zoneImportance)
				+ " cumulative values)";
	}

	public String toString() {
		return "[" + start + " ," + end + "]";
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public double getAverageCollaboration() {
		return averageCollaboration;
	}

	public void setAverageCollaboration(double averageCollaboration) {
		this.averageCollaboration = averageCollaboration;
	}

	public int getNoBlocks() {
		return noBlocks;
	}

	public void setNoBlocks(int noBlocks) {
		this.noBlocks = noBlocks;
	}

	public double getZoneImportance() {
		return zoneImportance;
	}

	public void setZoneImportance(double zoneImportance) {
		this.zoneImportance = zoneImportance;
	}

	public int compareTo(CollaborationZone o) {
		return (int) (Math.signum(o.getAverageCollaboration()
				- this.getAverageCollaboration()));
	}

}
