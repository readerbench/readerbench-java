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
package services.extendedCNA;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.Exceptions;

public class GraphMeasure implements Comparable<GraphMeasure>, java.io.Serializable {

    public static String SerializedFileLocation = "resources/in/LAK_corpus/graphMeasures.ser";

    private String name;
    private Double betwenness;
    private Double eccentricity;
    private Double closeness;
    private Double degree;
    private String uri;
    private GraphNodeItemType nodeType;
    private int noOfReferences;

    public Double getBetwenness() {
        return betwenness;
    }

    public void setBetwenness(Double betwenness) {
        this.betwenness = betwenness;
    }

    public Double getCloseness() {
        return closeness;
    }

    public void setCloseness(Double closeness) {
        this.closeness = closeness;
    }

    public Double getDegree() {
        return degree;
    }

    public void setDegree(Double degree) {
        this.degree = degree;
    }

    public Double getEccentricity() {
        return eccentricity;
    }

    public void setEccentricity(Double eccentricity) {
        this.eccentricity = eccentricity;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setNodeType(GraphNodeItemType nodeType) {
        this.nodeType = nodeType;
    }

    public GraphNodeItemType getNodeType() {
        return this.nodeType;
    }

    public String getNodeTypeString() {
        switch (this.nodeType) {
            case Article:
                return "Article";
            case Author:
                return "Author";
        }
        return "";
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNoOfReferences(int noOfReferences) {
        this.noOfReferences = noOfReferences;
    }

    public int getNoOfReferences() {
        return this.noOfReferences;
    }

    @Override
    public int compareTo(GraphMeasure o) {
        return o.betwenness.compareTo(this.betwenness);
    }

    @Override
    public String toString() {
        return "{" + this.name + " - " + this.betwenness + "}";
    }

    public static boolean saveSerializedObject(List<GraphMeasure> graphMeasures) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SerializedFileLocation))) {
            oos.writeObject(graphMeasures);
            return true;
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
            return false;
        }
    }

    public static List<GraphMeasure> readGraphMeasures() {
        List<GraphMeasure> measures = new ArrayList<>();
        try {
            ObjectInputStream objectinputstream = new ObjectInputStream(new FileInputStream(SerializedFileLocation));
            measures = (List<GraphMeasure>) objectinputstream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return measures;
    }
}
