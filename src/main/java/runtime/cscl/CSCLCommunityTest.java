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

import org.joda.time.DateTime;

import java.io.File;

import data.AbstractDocument;
import data.Lang;
import data.cscl.Community;
import data.cscl.CommunityUtils;
import services.processing.SerialProcessing;
import view.widgets.ReaderBenchView;
import webService.ReaderBenchServer;

public class CSCLCommunityTest {

    public static void main(String[] args) {

        ReaderBenchServer.initializeDB();

        ReaderBenchView.adjustToSystemGraphics();

//        String path = "resources/in/MOOC/forum_posts&comments";
        String path = "resources/in/1 year/";
        SerialProcessing.processCorpus(path, "resources/config/EN/LSA/TASA_LAK", "resources/config/EN/LDA/TASA_LAK", Lang.en, true, true, true, AbstractDocument.SaveType.SERIALIZED_AND_CSV_EXPORT);
        Community.processDocumentCollection(path, Lang.en, false, false, true, false, false, false, false, null, null, 0, 7);


        File dir = new File(path);
        if (dir.isDirectory()) {
            File[] communityFolder = dir.listFiles();
            for(File file: communityFolder){
                if(file.isDirectory())
                    computeCommunity(file.getPath());
            }
        }
    }

    public static void computeCommunity(String path){
        SerialProcessing.processCorpus(path, "resources/config/EN/LSA/TASA", "resources/config/EN/LDA/TASA", Lang.en, true, true, true, AbstractDocument.SaveType.SERIALIZED_AND_CSV_EXPORT);
        CommunityUtils.processDocumentCollectionForClustering(path, false, false, new DateTime(2012, 8, 1, 0, 0).toDate(),
                                                              new DateTime(2013, 7, 11, 0, 0).toDate(), 0, 7);
    }
}
