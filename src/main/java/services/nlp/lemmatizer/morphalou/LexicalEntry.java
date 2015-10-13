package services.nlp.lemmatizer.morphalou;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class LexicalEntry implements Serializable {
	private static final long serialVersionUID = -908462604193317464L;
	private String orthography;
	private String grammaticalCategory;
	private String grammaticalGender;
	private List<InflectedForm> inflectedForms;

	public LexicalEntry() {
		inflectedForms = new LinkedList<InflectedForm>();
	}

	public String getOrthography() {
		return orthography;
	}

	public void setOrthography(String orthograpgy) {
		this.orthography = orthograpgy;
	}

	public String getGrammaticalCategory() {
		return grammaticalCategory;
	}

	public void setGrammaticalCategory(String grammaticalCategory) {
		POS_ pos = POS_.valueOf(grammaticalCategory);
		switch (pos) {
		case commonNoun:
			this.grammaticalCategory = "NN";
			break;
		case verb:
			this.grammaticalCategory = "VB";
			break;
		case adjective:
			this.grammaticalCategory = "JJ";
			break;
		case adverb:
			this.grammaticalCategory = "RB";
			break;
		case pronoum:
			this.grammaticalCategory = "PR";
			break;
		default:
			this.grammaticalCategory = "NN";
			break;
		}
	}

	public String getGrammaticalGender() {
		return grammaticalGender;
	}

	public void setGrammaticalGender(String grammaticalGender) {
		this.grammaticalGender = grammaticalGender;
	}

	public List<InflectedForm> getInflectedForms() {
		return inflectedForms;
	}

	public void setInflectedForms(List<InflectedForm> inflectedForms) {
		this.inflectedForms = inflectedForms;
	}

	public void addInflectedForm(InflectedForm inflectedForm) {
		this.inflectedForms.add(inflectedForm);
	}

	@Override
	public boolean equals(Object obj) {
		LexicalEntry e = (LexicalEntry) obj;
		return this.getOrthography().equals(e.getOrthography())
				&& this.getGrammaticalCategory().equals(
						e.getGrammaticalCategory());
	}
}

enum POS_ {
	verb, commonNoun, adjective, adverb, functionWord, interjection, onomatopoeia, pronoum, other
}
