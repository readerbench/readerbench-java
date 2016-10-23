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
package runtime.cscl;

import java.util.Date;

import org.apache.log4j.BasicConfigurator;

import data.AbstractDocument.SaveType;
import data.Lang;
import data.cscl.Community;
import services.replicatedWorker.SerialCorpusAssessment;
import view.widgets.ReaderBenchView;
import webService.ReaderBenchServer;

public class CSCLCommunityTest {

    public static void main(String[] args) {
        BasicConfigurator.configure();

        ReaderBenchServer.initializeDB();

        ReaderBenchView.adjustToSystemGraphics();
        // Community.processAllFolders("resources/in/blogs_Nic/diana/new", "", false, "resources/config/EN/LSA/TASA", "resources/config/EN/LDA/TASA", Lang.en, true, true, null, null, 0, 7);

        String path = "resources/in/MOOC/forum_posts&comments";
        SerialCorpusAssessment.processCorpus(path, "resources/config/EN/LSA/TASA", "resources/config/EN/LDA/TASA", Lang.en, true, true, true, SaveType.SERIALIZED_AND_CSV_EXPORT);
        Long startDate = 1382630400L;
        Long endDate = 1387472400L;
        Community.processDocumentCollection(path, false, false, new Date(startDate * 1000), new Date(endDate * 1000), 0, 7);

        // String path = "resources/in/forum_Nic";
        // SerialCorpusAssessment.processCorpus(path, "resources/config/EN/LSA/TASA", "resources/config/EN/LDA/TASA", Lang.eng, true, true, true, SaveType.SERIALIZED_AND_CSV_EXPORT);
        // Community.processDocumentCollection(path, false, false, null, null,
        // 0, 7);
    }
}
