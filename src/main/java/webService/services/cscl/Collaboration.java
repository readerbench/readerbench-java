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
package webService.services.cscl;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import data.cscl.Conversation;
import data.cscl.Utterance;
import webService.result.ResultGraphPoint;

public class Collaboration {
	
	public static List<ResultGraphPoint> buildSocialKBGraph(Conversation c) {
		
		List<ResultGraphPoint> values = new ArrayList<ResultGraphPoint>();
		
		for (int i = 0; i < c.getBlocks().size(); i++) {
			if (c.getBlocks().get(i) != null) {
				values.add(new ResultGraphPoint("", i, ((Utterance) c.getBlocks().get(i))
						.getSocialKB()));
			}
			else {
				values.add(new ResultGraphPoint("", i, 0));
			}
		}
		
		return values;
		
	}
	
	public static List<ResultGraphPoint> buildVoiceOverlapGraph(Conversation c) {
		
		List<ResultGraphPoint> values = new ArrayList<ResultGraphPoint>();
		double[] evolution = c.getVoicePMIEvolution();
	
		if (evolution != null) for (int i = 0; i < evolution.length; i++) {
			values.add(new ResultGraphPoint("", i,  evolution[i]));
		}
			
		return values;
			
	}

}
