package DAO.sentiment;

import java.util.HashMap;
import java.util.Map.Entry;

public class SentimentEntity {

	private HashMap<String, Double> m_sentiments;
	private String[] m_sentimentText = { "Very Negative", "Negative", "Neutral", "Positive", "Very Positive" };

	public SentimentEntity() {
		m_sentiments = new HashMap<String, Double>();
	}

	public SentimentEntity(String string, double score) {
		this();
		addSentimentResultEntity(string, score);
	}

	public void addSentimentResultEntity(String string, double score) {
		m_sentiments.put(string, score);
	}

	public HashMap<String, Double> getSentimentResultEntity() {
		return m_sentiments;
	}

	public int size() {
		return m_sentiments.size();
	}

	public int setSentimentValue(String key, double value) {
		// Tries to set a given sentiment value to a given string;
		// If the map contains the key, then it sets the new value and returns
		// 0;
		// Else it returns -1;
		if (m_sentiments.containsKey(key)) {
			m_sentiments.put(key, value);
			return 0;
		} else {
			return -1;
		}
	}

	public String getSentimentValue(String key) {
		if (m_sentiments.containsKey(key)) {
			return m_sentimentText[(int) (m_sentiments.get(key).doubleValue())];
		} else {
			return "";
		}
	}

	public double getSentimentValue() {
		// returns the first entry sentiment value
		if (!m_sentiments.isEmpty()) {
			Entry<String, Double> entry = m_sentiments.entrySet().iterator().next();
			return entry.getValue();
		} else {
			return -1;
		}
	}

	public String getSentimentKey() {
		if (!m_sentiments.isEmpty()) {
			Entry<String, Double> entry = m_sentiments.entrySet().iterator().next();
			return entry.getKey();
		} else {
			return "";
		}
	}

	public void printSentiments() {
		for (String key : m_sentiments.keySet()) {
			double value = m_sentiments.get(key);
			if (value < m_sentimentText.length)
				System.out.println(m_sentimentText[(int) value] + "\t - \t" + key);
		}
	}
}
