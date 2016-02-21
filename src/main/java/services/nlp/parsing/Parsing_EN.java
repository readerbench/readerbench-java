package services.nlp.parsing;

import java.util.Properties;

import org.apache.log4j.BasicConfigurator;

import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.document.Document;
import edu.cmu.lti.jawjaw.pobj.Lang;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class Parsing_EN extends Parsing {

	private static Parsing_EN instance = null;

	private final StanfordCoreNLP pipeline = new StanfordCoreNLP(new ParsingParams_EN());

	private Parsing_EN() {
		lang = Lang.eng;
	}

	public static Parsing_EN getInstance() {
		if (instance == null) {
			instance = new Parsing_EN();
		}
		return instance;
	}

	@Override
	public StanfordCoreNLP getPipeline() {
		return pipeline;
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();

		AbstractDocumentTemplate docTmp = AbstractDocumentTemplate.getDocumentModel(
				"What is the private language hypothesis, and what is its importance? According to this hypothesis, the meanings of the terms of the private language are the very sensory experiences to which they refer. These experiences are private to the subject in that he alone is directly aware of them. As classically expressed, the premise is that we have knowledge by acquaintance of our sensory experiences. As the private experiences are the meanings of the words of the language, a fortiori the language itself is private. Such a hypothesis, if successfully defended, promises to solve two important philosophical problems: It explains the connection between language and reality - there is a class of expressions that are special in that their meanings are given immediately in experience and not in further verbal definition. More generally, these experiences constitute the basic semantic units in which all discursive meaning is rooted. I shall refer to this solution as the thesis of semantic autonomy. This hypothesis also provides a solution to the problem of knowledge. For the same reason that sensory experience seems such an appropriate candidate for the ultimate source of all meaning, so it seems appropriate as the ultimate foundation for all knowledge. It is the alleged character of sensory experience, as that which is immediately and directly knowable, that makes it the prime candidate for both the ultimate semantic and epistemic unit. This I shall refer to as the thesis of non-propositional knowledge (or knowledge by acquaintance). Human machine interface for ABC computer applications."
						+ " A survey of user opinion of computer system response time."
						+ " The EPS user interface management system. "
						+ "System and human system engineering testing of EPS. "
						+ "Relation of user perceived response time to error measurement.");

		AbstractDocument d = new Document(null, docTmp, null, null, Lang.eng, true, false);
		System.out.println(d);
	}
}

/**
 * @return
 */

class ParsingParams_EN extends Properties {
	private static final long serialVersionUID = -161579346328207322L;

	public ParsingParams_EN() {
		super();

		this.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, gender, depparse, sentiment");
	}
}
