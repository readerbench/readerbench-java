package webService.result;

import java.util.List;

public class ResultTextCategorization {

	private ResultTopic concepts;
	private List<ResultCategory> categories;

	public ResultTextCategorization(
			ResultTopic concepts,
			List<ResultCategory> resultCategories
	) {
		this.concepts = concepts;
		this.categories = resultCategories;
	}

}
