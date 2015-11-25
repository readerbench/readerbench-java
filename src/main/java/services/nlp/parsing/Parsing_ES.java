package services.nlp.parsing;

import java.util.Properties;

import org.apache.log4j.BasicConfigurator;

import data.AbstractDocument;
import data.AbstractDocumentTemplate;
import data.AbstractDocumentTemplate.BlockTemplate;
import data.document.Document;
import edu.cmu.lti.jawjaw.pobj.Lang;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class Parsing_ES {

	public static StanfordCoreNLP pipeline = new StanfordCoreNLP(
			new ParsingParams_ES());

	public static void main(String[] args) {
		BasicConfigurator.configure();
		AbstractDocumentTemplate docTmp = getDocumentModel();
		AbstractDocument d = new Document(null, docTmp, null, null, Lang.es,
				true, false);
		System.out.println(d);
	}

	/**
	 * @return
	 */
	protected static AbstractDocumentTemplate getDocumentModel() {
		AbstractDocumentTemplate docTmp = new AbstractDocumentTemplate();
		BlockTemplate block = docTmp.new BlockTemplate();
		block.setId(0);
		block.setContent("Yo sí soy muy macho -grita uno- Yo me voy. Agarra sus muebles, sus hijos, sus animales, los mete en una carreta y atraviesa la calle central donde todo el pueblo lo ve. Hasta que todos dicen: Si este se atreve, pues nosotros también nos vamos. Y empiezan a desmantelar literalmente el pueblo. Se llevan las cosas, los animales, todo.Y uno de los últimos que abandona el pueblo, dice: Que no venga la desgracia a caer sobre lo que queda de nuestra casa, y entonces la incendia y otros incendian también sus casas.Huyen en un tremendo y verdadero pánico, como en un éxodo de guerra, y en medio de ellos va la señora que tuvo el presagio, le dice a su hijo que está a su lado: Vistes m'hijo, que algo muy grave iba a suceder en este pueblo?");
		docTmp.getBlocks().add(block);
		return docTmp;
	}
}

class ParsingParams_ES extends Properties {
	private static final long serialVersionUID = -161579346328207322L;

	public ParsingParams_ES() {
		super();
		this.put("tokenize.language", "es");
		this.put("pos.model", "resources/config/POSmodels/spanish-distsim.tagger");
		this.put("parse.model",
				"edu/stanford/nlp/models/lexparser/spanishPCFG.ser.gz");
		this.put("parse.flags", "");
		this.put("parse.buildgraphs", "false");
		this.put("annotators", "tokenize, ssplit, pos, parse");
	}
}
