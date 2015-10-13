package services.nlp.lemmatizer.morphalou;

import java.io.Serializable;

public class InflectedForm implements Serializable{
	private static final long serialVersionUID = -3945615773969658540L;
	private String orthography;
	private String grammaticalNumber;
	private String grammaticalGender;
	private String grammaticalMood;
	private String grammaticalTense;
	private String grammaticalPerson;

	public InflectedForm() {

	}

	public String getOrthography() {
		return orthography;
	}

	public void setOrthography(String orthography) {
		this.orthography = orthography;
	}

	public String getGrammaticalNumber() {
		return grammaticalNumber;
	}

	public void setGrammaticalNumber(String grammaticalNumber) {
		this.grammaticalNumber = grammaticalNumber;
	}

	public String getGrammaticalGender() {
		return grammaticalGender;
	}

	public void setGrammaticalGender(String grammaticalGender) {
		this.grammaticalGender = grammaticalGender;
	}

	public String getGrammaticalMood() {
		return grammaticalMood;
	}

	public void setGrammaticalMood(String grammaticalMood) {
		this.grammaticalMood = grammaticalMood;
	}

	public String getGrammaticalTense() {
		return grammaticalTense;
	}

	public void setGrammaticalTense(String grammaticalTense) {
		this.grammaticalTense = grammaticalTense;
	}

	public String getGrammaticalPerson() {
		return grammaticalPerson;
	}

	public void setGrammaticalPerson(String grammaticalPerson) {
		this.grammaticalPerson = grammaticalPerson;
	}
}
