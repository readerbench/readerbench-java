package DAO.lexicalChains;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import DAO.Word;
import edu.cmu.lti.jawjaw.pobj.Sense;

/**
 * 
 * @authors Ioana Serban, Mihai Dascalu
 */
public class LexicalChainLink implements Serializable{
	private static final long serialVersionUID = 63732297667987014L;

	private Word word;
	private Sense senseId;
	private LexicalChain lexicalChain;
	private HashMap<LexicalChainLink, Double> connections;
	private double value = 0;

	public LexicalChainLink(Word word, Sense senseId) {
		this.word = word;
		this.senseId = senseId;
		this.connections = new HashMap<LexicalChainLink, Double>();
	}

	public void addConnection(LexicalChainLink link, double weight) {
		connections.put(link, weight);
		value += weight;
	}

	public void removeConnection(LexicalChainLink link) {
		double weight = connections.remove(link);
		value -= weight;
	}

	public boolean hasSameWord(LexicalChainLink link) {
		return word.equals(link.getWord());
		// return word.equals(link.getWord()) &&
		// senseId.equals(link.getSenseId());
	}

	public Word getWord() {
		return word;
	}

	public Sense getSenseId() {
		return senseId;
	}

	public HashMap<LexicalChainLink, Double> getConnections() {
		return connections;
	}

	public double getValue() {
		return value;
	}

	public LexicalChain getLexicalChain() {
		return lexicalChain;
	}

	public void setLexicalChain(LexicalChain lexicalChain) {
		this.lexicalChain = lexicalChain;
	}

	public String toString() {
		String s = "";
		s += getWord().getText() + "[" + getSenseId() + "]: ";
		for (Map.Entry<LexicalChainLink, Double> e : connections.entrySet()) {
			s += "(" + e.getKey().getSenseId() + "["
					+ e.getKey().getWord().getText() + "], " + e.getValue()
					+ ") ";
		}
		return s;
	}
}
