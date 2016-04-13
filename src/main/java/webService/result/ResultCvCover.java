package webService.result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.Word;

public class ResultCvCover {

	public ResultCvCover(ResultCvOrCover cv, ResultCvOrCover cover) {
		super();
		this.cv = cv;
		this.cover = cover;
	}
	public ResultCvOrCover getCv() {
		return cv;
	}
	public void setCv(ResultCvOrCover cv) {
		this.cv = cv;
	}
	public ResultCvOrCover getCover() {
		return cover;
	}
	public void setCover(ResultCvOrCover cover) {
		this.cover = cover;
	}
	public ResultCvOrCover cv;
	public ResultCvOrCover cover;
	private Map<String, Integer> wordOccurences;
	private double similarity;
	public double getSimilarity() {
		return similarity;
	}
	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}
	public Map<String, Integer> getWordOccurences() {
		return wordOccurences;
	}
	public void setWordOccurences(Map<String, Integer> wordOccurences) {
		this.wordOccurences = wordOccurences;
	}
	
}