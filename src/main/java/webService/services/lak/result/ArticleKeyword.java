package webService.services.lak.result;

import java.util.ArrayList;
import java.util.List;

public class ArticleKeyword {

    public String value;
    public String type;
    public List<Double> scoreList;

    public ArticleKeyword(String value, String type) {
        this.value = value;
        this.type = type;
        this.scoreList = new ArrayList<>();
    }
}
