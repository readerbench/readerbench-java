package runtime.cscl;

import data.cscl.Conversation;
import data.AbstractDocument.SaveType;
import data.Lang;

public class CSCLContributionSimilarities {

	private static String conversationPath = "resources/in/corpus_v2_sample/abboud_352c2_in.xml";

	public static void main() {
		Conversation c = Conversation.load(conversationPath, "resources/config/LSA/tasa_en",
				"resources/config/LDA/tasa_en", Lang.eng, false, true);
		c.computeAll(true, null, null, SaveType.SERIALIZED_AND_CSV_EXPORT);
	}
}
