package services.comprehensionModel.utils.distanceStrategies.utils;

import edu.stanford.nlp.ling.CoreLabel;

public class CMCoref {

    private CoreLabel token;
    private CoreLabel referencedToken;

    public CMCoref(CoreLabel token, CoreLabel referencedToken) {
        this.token = token;
        this.referencedToken = referencedToken;
    }

    public CoreLabel getToken() {
        return token;
    }

    public CoreLabel getReferencedToken() {
        return referencedToken;
    }

    public int getSentenceIndex() {
        return this.token.sentIndex();
    }
}
