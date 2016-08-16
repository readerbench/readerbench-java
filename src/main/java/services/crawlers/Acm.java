/* 
 * Copyright 2016 ReaderBench.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package services.crawlers;

import dao.CategoryDAO;
import dao.CategoryPhraseDAO;
import dao.DAOService;
import data.pojo.Category;
import data.pojo.CategoryPhrase;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Stefan
 */
public class Acm {
    private static final String ACM_URL = "http://dl.acm.org/ccs_flat.cfm";
    
    public static Map<String, List<String>> extractCategories() {
        Map<String, List<String>> result = new HashMap<>();
        Document doc;
        try {
            doc = Jsoup.connect(ACM_URL).get();
        }
        catch (IOException ex) {
            return result;
        }
        Elements categories = doc.select("li div");
        for (int i = 0; i < categories.size() - 1; i++) { //the last one is not a category
            Element category = categories.get(i);
            List<String> phrases = category.parent().select("ul li a")
                    .stream().map(a -> a.text()).collect(Collectors.toList());
            result.put(category.text(), phrases);
        }
        return result;
    }
    
    public static void insertToDB(Map<String, List<String>> extractedCategories) {
        for (Map.Entry<String, List<String>> e : extractedCategories.entrySet()) {
            Category category = new Category();
            category.setLabel(e.getKey());
            CategoryDAO.getInstance().save(category);
            for (String phrase : e.getValue()) {
                CategoryPhrase cp = new CategoryPhrase();
                cp.setLabel(phrase);
                cp.setFkCategory(category);
                CategoryPhraseDAO.getInstance().save(cp);
            }
        }
    }
     
    public static void main(String[] args) {
        Map<String, List<String>> extractedCategories = extractCategories();
        //insertToDB(extractedCategories);
        //DAOService.getInstance().close();
        System.out.println( extractedCategories.values().stream().mapToInt(List::size).sum() );
    }
}
