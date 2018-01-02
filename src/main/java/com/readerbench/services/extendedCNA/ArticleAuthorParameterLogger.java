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
package com.readerbench.services.extendedCNA;

import org.openide.util.Exceptions;
import com.readerbench.services.extendedCNA.distanceStrategies.IAuthorDistanceStrategy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArticleAuthorParameterLogger {

    public static String OUT_DIRECTORY = "out";
    public static int NO_TOP_SIMILAR = 5000;

    private final ArticleContainer authorContainer;

    public ArticleAuthorParameterLogger(ArticleContainer authorContainer) {
        this.authorContainer = authorContainer;
    }

    public void logTopSimilarAuthors(IAuthorDistanceStrategy referenceStrategy, IAuthorDistanceStrategy[] allStrategies) {
        ArrayList<AuthorPairDistanceContainer> authorList = new ArrayList<>();
        for (int i = 0; i < authorContainer.getAuthorContainers().size() - 1; i++) {
            for (int j = i + 1; j < authorContainer.getAuthorContainers().size(); j++) {
                SingleAuthorContainer a1 = authorContainer.getAuthorContainers().get(i);
                SingleAuthorContainer a2 = authorContainer.getAuthorContainers().get(j);
                double sim = referenceStrategy.computeDistanceBetween(a1, a2);
                AuthorPairDistanceContainer pair = new AuthorPairDistanceContainer(a1, a2, sim);
                authorList.add(pair);
            }
        }
        Collections.sort(authorList);
        this.createOutDirIfNotExists(ArticleAuthorParameterLogger.OUT_DIRECTORY);
        try (BufferedWriter bfwrt = new BufferedWriter(new FileWriter(ArticleAuthorParameterLogger.OUT_DIRECTORY + "/" + referenceStrategy.getStrategyKey() + "_TopSimilar.csv"))) {
            bfwrt.write("Author 1,Author 2" + this.getCsvHeader(allStrategies));

            for (int i = 0; i < Math.min(authorList.size(), NO_TOP_SIMILAR); i++) {
                AuthorPairDistanceContainer pair = authorList.get(i);
                String values = pair.getFirstAuthor().getAuthor().getAuthorUri() + ","
                        + pair.getSecondAuthor().getAuthor().getAuthorUri()
                        + this.getCsvValues(pair.getFirstAuthor(), pair.getSecondAuthor(), allStrategies);

                bfwrt.newLine();
                bfwrt.write(values);
            }
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    private void createOutDirIfNotExists(String dirName) {
        File theDir = new File(dirName);
        if (!theDir.exists()) {
            try {
                theDir.mkdir();
            } catch (SecurityException se) {
            }
        }
    }

    private String getCsvHeader(IAuthorDistanceStrategy[] allStrategies) {
        String out = ",";
        for (IAuthorDistanceStrategy strategy : allStrategies) {
            out += strategy.getStrategyName() + ",";
        }
        return out.substring(0, out.length() - 1);
    }

    private String getCsvValues(SingleAuthorContainer firstAuthor, SingleAuthorContainer secondAuthor, IAuthorDistanceStrategy[] allStrategies) {
        String out = ",";
        for (IAuthorDistanceStrategy strategy : allStrategies) {
            out += strategy.computeDistanceBetween(firstAuthor, secondAuthor) + ",";
        }
        return out.substring(0, out.length() - 1);
    }

    public void logGraphMeasures(List<GraphMeasure> graphMeasures) {
        this.createOutDirIfNotExists(ArticleAuthorParameterLogger.OUT_DIRECTORY);
        try (BufferedWriter bfwrtAuthor = new BufferedWriter(new FileWriter(OUT_DIRECTORY + "/AuthorMeasures.csv")); BufferedWriter bfwrtArticle = new BufferedWriter(new FileWriter(OUT_DIRECTORY + "/ArticleMeasures.csv"))) {
            bfwrtAuthor.write("Name,URI,Betwenness,Eccentricity,Closeness,Degree,Published Articles,Total Citations,Citations 2011,Citations 2012,Citations 2013,Citations 2014,Citations 2015");
            bfwrtArticle.write("Name,URI,Betwenness,Eccentricity,Closeness,Degree,No Of References");
            for (GraphMeasure measure : graphMeasures) {

                String lineText = measure.getName().replaceAll(",", " ") + "," + measure.getUri() + "," + measure.getBetwenness() + ","
                        + measure.getEccentricity() + "," + measure.getCloseness() + ","
                        + measure.getDegree();

                if (measure.getNodeType() == GraphNodeItemType.Author) {
                    bfwrtAuthor.newLine();
                    lineText += "," + this.getNoPublishedArticles(measure.getUri()) + ",,,,,,";
                    bfwrtAuthor.write(lineText);
                }
                if (measure.getNodeType() == GraphNodeItemType.Article) {
                    bfwrtArticle.newLine();
                    lineText += "," + measure.getNoOfReferences();
                    bfwrtArticle.write(lineText);
                }
            }
            bfwrtAuthor.close();
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    private int getNoPublishedArticles(String authorUri) {
        for (SingleAuthorContainer aContainer : this.authorContainer.getAuthorContainers()) {
            if (aContainer.getAuthor().getAuthorUri().equals(authorUri)) {
                return aContainer.getAuthorArticles().size();
            }
        }
        return 0;
    }
}
