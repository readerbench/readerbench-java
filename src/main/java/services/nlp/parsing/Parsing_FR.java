package services.nlp.parsing;

import java.util.Properties;

import org.apache.log4j.BasicConfigurator;

import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.AbstractDocumentTemplate.BlockTemplate;
import data.document.Document;
import edu.cmu.lti.jawjaw.pobj.Lang;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class Parsing_FR {

	public static StanfordCoreNLP pipeline = new StanfordCoreNLP(
			new ParsingParams_FR());

	public static void main(String[] args) {
		BasicConfigurator.configure();
		AbstractDocumentTemplate docTmp = getDocumentModel();
		AbstractDocument d = new Document(
				null,
				docTmp,
				null, null, Lang.fr, true, false);
		System.out.println(d);
	}

	/**
	 * @return
	 */
	protected static AbstractDocumentTemplate getDocumentModel() {
		AbstractDocumentTemplate docTmp = new AbstractDocumentTemplate();
		BlockTemplate block = docTmp.new BlockTemplate();
		block.setId(0);
		block.setContent("Ce soir-là, la famille de Matilda "
				+ "dînait rapidement comme d'habitude devant la télévision.\nTout est bon.");
		docTmp.getBlocks().add(block);
		return docTmp;
	}
}

class ParsingParams_FR extends Properties {
	private static final long serialVersionUID = -161579346328207322L;

	public ParsingParams_FR() {
		super();
		this.put("tokenize.language", "fr");
		this.put("pos.model", "resources/config/POSmodels/french.tagger");
		this.put("parse.model", "edu/stanford/nlp/models/lexparser/frenchFactored.ser.gz");
		this.put("parse.flags", "");
		this.put("parse.buildgraphs", "false");
		this.put("annotators", "tokenize, ssplit, pos, parse");
	}
}
