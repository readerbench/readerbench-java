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
package dao;

import data.pojo.CategoryPhrase;

/**
 *
 * @author Stefan
 */
public class CategoryPhraseDAO extends AbstractDAO<CategoryPhrase>{
    private static CategoryPhraseDAO instance = null;

    private CategoryPhraseDAO() {

    }

    public static CategoryPhraseDAO getInstance() {
        if (instance == null) {
            instance = new CategoryPhraseDAO();
        }
        return instance;
    }
    
}
