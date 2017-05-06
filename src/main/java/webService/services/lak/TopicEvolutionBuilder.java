package webService.services.lak;

import data.article.ResearchArticle;
import data.discourse.Keyword;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import services.discourse.keywordMining.KeywordModeling;
import webService.services.lak.result.TopicEvolution;

public class TopicEvolutionBuilder {

    public static int MaxNoKeywordsPerYear = 25;
    public static int MaxNoKeywords = 25;

    private List<ResearchArticle> allArticles;
    private List<Integer> years;
    private Map<Integer, List<ResearchArticle>> articlesByYears;

    public TopicEvolutionBuilder(List<ResearchArticle> articles) {
        this.allArticles = articles;
        this.indexArticles();
    }

    private void indexArticles() {
        this.articlesByYears = new TreeMap<>();
        this.years = new ArrayList<>();
        allArticles.forEach((article) -> {
            if (article.getDate() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(article.getDate());
                Integer year = cal.get(Calendar.YEAR);
                if (this.articlesByYears.containsKey(year)) {
                    this.articlesByYears.get(year).add(article);
                } else {
                    this.years.add(year);
                    List<ResearchArticle> currentArticles = new ArrayList<>();
                    currentArticles.add(article);
                    this.articlesByYears.put(year, currentArticles);
                }
            }
        });
        Collections.sort(this.years);
    }

    public TopicEvolution build() {
        List<Keyword> keywords = new ArrayList();
        Set<String> keywordSet = new TreeSet();

        years.forEach(year -> {
            List<ResearchArticle> articles = this.articlesByYears.get(year);
            List<Keyword> filteredKeywords = KeywordModeling.getCollectionTopics(articles);
            Collections.sort(filteredKeywords);
            filteredKeywords = filteredKeywords.subList(0, Math.min(filteredKeywords.size(), MaxNoKeywordsPerYear));
            filteredKeywords.forEach(filteredKeyword -> {
                if (!keywordSet.contains(filteredKeyword.getWord().getLemma())) {
                    keywordSet.add(filteredKeyword.getWord().getLemma());
                    keywords.add(filteredKeyword);
                }
            });
        });

        Collections.sort(keywords);
        List<Keyword> globalKeywords = keywords.subList(0, Math.min(keywords.size(), MaxNoKeywords));

        TopicEvolution topicEvolution = new TopicEvolution();

        years.forEach(year -> {
            topicEvolution.addYear(year);

            List<ResearchArticle> articles = this.articlesByYears.get(year);
            List<Keyword> filteredKeywords = KeywordModeling.getCollectionTopics(articles);

            globalKeywords.forEach(globalKeyword -> {
                double score = 0.0;
                for (Keyword filteredKeyword : filteredKeywords) {
                    if (globalKeyword.equals(filteredKeyword)) {
                        score = filteredKeyword.getRelevance();
                        break;
                    }
                }
                score = Math.round(score * 100.0) / 100.0;
                topicEvolution.addKeyword(globalKeyword.getWord().getLemma(), score);
            });
        });

        return topicEvolution;
    }
}
