package webService.result;

import java.util.List;

public class ResultSemanticAnnotation {

	private ResultTopic concepts;

	private double abstractDocumentSimilarity;
	private double keywordsAbstractCoverage;
	private double keywordsDocumentCoverage;

	private List<ResultKeyword> keywords;
	private List<ResultCategory> categories;

	public ResultSemanticAnnotation(ResultTopic resultTopic, double abstractDocumentSimilarity,
			double keywordsAbstractCoverage, double keywordsDocumentCoverage, List<ResultKeyword> resultKeywords,
			List<ResultCategory> resultCategories) {
		this.concepts = resultTopic;
		this.abstractDocumentSimilarity = abstractDocumentSimilarity;
		this.keywordsAbstractCoverage = keywordsAbstractCoverage;
		this.keywordsDocumentCoverage = keywordsDocumentCoverage;
		this.keywords = resultKeywords;
		this.categories = resultCategories;
	}

}
