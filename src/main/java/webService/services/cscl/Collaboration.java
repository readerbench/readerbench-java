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
				values.add(new ResultGraphPoint(i, ((Utterance) c.getBlocks().get(i))
						.getSocialKB()));
			}
		}
		
		return values;
		
	}
	
	public static List<ResultGraphPoint> buildVoiceOverlapGraph(Conversation c) {
		
		List<ResultGraphPoint> values = new ArrayList<ResultGraphPoint>();
		double[] evolution = c.getVoicePMIEvolution();
	
		for (int i = 0; i < evolution.length; i++) {
			values.add(new ResultGraphPoint(i,  evolution[i]));
		}
			
		return values;
			
	}

}
