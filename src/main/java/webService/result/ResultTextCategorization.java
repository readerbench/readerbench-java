package webService.result;

import java.util.List;

public class ResultTextCategorization {

	private List<ResultKeyword> keywords;
	private List<ResultCategory> categories;

	public ResultTextCategorization(
			List<ResultKeyword> resultKeywords,
			List<ResultCategory> resultCategories
	) {
		this.keywords = resultKeywords;
		this.categories = resultCategories;
	}

}
