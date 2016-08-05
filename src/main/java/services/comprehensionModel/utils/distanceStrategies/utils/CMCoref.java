package services.comprehensionModel.utils.distanceStrategies.utils;

import edu.stanford.nlp.ling.CoreLabel;

public class CMCoref {
	public CoreLabel token;
	public CoreLabel referencedToken;
	
	public CMCoref(CoreLabel token, CoreLabel referencedToken) {
		this.token = token;
		this.referencedToken = referencedToken;
	}
	
	public int getSentenceIndex() {
		return this.token.sentIndex();
	}
}