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
package com.readerbench.coreservices.cscl;

import com.readerbench.data.Block;
import com.readerbench.datasourceprovider.data.cscl.CollaborationZone;
import com.readerbench.datasourceprovider.data.cscl.Conversation;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class CollaborationZones {

	private Conversation chat;

	private List<CollaborationZone> convergencies;
	private double noConvergencies;
	private double noConvergenceUtterances;
	private double averageConvergencyScore;

	private List<CollaborationZone> divergencies;
	private double noDivergencies;
	private double noDivergenceUtterances;
	private double averageDivergencyScore;

	private double convergencyDivergencyZoneRatio;
	private double convergencyDivergencyUtteranceRatio;
	private double averageConvergencyDivergecyScoreRatio;

	double[] distribution;

	public CollaborationZones(Conversation chat, double[] distribution) {
		convergencies = new LinkedList<CollaborationZone>();
		divergencies = new LinkedList<CollaborationZone>();
		this.chat = chat;
		noConvergencies = 0;
		noConvergenceUtterances = 0;
		noDivergencies = 0;
		noDivergenceUtterances = 0;
		this.distribution = distribution;
	}

	public List<CollaborationZone> getCollaborations() {
		return convergencies;
	}

	public void setCollaborations(List<CollaborationZone> zones) {
		this.convergencies = zones;

		computeNoCollaborations();
		computeAverageCollaborationScore();
		sortCollaborationZones();
		computeDivergencyZones();
		computeAverageDivergencyScore();
		computeConvergencyDivergencyZoneRatio();
		computeConvergencyDivergencyUtteranceRatio();
		computeAverageConvergencyDivergecyScoreRatio();
	}

	private void computeNoCollaborations() {
		this.noConvergencies = convergencies.size();
		System.out.println("CollaborationZones::computeNoCollaborations() "
				+ noConvergencies);
		computeNoCollaborationUtterances();
	}

	private void computeNoCollaborationUtterances() {
		for (CollaborationZone zone : convergencies) {
			noConvergenceUtterances += zone.getNoBlocks();
		}
		System.out
				.println("CollaborationZones::computeNoCollaborationUtterances() "
						+ noConvergenceUtterances);
	}

	private void computeAverageCollaborationScore() {
		System.out.println("CollaborationZones::computeAverageCollaboration()");
		double collaboration = 0;

		for (CollaborationZone zone : convergencies) {
			collaboration += zone.getAverageCollaboration();
		}
		if (noConvergencies != 0)
			averageConvergencyScore = collaboration / noConvergencies;
		System.out
				.println("CollaborationZones::computeAverageCollaborationScore() final: "
						+ averageConvergencyScore);

	}

	private void sortCollaborationZones() {
		Collections.sort(convergencies, new Comparator<CollaborationZone>() {

			@Override
			public int compare(CollaborationZone z1, CollaborationZone z2) {
				if (z1.getStart() < z2.getStart()) {
					return -1;
				}
				if (z1.getStart() > z2.getStart()) {
					return 1;
				}
				return 0;
			}
		});
		System.out.println("CollaborationZones::sortCollaborationZones() done");
	}

	private void computeDivergencyZones() {
		System.out.println("CollaborationZones::computeDivergencyZones() ");
		List<Block> blocks = chat.getBlocks();
		int chatSize = blocks.size();
		int start = 0;
		int itteration = 0;
		for (CollaborationZone zone : convergencies) {
			double sumCollaboration = 0;
			int noCollaboration = 0;
			double avgCollaboration = 0;

			if (zone.getStart() < chatSize && start < zone.getStart()) {
				for (int i = start; i < zone.getStart(); i++) {
					sumCollaboration += distribution[i];
					noCollaboration++;
				}

				if (noCollaboration != 0)
					avgCollaboration = sumCollaboration / noCollaboration;

				CollaborationZone divZone = new CollaborationZone(start,
						zone.getStart() - 1, avgCollaboration, noCollaboration);

				start = zone.getEnd() + 1;
				divergencies.add(divZone);
				itteration++;
			}

			if (itteration == noConvergencies && start <= distribution.length) {
				sumCollaboration = 0;
				noCollaboration = 0;
				avgCollaboration = 0;
				// Take into account the last divergency zone
				for (int i = start; i < distribution.length; i++) {
					sumCollaboration += distribution[i];
					noCollaboration++;
				}

				if (noCollaboration != 0)
					avgCollaboration = sumCollaboration / noCollaboration;

				CollaborationZone divZone = new CollaborationZone(start,
						distribution.length, avgCollaboration, noCollaboration);
				divergencies.add(divZone);
			}
		}
		computeNoDivergencies();
	}

	private void computeNoDivergencies() {
		noDivergencies = divergencies.size();
		System.out.println("CollaborationZones::computeNoDivergencies(): "
				+ noDivergencies);
		computeNoDivergencyUtterances();
	}

	private void computeNoDivergencyUtterances() {
		for (CollaborationZone zone : divergencies) {
			noDivergenceUtterances += zone.getNoBlocks();
		}
		System.out
				.println("CollaborationZones::computeNoDivergencyUtterances(): "
						+ noDivergenceUtterances);
	}

	private void computeAverageDivergencyScore() {
		System.out
				.println("CollaborationZones::computeAverageDivergencyScore()");
		double collaboration = 0;

		for (CollaborationZone zone : divergencies) {
			collaboration += zone.getAverageCollaboration();
		}
		if (noDivergencies != 0)
			averageDivergencyScore = collaboration / noDivergencies;
		System.out
				.println("CollaborationZones::computeAverageDivergencyScore() final: "
						+ averageDivergencyScore);
	}

	private void computeConvergencyDivergencyZoneRatio() {
		System.out
				.println("CollaborationZones::computeConvergencyDivergencyZoneRatio() noCollaborations: "
						+ noConvergencies
						+ " noDivergencies: "
						+ noDivergencies);
		convergencyDivergencyZoneRatio = noConvergencies / noDivergencies;
		System.out
				.println("CollaborationZones::computeConvergencyDivergencyZoneRatio(): "
						+ convergencyDivergencyZoneRatio);
	}

	private void computeConvergencyDivergencyUtteranceRatio() {
		System.out
				.println("CollaborationZones::computeConvergencyDivergencyUtteranceRatio() noCollaborationUtterances "
						+ noConvergenceUtterances
						+ " noDivergencyUtterances "
						+ noDivergenceUtterances);
		convergencyDivergencyUtteranceRatio = noConvergenceUtterances
				/ noDivergenceUtterances;
		System.out
				.println("CollaborationZones::computeConvergencyDivergencyUtteranceRatio(): "
						+ convergencyDivergencyUtteranceRatio);
	}

	private void computeAverageConvergencyDivergecyScoreRatio() {
		averageConvergencyDivergecyScoreRatio = averageConvergencyScore
				/ averageDivergencyScore;
		System.out
				.println("CollaborationZones::computeAverageConvergencyDivergecyScoreRatio(): "
						+ averageConvergencyDivergecyScoreRatio);
	}

	public double getAverageCollaborationScore() {
		return averageConvergencyScore;
	}

	public double getNoCollaborations() {
		return noConvergencies;
	}

	public List<CollaborationZone> getDivergencies() {
		return divergencies;
	}

	public void setDivergencies(List<CollaborationZone> divergencies) {
		this.divergencies = divergencies;
	}

	public double getConvergencyDivergencyZoneRatio() {
		return convergencyDivergencyZoneRatio;
	}

	public double getNoDivergencies() {
		return noDivergencies;
	}

	public double getNoDivergencyUtterances() {
		return noDivergenceUtterances;
	}

	public double getAverageDivergencyScore() {
		return averageDivergencyScore;
	}

	public double getConvergencyDivergencyUtteranceRatio() {
		return convergencyDivergencyUtteranceRatio;
	}

	public void setConvergencyDivergencyUtteranceRatio(
			double convergencyDivergencyUtteranceRatio) {
		this.convergencyDivergencyUtteranceRatio = convergencyDivergencyUtteranceRatio;
	}

	public double getAverageConvergencyDivergecyScoreRatio() {
		return averageConvergencyDivergecyScoreRatio;
	}

	public void setAverageConvergencyDivergecyScoreRatio(
			double averageConvergencyDivergecyScoreRatio) {
		this.averageConvergencyDivergecyScoreRatio = averageConvergencyDivergecyScoreRatio;
	}
}
