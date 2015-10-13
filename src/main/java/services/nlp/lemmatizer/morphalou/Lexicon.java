package services.nlp.lemmatizer.morphalou;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class Lexicon implements Serializable {
	private static final long serialVersionUID = 6830363957212902687L;
	private List<LexicalEntry> lexicalEntries;

	public Lexicon() {
		lexicalEntries = new LinkedList<LexicalEntry>();
	}

	public List<LexicalEntry> getLexicalEntries() {
		return lexicalEntries;
	}

	public void setLexicalEntries(List<LexicalEntry> lexicalEntries) {
		this.lexicalEntries = lexicalEntries;
	}

	public void addLexicalEntry(LexicalEntry le) {
		this.lexicalEntries.add(le);
	}
}
