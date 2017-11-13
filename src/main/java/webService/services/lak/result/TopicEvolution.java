package webService.services.lak.result;

import java.util.ArrayList;
import java.util.List;

public class TopicEvolution {

    List<ArticleKeyword> wordList;
    List<Integer> yearList;

    public TopicEvolution() {
        this.wordList = new ArrayList<>();
        this.yearList = new ArrayList<>();
    }

    public void addYear(Integer year) {
        this.yearList.add(year);
    }

    public void addKeyword(String value, double score) {
        if (this.contains(value)) {
            ArticleKeyword keyword = this.get(value);
            keyword.scoreList.add(score);
            return;
        }
        ArticleKeyword keyword = new ArticleKeyword(value, "Keyword");
        keyword.scoreList.add(score);
        this.wordList.add(keyword);
    }

    private boolean contains(String value) {
        for (ArticleKeyword word : wordList) {
            if (word.value.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private ArticleKeyword get(String value) {
        for (ArticleKeyword word : wordList) {
            if (word.value.equals(value)) {
                return word;
            }
        }
        return null;
    }

    public void normalize() {
        for (int yearIndex = 0; yearIndex < this.yearList.size(); yearIndex++) {
            double maxScore = 0.0;
            for (ArticleKeyword word : this.wordList) {
                maxScore = Math.max(maxScore, word.scoreList.get(yearIndex));
            }
            if (maxScore > 0) {
                for (ArticleKeyword word : this.wordList) {
                    word.scoreList.set(yearIndex, word.scoreList.get(yearIndex) / maxScore);
                }
            }
        }
    }
}
